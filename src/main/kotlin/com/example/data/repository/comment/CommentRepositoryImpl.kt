package com.example.data.repository.comment

import com.mongodb.client.model.Sorts
import com.example.data.models.Comment
import com.example.data.models.Like
import com.example.data.models.Post
import com.example.data.responses.CommentResponse
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

    override suspend fun getComment(commentId: String): Comment? {
        return comments.findOneById(commentId)
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

    override suspend fun getCommentsByMostRecent(
        postId: String,
        ownUserId: String,
        page: Int,
        pageSize: Int
    ): List<CommentResponse> {
        val commentsCursor = comments.find(Comment::postId eq postId)
            .sort(Sorts.descending("timestamp"))
            .skip(page * pageSize)
            .limit(pageSize)
            .toList()
        return commentsCursor.map { comment ->
            val isLiked = likes.findOne(
                and(
                    Like::userId eq ownUserId,
                    Like::parentId eq comment.id
                )
            ) != null
            CommentResponse(
                id = comment.id,
                username = comment.username,
                profilePictureUrl = comment.profileImageUrl,
                timestamp = comment.timestamp,
                comment = comment.comment,
                isLiked = isLiked,
                likeCount = comment.likeCount,
                isOwnComment = ownUserId == comment.userId
            )
        }
    }

    override suspend fun getCommentsByMostOld(
        postId: String,
        ownUserId: String,
        page: Int,
        pageSize: Int
    ): List<CommentResponse> {
        val commentsCursor = comments.find(Comment::postId eq postId)
            .sort(Sorts.ascending("timestamp"))
            .skip(page * pageSize)
            .limit(pageSize)
            .toList()
        return commentsCursor.map { comment ->
            val isLiked = likes.findOne(
                and(
                    Like::userId eq ownUserId,
                    Like::parentId eq comment.id
                )
            ) != null
            CommentResponse(
                id = comment.id,
                username = comment.username,
                profilePictureUrl = comment.profileImageUrl,
                timestamp = comment.timestamp,
                comment = comment.comment,
                isLiked = isLiked,
                likeCount = comment.likeCount,
                isOwnComment = ownUserId == comment.userId
            )
        }
    }

    override suspend fun getCommentsByMostPopular(
        postId: String,
        ownUserId: String,
        page: Int,
        pageSize: Int
    ): List<CommentResponse> {
        val commentsCursor = comments.find(Comment::postId eq postId)
            .sort(Sorts.descending("likeCount"))
            .skip(page * pageSize)
            .limit(pageSize)
            .toList()
        return commentsCursor.map { comment ->
            val isLiked = likes.findOne(
                and(
                    Like::userId eq ownUserId,
                    Like::parentId eq comment.id
                )
            ) != null
            CommentResponse(
                id = comment.id,
                username = comment.username,
                profilePictureUrl = comment.profileImageUrl,
                timestamp = comment.timestamp,
                comment = comment.comment,
                isLiked = isLiked,
                likeCount = comment.likeCount,
                isOwnComment = ownUserId == comment.userId
            )
        }
    }
}