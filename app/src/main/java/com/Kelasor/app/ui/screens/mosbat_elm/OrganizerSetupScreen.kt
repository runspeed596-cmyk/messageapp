package com.Kelasor.app.ui.screens.mosbat_elm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.components.PrimaryButton
import com.Kelasor.app.ui.theme.VazirFontFamily
import com.Kelasor.app.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerSetupScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAcademyProfileSetup: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUser = state.user
    
    // Check what is missing
    val initialFirstName = currentUser?.firstName ?: ""
    val initialLastName = currentUser?.lastName ?: ""
    val initialNationalCode = currentUser?.nationalCode ?: ""
    
    var firstName by remember(initialFirstName) { mutableStateOf(initialFirstName) }
    var lastName by remember(initialLastName) { mutableStateOf(initialLastName) }
    var nationalCode by remember(initialNationalCode) { mutableStateOf(initialNationalCode) }
    
    val needsFirstName = initialFirstName.isBlank()
    val needsLastName = initialLastName.isBlank()
    val needsNationalCode = initialNationalCode.isBlank()
    
    val isFormComplete = firstName.isNotBlank() && lastName.isNotBlank() && nationalCode.isNotBlank() && nationalCode.length == 10

    // If completely filled already from backend perspective, normally we'd bypass. 
    // But maybe we want them to review. We'll show the form if anything is missing.
    // Assuming if nothing is missing, we wouldn't show this or we'd just have a "Continue" button.
    
    LaunchedEffect(needsFirstName, needsLastName, needsNationalCode) {
        if (!needsFirstName && !needsLastName && !needsNationalCode) {
            // Already has everything? The caller logic in the navigation might bypass this,
            // but if we are here, we can just allow them to proceed.
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ثبت به عنوان برگزارکننده", fontFamily = VazirFontFamily, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                "برای ایجاد دوره، لطفا اطلاعات هویتی زیر را تکمیل کنید:",
                fontFamily = VazirFontFamily,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (needsFirstName || needsLastName) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("نام", fontFamily = VazirFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("نام خانوادگی", fontFamily = VazirFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                // Readonly display of name
                OutlinedTextField(
                    value = "$initialFirstName $initialLastName",
                    onValueChange = { },
                    label = { Text("نام و نام خانوادگی", fontFamily = VazirFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (needsNationalCode) {
                OutlinedTextField(
                    value = nationalCode,
                    onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) nationalCode = it },
                    label = { Text("کد ملی", fontFamily = VazirFontFamily) },
                    placeholder = { Text("کد ملی ۱۰ رقمی خود را وارد کنید", fontFamily = VazirFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
            } else {
                OutlinedTextField(
                    value = initialNationalCode,
                    onValueChange = { },
                    label = { Text("کد ملی", fontFamily = VazirFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "تایید و ادامه",
                onClick = {
                    if (needsFirstName || needsLastName || needsNationalCode) {
                        viewModel.updateProfile(
                            username = currentUser?.username ?: "",
                            displayName = "$firstName $lastName",
                            firstName = firstName,
                            lastName = lastName,
                            nationalCode = nationalCode,
                            bio = currentUser?.bio
                        )
                    }
                    onNavigateToAcademyProfileSetup()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormComplete,
                isLoading = state.isLoading
            )
        }
    }
}
