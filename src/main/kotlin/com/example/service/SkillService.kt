package com.example.service

import com.example.data.models.Skill
import com.example.data.repository.skill.SkillRepository

class SkillService(
    private val repository: SkillRepository
) {

    suspend fun getSkills(): List<Skill> {
        return repository.getSkills()
    }
}