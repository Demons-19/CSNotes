package com.kama.notes.service;

public interface NoteReviewQueueService {
    void enqueueCreate(Long userId, Integer questionId, String content);

    void enqueue(Integer noteId, Long userId);

    void enqueueRetry(Integer noteId, Long userId, long executeAtEpochMilli);
}
