package com.example.routes

import com.google.gson.Gson
import com.example.data.requests.UpdateProfileRequest
import com.example.data.responses.BasicApiResponse
import com.example.data.responses.UserResponseItem
import com.example.service.UserService
import com.example.util.ApiResponseMessages.USER_NOT_FOUND
import com.example.util.Constants.BANNER_IMAGE_PATH
import com.example.util.Constants.BASE_URL
import com.example.util.Constants.PROFILE_PICTURE_PATH
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

fun Route.searchUser(userService: UserService) {
    authenticate {
        get("/api/user/search") {
            val query = call.parameters[QueryParams.PARAM_QUERY]
            if(query.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.OK,
                    listOf<UserResponseItem>()
                )
                return@get
            }
            val searchResults = userService.searchForUsers(query, call.userId)
            call.respond(
                HttpStatusCode.OK,
                searchResults
            )
        }
    }
}

fun Route.getUserProfile(userService: UserService) {
    authenticate {
        get("/api/user/profile") {
            val userId = call.parameters[QueryParams.PARAM_USER_ID]
            if(userId.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val profileResponse = userService.getUserProfile(userId, call.userId)
            if(profileResponse == null) {
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse<Unit>(
                        successful = false,
                        message = USER_NOT_FOUND
                    )
                )
                return@get
            }
            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse(
                    successful = true,
                    data = profileResponse
                )
            )
        }
    }
}

fun Route.updateUserProfile(userService: UserService) {
    val gson: Gson by inject()
    authenticate {
        put("/api/user/update") {
            val multipart = call.receiveMultipart()
            var updateProfileRequest: UpdateProfileRequest? = null
            var profilePictureFileName: String? = null
            var bannerImageFileName: String? = null
            multipart.forEachPart { partData ->
                when (partData) {
                    is PartData.FormItem -> {
                        if(partData.name == "update_profile_data") {
                            updateProfileRequest = gson.fromJson(
                                partData.value,
                                UpdateProfileRequest::class.java
                            )
                        }
                    }
                    is PartData.FileItem -> {
                        if(partData.name == "profile_picture") {
                            profilePictureFileName = partData.save(PROFILE_PICTURE_PATH)
                        } else if(partData.name == "banner_image") {
                            bannerImageFileName = partData.save(BANNER_IMAGE_PATH)
                        }
                    }
                    is PartData.BinaryItem -> Unit
                    is PartData.BinaryChannelItem -> Unit
                }
            }

            val profilePictureUrl = "${BASE_URL}profile_pictures/$profilePictureFileName"
            val bannerImageUrl = "${BASE_URL}banner_images/$bannerImageFileName"

            updateProfileRequest?.let { request ->
                val updateAcknowledged = userService.updateUser(
                    userId = call.userId,
                    profileImageUrl = if(profilePictureFileName == null) {
                        null
                    } else {
                        profilePictureUrl
                    },
                    bannerUrl = if(bannerImageFileName == null) {
                        null
                    } else {
                        bannerImageUrl
                    },
                    updateProfileRequest = request
                )
                if(updateAcknowledged) {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse<Unit>(
                            successful = true
                        )
                    )
                } else {
                    File("${PROFILE_PICTURE_PATH}/$profilePictureFileName").delete()
                    call.respond(HttpStatusCode.InternalServerError)
                }
            } ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
        }
    }
}

fun Route.getOwnProfilePicture(userService: UserService) {
    authenticate {
        get("/api/user/profile/picture") {
            val profilePictureUrl = userService.getOwnProfilePicture(call.userId)
            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse(
                    successful = true,
                    data = profilePictureUrl
                )
            )
        }
    }
}