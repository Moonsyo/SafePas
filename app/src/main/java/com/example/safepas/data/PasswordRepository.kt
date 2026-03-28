package com.example.safepas.data

import kotlinx.coroutines.flow.Flow

class PasswordRepository (private val passwordDao: PasswordDao) {
    val allPasswords: Flow<List<PasswordEntry>> = passwordDao.getAllPasswords()

    suspend fun insert (password: PasswordEntry) {
        passwordDao.insertPassword(password)
    }

    suspend fun delete (password: PasswordEntry) {
        passwordDao.deletePassword(password)
    }

}