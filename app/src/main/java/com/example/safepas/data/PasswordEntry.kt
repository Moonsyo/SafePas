package com.example.safepas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class PasswordEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val serviceName: String,
    val login: String,
    val password: String,
    val websiteUrl: String?,
    val isVisible: Boolean,
    val category: Category
)
