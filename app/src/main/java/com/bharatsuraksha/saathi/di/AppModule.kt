package com.bharatsuraksha.saathi.di

import android.content.Context
import androidx.room.Room
import com.bharatsuraksha.saathi.data.db.EvidenceDao
import com.bharatsuraksha.saathi.data.db.HistoryDao
import com.bharatsuraksha.saathi.data.db.SaathiDatabase
import com.bharatsuraksha.saathi.data.rules.AssetRulesRepository
import com.bharatsuraksha.saathi.domain.engine.RulesRepository
import com.bharatsuraksha.saathi.domain.engine.ScamEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SaathiDatabase =
        Room.databaseBuilder(context, SaathiDatabase::class.java, "saathi.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideHistoryDao(db: SaathiDatabase): HistoryDao = db.historyDao()

    @Provides
    fun provideEvidenceDao(db: SaathiDatabase): EvidenceDao = db.evidenceDao()

    @Provides
    @Singleton
    fun provideRulesRepository(impl: AssetRulesRepository): RulesRepository = impl

    @Provides
    @Singleton
    fun provideScamEngine(rulesRepository: RulesRepository): ScamEngine =
        ScamEngine(rulesRepository.loadRules())
}
