package com.example.routes

import com.example.service.SkillService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getSkills(skillService: SkillService) {
    authenticate {
        get("/api/skills/get") {
            call.respond(
                HttpStatusCode.OK,
                skillService.getSkills().map { it.toSkillDto() }
            )
        }
    }
}