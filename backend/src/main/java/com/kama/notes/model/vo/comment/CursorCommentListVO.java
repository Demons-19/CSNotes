package com.kama.notes.model.vo.comment;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CursorCommentListVO {
    private List<CommentVO> items;
    private NextCursor nextCursor;
    private Boolean hasMore;

    @Data
    public static class NextCursor {
        private LocalDateTime createdAt;
        private Integer commentId;
        private Integer likeCount;
        private Integer replyCount;
    }
}
