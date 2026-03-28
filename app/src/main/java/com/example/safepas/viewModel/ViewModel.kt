package com.example.safepas.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safepas.data.Category
import com.example.safepas.data.PasswordEntry
import com.example.safepas.data.PasswordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PasswordViewModel(private val repository: PasswordRepository) : ViewModel() {
    
    // Источник данных для полей ввода (черновик)
    private val _uiState = MutableStateFlow(PasswordUiState())
    
    // Итоговое состояние: объединяем черновик и список из базы данных
    val uiState: StateFlow<PasswordUiState> = combine(
        _uiState,
        repository.allPasswords
    ) { state, list ->
        val filteredList = if (state.selectedCategory == null) {list} else {list.filter { it.category == state.selectedCategory }}
        state.copy(passwordList = filteredList)

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PasswordUiState()
    )

    fun updateServiceName(serviceName: String) {
        _uiState.update { 
            val newState = it.copy(serviceName = serviceName)
            newState.copy(isEntryValid = validateEntry(newState))
        }
    }

    fun updateLogin(login: String) {
        _uiState.update { 
            val newState = it.copy(login = login)
            newState.copy(isEntryValid = validateEntry(newState))
        }
    }

    fun updatePassword(password: String) {
        _uiState.update {
            val newState = it.copy(password = password)
            newState.copy(isEntryValid = validateEntry(newState))
        }
    }

    private fun validateEntry(state: PasswordUiState): Boolean {
        return state.serviceName.isNotBlank() && 
               state.login.isNotBlank() && 
               state.password.isNotBlank()
    }

    fun updateWebSiteUrl(websiteUrl: String) {
        _uiState.update { it.copy(websiteUrl = websiteUrl) }
    }

    fun updateCategory(category: Category) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateIsAddingNew(isAddingNew: Boolean) {
        _uiState.update { it.copy(isAddingNew=isAddingNew) }
    }

    fun updateIsPasswordVisible(isPasswordVisible: Boolean) {
        _uiState.update { it.copy(isPasswordVisible=isPasswordVisible) }
    }

    fun savePassword() {
        val state = _uiState.value
        
        if (!validateEntry(state)) return

        val newPassword = PasswordEntry(
            serviceName = state.serviceName,
            login = state.login,
            password = state.password,
            websiteUrl = state.websiteUrl,
            isVisible = state.isPasswordVisible,
            category = state.category,
            id = state.editingId ?: 0
        )
        
        viewModelScope.launch {
            repository.insert(newPassword)
            resetFields()
        }
    }

    fun toggleVisibility(entry: PasswordEntry) {
        viewModelScope.launch {
            repository.insert(entry.copy(isVisible = !entry.isVisible))
        }
    }

    fun deletePassword (password: PasswordEntry) {
        viewModelScope.launch {
            repository.delete(password)
        }
    }

    fun cancelAdding() {
        resetFields()
    }


    fun selectedCategory (category: Category?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    private fun resetFields() {
       _uiState.update {
               it.copy(
                   serviceName = "",
                   login = "",
                   password = "",
                   websiteUrl = "",
                   category = Category.SOCIAL,
                   isAddingNew = false,
                   isPasswordVisible = false,
                   isEntryValid = false,
                   editingId = null
               )
       }
    }

    fun editPassword(password: PasswordEntry) {
        _uiState.update { it.copy(
            serviceName = password.serviceName,
            login = password.login,
            password = password.password,
            websiteUrl = password.websiteUrl ?: "",
            category = password.category,
            isPasswordVisible = password.isVisible,
            editingId = password.id,
            isEntryValid = true
        ) }
    }


}
