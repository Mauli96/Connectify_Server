package com.example.service

import com.example.data.models.Comment
import com.example.data.repository.comment.CommentRepository
import com.example.data.repository.user.UserRepository
import com.example.data.requests.CreateCommentRequest
import com.example.data.responses.CommentResponse
import com.example.util.CommentFilter
import com.example.util.Constants

class CommentService(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) {

    suspend fun createComment(
        createCommentRequest: CreateCommentRequest,
        userId: String
    ): ValidationEvent {
        createCommentRequest.apply {
            if(comment.isBlank() || postId.isBlank()) {
                return ValidationEvent.ErrorFieldEmpty
            }
            if(comment.length > Constants.MAX_COMMENT_LENGTH) {
                return ValidationEvent.ErrorCommentTooLong
            }
        }
        val user = userRepository.getUserById(userId) ?: return ValidationEvent.UserNotFound
        commentRepository.createComment(
            Comment(
                username = user.username,
                profileImageUrl = user.profileImageUrl,
                likeCount = 0,
                comment = createCommentRequest.comment,
                userId = userId,
                postId = createCommentRequest.postId,
                timestamp = System.currentTimeMillis()
            )
        )
        return ValidationEvent.Success
    }

    suspend fun deleteCommentsForPost(postId: String) {
        commentRepository.deleteCommentsFromPost(postId)
    }

    suspend fun deleteComment(commentId: String): Boolean {
        return commentRepository.deleteComment(commentId)
    }

    suspend fun getCommentsForPost(
        postId: String,
        ownUserId: String,
        filterType: CommentFilter,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_PAGE_SIZE
    ): List<CommentResponse> {
        return when(filterType) {
            CommentFilter.MOST_RECENT -> commentRepository.getCommentsByMostRecent(postId, ownUserId, page, pageSize)
            CommentFilter.MOST_OLD -> commentRepository.getCommentsByMostOld(postId, ownUserId, page, pageSize)
            CommentFilter.MOST_POPULAR -> commentRepository.getCommentsByMostPopular(postId, ownUserId, page, pageSize)
        }
    }

    suspend fun getCommentById(commentId: String): Comment? {
        return commentRepository.getComment(commentId)
    }

    sealed class ValidationEvent {
        object ErrorFieldEmpty : ValidationEvent()
        object ErrorCommentTooLong : ValidationEvent()
        object UserNotFound: ValidationEvent()
        object Success : ValidationEvent()
    }
}