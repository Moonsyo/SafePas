package com.example.safepas.viewModel

import com.example.safepas.data.Category
import com.example.safepas.data.PasswordEntry

data class PasswordUiState(
    val passwordList: List<PasswordEntry> = emptyList(),
    val serviceName: String = "",
    val login: String = "",
    val password: String = "",
    val isAddingNew: Boolean = false,
    val isEntryValid: Boolean = false,
    val websiteUrl: String = "",
    val category: Category = Category.SOCIAL,
    val isPasswordVisible: Boolean= false,
    val selectedCategory: Category?=null,
    val editingId: Int?=null
)

