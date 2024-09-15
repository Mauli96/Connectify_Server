package example.com.data.repository.comment.model

import example.com.data.models.Comment

data class CommentWithLikeStatus(
    val comment: Comment,
    val isLiked: Boolean,
    val isOwnComment: Boolean
)