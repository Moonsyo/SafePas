package com.example.safepas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.safepas.data.Category
import com.example.safepas.data.PasswordEntry
import com.example.safepas.viewModel.PasswordViewModel
import com.example.safepas.viewModel.PasswordUiState

@Composable
fun SafePas(paddingValues: PaddingValues) {
    val navController = rememberNavController()

    val viewModel: PasswordViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val application =
                    (this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SafeAppApplication)
                PasswordViewModel(application.repository)
            }
        }
    )

    NavHost(
        navController = navController,
        startDestination = SafePasScreen.HOME.name,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(route = SafePasScreen.HOME.name) {
            PasswordListScreen(viewModel = viewModel, navController = navController)
        }

        composable(route = SafePasScreen.ADD_PASSWORD.name) {
            val uiState by viewModel.uiState.collectAsState()
            AddPasswordScreen(
                uiState = uiState,
                onServiceNameChange = { viewModel.updateServiceName(it) },
                onLoginChange = { viewModel.updateLogin(it) },
                onPasswordChange = { viewModel.updatePassword(it) },
                onIsPasswordVisibleChange = { viewModel.updateIsPasswordVisible(it) },
                onWebsiteUrlChange = { viewModel.updateWebSiteUrl(it) },
                onCategoryChange = { viewModel.updateCategory(it) },
                onCancelClick = {
                    viewModel.cancelAdding()
                    navController.popBackStack()
                },
                onSaveClick = {
                    viewModel.savePassword()
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PasswordListScreen(viewModel: PasswordViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.cancelAdding()
                navController.navigate(SafePasScreen.ADD_PASSWORD.name)
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Лента фильтров
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = { viewModel.selectedCategory(null) },
                    label = { Text("All") }
                )
                Category.entries.forEach { category ->
                    FilterChip(
                        selected = category == uiState.selectedCategory,
                        onClick = { viewModel.selectedCategory(category) },
                        label = { Text(category.name) }
                    )
                }
            }

            // Список карточек
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.passwordList) { entry ->
                    PasswordCard(
                        entry = entry,
                        onCopyClick = {
                            clipboardManager.setText(AnnotatedString(entry.password))
                        },
                        onToggleVisibilityClick = {
                            viewModel.toggleVisibility(entry)
                        },
                        onEditClick = {
                            viewModel.editPassword(entry)
                            navController.navigate(SafePasScreen.ADD_PASSWORD.name)
                        },
                        onDeleteClick = {
                            viewModel.deletePassword(entry)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPasswordScreen(
    uiState: PasswordUiState,
    onServiceNameChange: (String) -> Unit,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onIsPasswordVisibleChange: (Boolean) -> Unit,
    onWebsiteUrlChange: (String) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (uiState.editingId != null) "Edit Password" else "Add new Password",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = uiState.serviceName,
            onValueChange = onServiceNameChange,
            label = { Text("Service Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )

        OutlinedTextField(
            value = uiState.login,
            onValueChange = onLoginChange,
            label = { Text("Login / Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { onIsPasswordVisibleChange(!uiState.isPasswordVisible) }) {
                    Icon(
                        imageVector = if (uiState.isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle Visibility"
                    )
                }
            }
        )

        OutlinedTextField(
            value = uiState.websiteUrl,
            onValueChange = onWebsiteUrlChange,
            label = { Text("Website URL (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )

        Text("Category", style = MaterialTheme.typography.labelLarge)

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Category.entries.forEach { category ->
                FilterChip(
                    selected = (category == uiState.category),
                    onClick = { onCategoryChange(category) },
                    label = { Text(category.name) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onCancelClick
            ) {
                Text("Cancel")
            }

            Button(
                modifier = Modifier.weight(1f),
                enabled = uiState.isEntryValid,
                onClick = onSaveClick
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun PasswordCard(
    entry: PasswordEntry,
    onCopyClick: () -> Unit,
    onToggleVisibilityClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onEditClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = getCategoryColor(entry.category),
                        shape = RoundedCornerShape(8.dp),
                    )
            ) {
                Icon(
                    imageVector = getCategoryIcon(entry.category),
                    contentDescription = null,
                    tint = Color.White,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = entry.serviceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Login: ${entry.login}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (entry.isVisible) entry.password else "••••••••",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row {
                IconButton(onClick = onCopyClick) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                }
                IconButton(onClick = onToggleVisibilityClick) {
                    Icon(
                        imageVector = if (entry.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Visibility"
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

fun getCategoryIcon(category: Category): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        Category.SOCIAL -> Icons.Default.Person
        Category.BANKING -> Icons.Default.AccountBalance
        Category.WORK -> Icons.Default.Work
        Category.FUN -> Icons.Default.SentimentSatisfied
        Category.GAMES -> Icons.Default.Gamepad
    }
}

fun getCategoryColor(category: Category): Color {
    return when (category) {
        Category.SOCIAL -> Color(0xFF4CAF50)
        Category.BANKING -> Color(0xFFF44336)
        Category.WORK -> Color(0xFF2196F3)
        Category.FUN -> Color(0xFF00796B)
        Category.GAMES -> Color(0xFF9C27B0)
    }
}
