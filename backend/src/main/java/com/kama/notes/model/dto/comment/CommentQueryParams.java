package com.kama.notes.model.dto.comment;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 评论查询参数
 */
@Data
public class CommentQueryParams {
    /**
     * 笔记ID
     */
    @NotNull(message = "笔记ID不能为空")
    private Integer noteId;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize;

    /**
     * 一级评论排序：hot / latest
     */
    private String sort;

    /**
     * latest 游标：创建时间
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime cursorCreatedAt;

    /**
     * latest/hot 游标：评论ID
     */
    private Integer cursorCommentId;

    /**
     * hot 游标：点赞数
     */
    private Integer cursorLikeCount;

    /**
     * hot 游标：回复数
     */
    private Integer cursorReplyCount;
}
