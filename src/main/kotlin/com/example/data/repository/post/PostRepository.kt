package com.example.data.repository.post

import com.example.data.models.Post
import com.example.data.responses.PostResponse
import com.example.util.Constants

interface PostRepository {

    suspend fun createPost(post: Post): Boolean

    suspend fun deletePost(postId: String)

    suspend fun getPostsByFollows(
        ownUserId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_PAGE_SIZE
    ): List<PostResponse>

    suspend fun getPostsForProfile(
        ownUserId: String,
        userId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_PAGE_SIZE
    ): List<PostResponse>

    suspend fun getPost(postId: String): Post?

    suspend fun getPostDetails(
        ownUserId: String,
        postId: String
    ): PostResponse?

    suspend fun savePost(
        userId: String,
        postId: String
    ): Boolean

    suspend fun getSavedPosts(
        userId: String,
        page: Int = 0,
        pageSize: Int = Constants.DEFAULT_PAGE_SIZE
    ): List<PostResponse>

    suspend fun removeSavedPost(
        userId: String,
        postId: String
    )
}