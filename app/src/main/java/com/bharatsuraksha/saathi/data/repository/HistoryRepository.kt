package com.bharatsuraksha.saathi.data.repository

import com.bharatsuraksha.saathi.data.db.HistoryDao
import com.bharatsuraksha.saathi.data.db.HistoryEntity
import com.bharatsuraksha.saathi.domain.model.AnalysisResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val dao: HistoryDao
) {
    fun observeAll(): Flow<List<HistoryEntity>> = dao.observeAll()

    suspend fun add(message: String, result: AnalysisResult) {
        dao.insert(
            HistoryEntity(
                message = message,
                score = result.score,
                verdict = result.verdict.name,
                checkedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
