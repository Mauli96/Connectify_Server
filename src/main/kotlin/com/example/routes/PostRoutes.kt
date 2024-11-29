package com.example.routes

import com.google.gson.Gson
import com.example.data.requests.CreatePostRequest
import com.example.data.responses.BasicApiResponse
import com.example.service.CommentService
import com.example.service.LikeService
import com.example.service.PostService
import com.example.util.ApiResponseMessages
import com.example.util.ApiResponseMessages.POST_NOT_FOUND
import com.example.util.Constants
import com.example.util.QueryParams
import com.example.util.save
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.io.File

fun Route.createPost(
    postService: PostService,
) {
    val gson: Gson by inject()
    authenticate {
        post("/api/post/create") {
            val multipart = call.receiveMultipart()
            var createPostRequest: CreatePostRequest? = null
            var fileName: String? = null
            multipart.forEachPart { partData ->
                when (partData) {
                    is PartData.FormItem -> {
                        if (partData.name == "post_data") {
                            createPostRequest = gson.fromJson(
                                partData.value,
                                CreatePostRequest::class.java
                            )
                        }
                    }
                    is PartData.FileItem -> {
                        fileName = partData.save(Constants.POST_PICTURE_PATH)
                    }
                    is PartData.BinaryItem -> Unit
                    is PartData.BinaryChannelItem -> Unit
                }
            }

            val postPictureUrl = "${Constants.BASE_URL}post_pictures/$fileName"

            createPostRequest?.let { request ->
                val createPostAcknowledged = postService.createPost(
                    request = request,
                    userId = call.userId,
                    imageUrl = postPictureUrl
                )
                if (createPostAcknowledged) {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse<Unit>(
                            successful = true
                        )
                    )
                } else {
                    File("${Constants.POST_PICTURE_PATH}/$fileName").delete()
                    call.respond(HttpStatusCode.InternalServerError)
                }
            } ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
        }
    }
}

fun Route.getPostsForProfile(
    postService: PostService,
) {
    authenticate {
        get("/api/user/posts") {
            val userId = call.parameters[QueryParams.PARAM_USER_ID]
            val page = call.parameters[QueryParams.PARAM_PAGE]?.toIntOrNull() ?: 0
            val pageSize =
                call.parameters[QueryParams.PARAM_PAGE_SIZE]?.toIntOrNull() ?: Constants.DEFAULT_PAGE_SIZE

            val posts = postService.getPostsForProfile(
                ownUserId = call.userId,
                userId = userId ?: call.userId,
                page = page,
                pageSize = pageSize
            )
            call.respond(
                HttpStatusCode.OK,
                posts
            )
        }
    }
}

fun Route.getPostsForFollows(
    postService: PostService,
) {
    authenticate {
        get("/api/post/get") {
            val page = call.parameters[QueryParams.PARAM_PAGE]?.toIntOrNull() ?: 0
            val pageSize =
                call.parameters[QueryParams.PARAM_PAGE_SIZE]?.toIntOrNull() ?: Constants.DEFAULT_PAGE_SIZE

            val posts = postService.getPostsForFollows(call.userId, page, pageSize)
            call.respond(
                HttpStatusCode.OK,
                posts
            )
        }
    }
}

fun Route.deletePost(
    postService: PostService,
    likeService: LikeService,
    commentService: CommentService
) {
    authenticate {
        delete("/api/post/delete") {
            val postId = call.parameters[QueryParams.PARAM_POST_ID] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            val post = postService.getPost(postId)
            if(post == null) {
                call.respond(HttpStatusCode.NotFound)
                return@delete
            }
            if(post.userId == call.userId) {
                postService.deletePost(postId)
                likeService.deleteLikesForParent(postId)
                commentService.deleteCommentsForPost(postId)
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}

fun Route.getPostDetails(postService: PostService) {
    authenticate {
        get("/api/post/details") {
            val postId = call.parameters[QueryParams.PARAM_POST_ID] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val post = postService.getPostDetails(call.userId, postId) ?: kotlin.run {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse(
                    successful = true,
                    data = post
                )
            )
        }
    }
}

fun Route.savePost(postService: PostService) {
    authenticate {
        post("/api/post/save") {
            val postId = call.parameters[QueryParams.PARAM_POST_ID] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            postService.savePost(
                userId = call.userId,
                postId = postId
            )
            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse<Unit>(
                    successful = true
                )
            )
        }
    }
}

fun Route.getSavedPosts(postService: PostService) {
    authenticate {
        get("/api/post/save/get") {
            val page = call.parameters[QueryParams.PARAM_PAGE]?.toIntOrNull() ?: 0
            val pageSize =
                call.parameters[QueryParams.PARAM_PAGE_SIZE]?.toIntOrNull() ?: Constants.DEFAULT_PAGE_SIZE

            val posts = postService.getSavedPosts(call.userId, page, pageSize)
            call.respond(
                HttpStatusCode.OK,
                posts
            )
        }
    }
}

fun Route.removeSavedPost(postService: PostService) {
    authenticate {
        delete("/api/post/unsave") {
            val postId = call.parameters[QueryParams.PARAM_POST_ID] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            postService.removeSavedPost(call.userId, postId)
            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse<Unit>(
                    successful = true
                )
            )
        }
    }
}

fun Route.downloadPost(postService: PostService) {
    authenticate {
        get("/api/post/download") {
            val postId = call.parameters[QueryParams.PARAM_POST_ID] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val postImageUrl = postService.getPost(postId)?.imageUrl
            if (postImageUrl != null) {
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse(
                        successful = true,
                        data = postImageUrl
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    message = POST_NOT_FOUND
                )
            }
        }
    }
}