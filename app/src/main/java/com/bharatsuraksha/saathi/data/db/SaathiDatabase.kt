package com.bharatsuraksha.saathi.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HistoryEntity::class, EvidenceEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SaathiDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun evidenceDao(): EvidenceDao
}
