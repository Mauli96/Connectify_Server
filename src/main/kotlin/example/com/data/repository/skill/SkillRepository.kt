package example.com.data.repository.skill

import example.com.data.models.Skill

interface SkillRepository {

    suspend fun getSkills(): List<Skill>
}