package com.kama.notes.service.impl;

import com.kama.notes.client.AliyunContentSecurityClient;
import com.kama.notes.config.NoteReviewProperties;
import com.kama.notes.event.MessageEvent;
import com.kama.notes.mapper.NoteMapper;
import com.kama.notes.model.entity.Note;
import com.kama.notes.model.entity.Question;
import com.kama.notes.service.NoteHotRankService;
import com.kama.notes.service.NoteReviewProcessor;
import com.kama.notes.service.NoteReviewQueueService;
import com.kama.notes.service.QuestionService;
import com.kama.notes.utils.MessageBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class NoteReviewProcessorImpl implements NoteReviewProcessor {

    private static final int STATUS_REVIEWING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_REJECTED = 2;

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private AliyunContentSecurityClient aliyunContentSecurityClient;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private NoteHotRankService noteHotRankService;

    @Autowired
    private NoteReviewQueueService noteReviewQueueService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private NoteReviewProperties properties;

    @Autowired
    private QuestionService questionService;

    @Override
    public void process(MapRecord<String, Object, Object> record) {
        String type = parseString(record.getValue().get("type"));
        if ("create".equals(type)) {
            processCreate(record);
            return;
        }
        processReview(record);
    }

    private void processCreate(MapRecord<String, Object, Object> record) {
        Long userId = parseLong(record.getValue().get("userId"));
        Integer questionId = parseInteger(record.getValue().get("questionId"));
        String content = parseString(record.getValue().get("content"));
        if (userId == null || questionId == null || content == null || content.isBlank()) {
            ack(record);
            return;
        }

        try {
            Question question = questionService.findById(questionId);
            if (question == null) {
                applicationEventPublisher.publishEvent(new MessageEvent(this,
                        MessageBuilder.systemAnnouncement(userId, "笔记提交失败：题目不存在或已被删除")));
                ack(record);
                return;
            }

            Note note = new Note();
            note.setAuthorId(userId);
            note.setQuestionId(questionId);
            note.setContent(content);
            note.setStatus(STATUS_REVIEWING);
            note.setReviewRetryCount(0);
            noteMapper.insert(note);
            noteReviewQueueService.enqueue(note.getNoteId(), userId);
            ack(record);
        } catch (Exception e) {
            log.error("异步创建笔记失败, userId={}, questionId={}", userId, questionId, e);
            applicationEventPublisher.publishEvent(new MessageEvent(this,
                    MessageBuilder.systemAnnouncement(userId, "笔记提交失败，请稍后重试")));
            ack(record);
        }
    }

    private void processReview(MapRecord<String, Object, Object> record) {
        Integer noteId = parseInteger(record.getValue().get("noteId"));
        Long userId = parseLong(record.getValue().get("userId"));
        if (noteId == null || userId == null) {
            ack(record);
            return;
        }

        Note note = noteMapper.findRawById(noteId);
        if (note == null || !isReviewing(note.getStatus())) {
            ack(record);
            return;
        }

        try {
            boolean passed = aliyunContentSecurityClient.textPass(note.getContent());
            sleepAfterReviewResultIfNecessary(noteId, passed);
            int updated = noteMapper.updateStatusIfCurrent(noteId, STATUS_REVIEWING, passed ? STATUS_APPROVED : STATUS_REJECTED);
            if (updated > 0) {
                if (passed) {
                    noteMapper.clearReviewError(noteId);
                    noteHotRankService.updateNoteHotScore(noteId);
                    applicationEventPublisher.publishEvent(new MessageEvent(this,
                            MessageBuilder.noteAuditApproved(userId, noteId)));
                } else {
                    applicationEventPublisher.publishEvent(new MessageEvent(this,
                            MessageBuilder.noteAuditRejected(userId, noteId)));
                }
            }
            ack(record);
        } catch (Exception e) {
            log.error("异步审核笔记失败, noteId={}", noteId, e);
            handleRetry(note, userId, e.getMessage());
            ack(record);
        }
    }

    private void handleRetry(Note note, Long userId, String errorMessage) {
        String safeError = truncate(errorMessage);
        int retryCount = note.getReviewRetryCount() == null ? 0 : note.getReviewRetryCount();
        if (retryCount + 1 >= properties.getMaxRetryCount()) {
            int updated = noteMapper.markReviewFailed(note.getNoteId(), safeError);
            if (updated > 0) {
                applicationEventPublisher.publishEvent(new MessageEvent(this,
                        MessageBuilder.noteAuditRejected(userId, note.getNoteId())));
            }
            return;
        }

        noteMapper.incrementReviewRetryCount(note.getNoteId(), safeError);
        long delaySeconds = calculateRetryDelaySeconds(retryCount + 1);
        long executeAt = Instant.now().toEpochMilli() + delaySeconds * 1000;
        noteReviewQueueService.enqueueRetry(note.getNoteId(), userId, executeAt);
    }

    private long calculateRetryDelaySeconds(int retryCount) {
        long delay = (long) (properties.getBaseRetryDelaySeconds() * Math.pow(2, Math.max(0, retryCount - 1)));
        return Math.min(delay, properties.getMaxRetryDelaySeconds());
    }

    private void sleepAfterReviewResultIfNecessary(Integer noteId, boolean passed) throws InterruptedException {
        long delayMillis = properties.getMockResultDelayMillis();
        if (delayMillis <= 0) {
            return;
        }
        log.info("审核结果已返回，延迟落库 noteId={}, passed={}, delay={}ms", noteId, passed, delayMillis);
        Thread.sleep(delayMillis);
    }

    private void ack(MapRecord<String, Object, Object> record) {
        stringRedisTemplate.opsForStream().acknowledge(properties.getGroup(), record);
    }

    private boolean isReviewing(Integer status) {
        return status != null && status == STATUS_REVIEWING;
    }

    private Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String parseString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String truncate(String text) {
        if (text == null || text.isBlank()) {
            return "审核服务调用异常";
        }
        return text.length() > 500 ? text.substring(0, 500) : text;
    }
}
