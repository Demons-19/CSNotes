package com.kama.notes.service;

import com.kama.notes.model.base.ApiResponse;
import com.kama.notes.model.base.EmptyVO;
import com.kama.notes.model.dto.comment.CommentQueryParams;
import com.kama.notes.model.dto.comment.CreateCommentRequest;
import com.kama.notes.model.dto.comment.UpdateCommentRequest;
import com.kama.notes.model.vo.comment.CommentVO;
import com.kama.notes.model.vo.comment.CursorCommentListVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评论服务接口
 */
@Transactional
public interface CommentService {
    ApiResponse<Integer> createComment(CreateCommentRequest request);

    ApiResponse<EmptyVO> updateComment(Integer commentId, UpdateCommentRequest request);

    ApiResponse<EmptyVO> deleteComment(Integer commentId);

    ApiResponse<CursorCommentListVO> getComments(CommentQueryParams params);

    ApiResponse<List<CommentVO>> getReplies(Integer rootCommentId, Integer page, Integer pageSize, String sort);

    ApiResponse<EmptyVO> likeComment(Integer commentId);

    ApiResponse<EmptyVO> unlikeComment(Integer commentId);
}
