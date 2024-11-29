package com.example.service

import com.example.data.models.Activity
import com.example.data.repository.activity.ActivityRepository
import com.example.data.repository.comment.CommentRepository
import com.example.data.repository.post.PostRepository
import com.example.data.repository.user.UserRepository
import com.example.data.responses.ActivityResponse
import com.example.data.util.ActivityType
import com.example.data.util.ParentType
import com.example.util.Constants

class ActivityService(
    private val activityRepository: ActivityRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) {

    suspend fun getActivitiesForUser(
        userId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_PAGE_SIZE
    ): List<ActivityResponse> {
        return activityRepository.getActivitiesForUser(userId, page, pageSize)
    }

    suspend fun addCommentActivity(
        byUserId: String,
        postId: String
    ): Boolean {
        val userIdOfPost = postRepository.getPost(postId)?.userId ?: return false
        if(byUserId == userIdOfPost) {
            return false
        }
        val user = userRepository.getUserById(byUserId) ?: return false
        activityRepository.createActivity(
            Activity(
                timestamp = System.currentTimeMillis(),
                byUserId = byUserId,
                toUserId = userIdOfPost,
                username = user.username,
                profilePictureUrl = user.profileImageUrl,
                type = ActivityType.CommentedOnPost.type,
                parentId = postId
            )
        )
        return true
    }

    suspend fun addLikeActivity(
        byUserId: String,
        parentType: ParentType,
        parentId: String
    ): Boolean {
        val toUserId = when (parentType) {
            is ParentType.Post -> {
                postRepository.getPost(parentId)?.userId
            }
            is ParentType.Comment -> {
                commentRepository.getComment(parentId)?.userId
            }
            is ParentType.None -> return false
        } ?: return false
        if(byUserId == toUserId) {
            return false
        }
        val user = userRepository.getUserById(byUserId) ?: return false
        activityRepository.createActivity(
            Activity(
                timestamp = System.currentTimeMillis(),
                byUserId = byUserId,
                toUserId = toUserId,
                username = user.username,
                profilePictureUrl = user.profileImageUrl,
                type = when(parentType) {
                    is ParentType.Post -> ActivityType.LikedPost.type
                    is ParentType.Comment -> ActivityType.LikedComment.type
                    else -> ActivityType.LikedPost.type
                },
                parentId = parentId
            )
        )
        return true
    }

    suspend fun createActivity(activity: Activity) {
        activityRepository.createActivity(activity)
    }

    suspend fun deleteActivity(activityId: String): Boolean {
        return activityRepository.deleteActivity(activityId)
    }
}