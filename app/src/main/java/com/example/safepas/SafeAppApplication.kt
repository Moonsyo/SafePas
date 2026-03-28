package com.example.safepas

import android.app.Application
import com.example.safepas.data.AppDatabase
import com.example.safepas.data.PasswordRepository

class SafeAppApplication: Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: PasswordRepository by lazy { PasswordRepository (database.passwordDao()) }
}
