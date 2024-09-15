package example.com.data.repository.comment

import example.com.data.models.Comment
import example.com.data.models.Like
import example.com.data.models.Post
import example.com.data.repository.comment.model.CommentWithLikeStatus
import example.com.data.responses.CommentResponse
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.inc
import org.litote.kmongo.setValue

class CommentRepositoryImpl(
    db: CoroutineDatabase
) : CommentRepository {

    private val posts = db.getCollection<Post>()
    private val comments = db.getCollection<Comment>()
    private val likes = db.getCollection<Like>()

    override suspend fun createComment(comment: Comment): String {
        comments.insertOne(comment)
        val oldCommentCount = posts.findOneById(comment.postId)?.commentCount ?: 0
        posts.updateOneById(
            comment.postId,
            setValue(Post::commentCount, oldCommentCount + 1)
        )
        return comment.id
    }

    override suspend fun deleteComment(commentId: String): Boolean {
        comments.findOneById(commentId)?.also {
            posts.updateOneById(
                it.postId,
                inc(Post::commentCount, -1)
            )
        }
        val deleteCount = comments.deleteOneById(commentId).deletedCount
        return deleteCount > 0
    }

    override suspend fun deleteCommentsFromPost(postId: String): Boolean {
        return comments.deleteMany(
            Comment::postId eq postId
        ).wasAcknowledged()
    }

    override suspend fun getCommentsWithStatus(postId: String, ownUserId: String): List<CommentWithLikeStatus> {
        return comments.find(Comment::postId eq postId).toList().map { comment ->
            val isLiked = likes.findOne(
                and(
                    Like::userId eq ownUserId,
                    Like::parentId eq comment.id
                )
            ) != null
            CommentWithLikeStatus(
                comment = comment,
                isLiked = isLiked,
                isOwnComment = ownUserId == comment.userId
            )
        }
    }

    override suspend fun getCommentsByMostRecent(postId: String, ownUserId: String): List<CommentResponse> {
        val comments = getCommentsWithStatus(postId, ownUserId)
        return comments.map { commentWithStatus ->
            CommentResponse(
                id = commentWithStatus.comment.id,
                username = commentWithStatus.comment.username,
                profilePictureUrl = commentWithStatus.comment.profileImageUrl,
                timestamp = commentWithStatus.comment.timestamp,
                comment = commentWithStatus.comment.comment,
                isLiked = commentWithStatus.isLiked,
                likeCount = commentWithStatus.comment.likeCount,
                isOwnComment = commentWithStatus.isOwnComment
            )
        }.sortedByDescending { it.timestamp }
    }

    override suspend fun getCommentsByMostOld(postId: String, ownUserId: String): List<CommentResponse> {
        val comments = getCommentsWithStatus(postId, ownUserId)
        return comments.map { commentWithStatus ->
            CommentResponse(
                id = commentWithStatus.comment.id,
                username = commentWithStatus.comment.username,
                profilePictureUrl = commentWithStatus.comment.profileImageUrl,
                timestamp = commentWithStatus.comment.timestamp,
                comment = commentWithStatus.comment.comment,
                isLiked = commentWithStatus.isLiked,
                likeCount = commentWithStatus.comment.likeCount,
                isOwnComment = commentWithStatus.isOwnComment
            )
        }.sortedBy { it.timestamp }
    }

    override suspend fun getCommentsByMostPopular(postId: String, ownUserId: String): List<CommentResponse> {
        val comments = getCommentsWithStatus(postId, ownUserId)
        return comments.map { commentWithStatus ->
            CommentResponse(
                id = commentWithStatus.comment.id,
                username = commentWithStatus.comment.username,
                profilePictureUrl = commentWithStatus.comment.profileImageUrl,
                timestamp = commentWithStatus.comment.timestamp,
                comment = commentWithStatus.comment.comment,
                isLiked = commentWithStatus.isLiked,
                likeCount = commentWithStatus.comment.likeCount,
                isOwnComment = commentWithStatus.isOwnComment
            )
        }.sortedByDescending { it.likeCount }
    }


    override suspend fun getComment(commentId: String): Comment? {
        return comments.findOneById(commentId)
    }
}