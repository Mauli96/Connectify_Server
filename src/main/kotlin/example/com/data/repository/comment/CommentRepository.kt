package example.com.data.repository.comment

import example.com.data.models.Comment
import example.com.data.repository.comment.model.CommentWithLikeStatus
import example.com.data.responses.CommentResponse

interface CommentRepository {

    suspend fun createComment(comment: Comment): String

    suspend fun deleteComment(commentId: String): Boolean

    suspend fun deleteCommentsFromPost(postId: String): Boolean

    suspend fun getCommentsWithStatus(postId: String, ownUserId: String): List<CommentWithLikeStatus>

    suspend fun getCommentsByMostRecent(postId: String, ownUserId: String): List<CommentResponse>

    suspend fun getCommentsByMostOld(postId: String, ownUserId: String): List<CommentResponse>

    suspend fun getCommentsByMostPopular(postId: String, ownUserId: String): List<CommentResponse>

    suspend fun getComment(commentId: String): Comment?
}