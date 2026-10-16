package com.bharatsuraksha.saathi.data.repository

import com.bharatsuraksha.saathi.data.db.EvidenceDao
import com.bharatsuraksha.saathi.data.db.EvidenceEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EvidenceRepository @Inject constructor(
    private val dao: EvidenceDao
) {
    fun observeAll(): Flow<List<EvidenceEntity>> = dao.observeAll()

    suspend fun add(item: EvidenceEntity): Long = dao.insert(item)

    suspend fun all(): List<EvidenceEntity> = dao.getAll()

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()
}
