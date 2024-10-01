package example.com.data.repository.comment

import example.com.data.models.Comment
import example.com.data.responses.CommentResponse
import example.com.util.Constants

interface CommentRepository {

    suspend fun createComment(comment: Comment): String

    suspend fun getComment(commentId: String): Comment?

    suspend fun deleteComment(commentId: String): Boolean

    suspend fun deleteCommentsFromPost(postId: String): Boolean

    suspend fun getCommentsByMostRecent(
        postId: String,
        ownUserId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_PAGE_SIZE
    ): List<CommentResponse>

    suspend fun getCommentsByMostOld(
        postId: String,
        ownUserId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_PAGE_SIZE
    ): List<CommentResponse>

    suspend fun getCommentsByMostPopular(
        postId: String,
        ownUserId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_PAGE_SIZE
    ): List<CommentResponse>
}