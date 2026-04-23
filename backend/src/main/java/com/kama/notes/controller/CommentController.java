package com.kama.notes.controller;

import com.kama.notes.model.base.ApiResponse;
import com.kama.notes.model.base.EmptyVO;
import com.kama.notes.model.dto.comment.CommentQueryParams;
import com.kama.notes.model.dto.comment.CreateCommentRequest;
import com.kama.notes.model.dto.comment.UpdateCommentRequest;
import com.kama.notes.model.vo.comment.CommentVO;
import com.kama.notes.model.vo.comment.CursorCommentListVO;
import com.kama.notes.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * 评论控制器
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/comments")
    public ApiResponse<CursorCommentListVO> getComments(@Valid CommentQueryParams params) {
        return commentService.getComments(params);
    }

    @GetMapping("/comments/{rootCommentId}/replies")
    public ApiResponse<List<CommentVO>> getReplies(
            @PathVariable("rootCommentId") Integer rootCommentId,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer pageSize,
            @RequestParam(defaultValue = "asc") String sort) {
        return commentService.getReplies(rootCommentId, page, pageSize, sort);
    }

    @PostMapping("/comments")
    public ApiResponse<Integer> createComment(@Valid @RequestBody CreateCommentRequest request) {
        return commentService.createComment(request);
    }

    @PatchMapping("/comments/{commentId}")
    public ApiResponse<EmptyVO> updateComment(
            @PathVariable("commentId") Integer commentId,
            @Valid @RequestBody UpdateCommentRequest request) {
        return commentService.updateComment(commentId, request);
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<EmptyVO> deleteComment(@PathVariable("commentId") Integer commentId) {
        return commentService.deleteComment(commentId);
    }

    @PostMapping("/comments/{commentId}/like")
    public ApiResponse<EmptyVO> likeComment(@PathVariable("commentId") Integer commentId) {
        return commentService.likeComment(commentId);
    }

    @DeleteMapping("/comments/{commentId}/like")
    public ApiResponse<EmptyVO> unlikeComment(@PathVariable("commentId") Integer commentId) {
        return commentService.unlikeComment(commentId);
    }
}
