package com.example.data.repository.skill

import com.example.data.models.Skill
import com.example.data.repository.skill.SkillRepository
import org.litote.kmongo.coroutine.CoroutineDatabase

class SkillRepositoryImpl(
    db: CoroutineDatabase
): SkillRepository {

    private val skills = db.getCollection<Skill>()

    override suspend fun getSkills(): List<Skill> {
        return skills.find().toList()
    }
}