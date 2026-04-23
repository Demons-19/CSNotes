/**
 * 评论作者信息
 */
export interface CommentAuthor {
  userId: string
  username: string
  avatarUrl: string
}

/**
 * 用户操作状态
 */
export interface CommentUserActions {
  isLiked: boolean
}

/**
 * 评论数据
 */
export interface Comment {
  commentId: number
  rootCommentId?: number
  noteId: number
  content: string
  likeCount: number
  replyCount: number
  createdAt: string
  author: CommentAuthor
  replyToUser?: CommentAuthor
  userActions?: CommentUserActions
  replies?: Comment[]
}

export type CommentSort = 'hot' | 'latest'
export type ReplySort = 'asc' | 'desc'

export interface NextCursor {
  createdAt: string
  commentId: number
  likeCount: number
  replyCount: number
}

export interface CursorCommentListVO {
  items: Comment[]
  nextCursor: NextCursor | null
  hasMore: boolean
}

export interface CommentQueryParams {
  noteId: number
  pageSize?: number
  sort?: CommentSort
  cursorCreatedAt?: string
  cursorCommentId?: number
  cursorLikeCount?: number
  cursorReplyCount?: number
}

export interface ReplyQueryParams {
  rootCommentId: number
  page?: number
  pageSize?: number
  sort?: ReplySort
}

export interface CreateCommentRequest {
  noteId: number
  parentId?: number
  replyToUserId?: number
  content: string
}
