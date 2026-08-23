package com.pablo.paper

import android.app.Application
import com.pablo.paper.data.db.PaperDatabase
import com.pablo.paper.data.repository.AnnotationRepository
import com.pablo.paper.data.repository.AnnotationRepositoryImpl
import com.pablo.paper.data.repository.DocumentRepository
import com.pablo.paper.data.repository.DocumentRepositoryImpl
import com.pablo.paper.data.repository.PreferencesRepository

class PaperApplication : Application() {

    lateinit var database: PaperDatabase
        private set

    lateinit var documentRepository: DocumentRepository
        private set

    lateinit var annotationRepository: AnnotationRepository
        private set

    lateinit var preferencesRepository: PreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = PaperDatabase.getInstance(this)
        documentRepository = DocumentRepositoryImpl(this, database.documentDao())
        annotationRepository = AnnotationRepositoryImpl(database.annotationDao())
        preferencesRepository = PreferencesRepository(this)
    }
}
