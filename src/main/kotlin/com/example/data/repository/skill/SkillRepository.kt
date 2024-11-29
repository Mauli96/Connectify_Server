package com.example.data.repository.skill

import com.example.data.models.Skill

interface SkillRepository {

    suspend fun getSkills(): List<Skill>
}