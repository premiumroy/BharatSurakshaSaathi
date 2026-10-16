package com.bharatsuraksha.saathi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One piece of scam evidence the user saved locally. Never uploaded. */
@Entity(tableName = "evidence")
data class EvidenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: String,
    val whenText: String,
    val numbers: String,
    val notes: String,
    val screenshotPath: String?,
    val createdAt: Long
)
