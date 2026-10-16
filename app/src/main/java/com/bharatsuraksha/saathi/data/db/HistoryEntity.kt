package com.bharatsuraksha.saathi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One saved check. Stays only on this device — never uploaded. */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val message: String,
    val score: Int,
    val verdict: String,
    val checkedAt: Long
)
