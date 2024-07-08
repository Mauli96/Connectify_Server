package example.com.service

import example.com.data.models.Skill
import example.com.data.repository.skill.SkillRepository

class SkillService(
    private val repository: SkillRepository
) {

    suspend fun getSkills(): List<Skill> {
        return repository.getSkills()
    }
}