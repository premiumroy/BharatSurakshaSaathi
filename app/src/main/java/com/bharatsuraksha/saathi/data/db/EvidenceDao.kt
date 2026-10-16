package com.bharatsuraksha.saathi.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceDao {

    @Insert
    suspend fun insert(item: EvidenceEntity): Long

    @Query("SELECT * FROM evidence ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<EvidenceEntity>>

    @Query("SELECT * FROM evidence ORDER BY createdAt DESC")
    suspend fun getAll(): List<EvidenceEntity>

    @Query("DELETE FROM evidence WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM evidence")
    suspend fun deleteAll()
}
