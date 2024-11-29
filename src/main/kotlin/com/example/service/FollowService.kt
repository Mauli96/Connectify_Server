package com.example.service

import com.example.data.repository.follow.FollowRepository
import com.example.data.repository.user.UserRepository
import com.example.data.requests.FollowUpdateRequest
import com.example.data.responses.UserResponseItem

class FollowService(
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository
) {

    suspend fun followUserIfExists(request: FollowUpdateRequest, followingUserId: String): Boolean {
        return followRepository.followUserIfExists(
            followingUserId,
            request.followedUserId
        )
    }

    suspend fun unfollowUserIfExists(followedUserId: String, followingUserId: String): Boolean {
        return followRepository.unfollowUserIfExists(
            followingUserId,
            followedUserId
        )
    }

    suspend fun getFollowsByUser(userId: String): List<UserResponseItem> {
        val followsByUser = followRepository.getFollowsByUser(userId)
        val followingIds = followsByUser.map { it.followedUserId }
        val users = userRepository.getUsers(followingIds)
        return users.map { user ->
            val isFollowing = followsByUser.find { it.followedUserId == user.id } != null
            UserResponseItem(
                userId = user.id,
                username = user.username,
                profilePictureUrl = user.profileImageUrl,
                bio = user.bio,
                isFollowing = isFollowing
            )
        }
    }

    suspend fun getFollowedToUser(userId: String): List<UserResponseItem> {
        val followsByUser = followRepository.getFollowsByUser(userId)
        val followedToUser = followRepository.getFollowedToUser(userId)
        val followerIds = followedToUser.map { it.followingUserId }
        val users = userRepository.getUsers(followerIds)
        return users.map { user ->
            val isFollowing = followsByUser.find { it.followedUserId == user.id } != null
            UserResponseItem(
                userId = user.id,
                username = user.username,
                profilePictureUrl = user.profileImageUrl,
                bio = user.bio,
                isFollowing = isFollowing
            )
        }
    }
}