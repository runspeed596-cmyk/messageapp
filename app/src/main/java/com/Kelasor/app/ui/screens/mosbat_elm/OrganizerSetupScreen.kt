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
import com.Kelasor.app.ui.theme.DanaFontFamily
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
    val initialManagerName = currentUser?.firstName ?: ""
    val initialNationalCode = currentUser?.nationalCode ?: ""
    
    var managerName by remember(initialManagerName) { mutableStateOf(initialManagerName) }
    var nationalCode by remember(initialNationalCode) { mutableStateOf(initialNationalCode) }
    
    val needsManagerName = initialManagerName.isBlank()
    val needsNationalCode = initialNationalCode.isBlank()
    
    val isFormComplete = managerName.isNotBlank() && nationalCode.isNotBlank() && nationalCode.length == 10

    // If completely filled already from backend perspective, normally we'd bypass. 
    // But maybe we want them to review. We'll show the form if anything is missing.
    // Assuming if nothing is missing, we wouldn't show this or we'd just have a "Continue" button.
    
    LaunchedEffect(needsManagerName, needsNationalCode) {
        if (!needsManagerName && !needsNationalCode) {
            // Already has everything? The caller logic in the navigation might bypass this,
            // but if we are here, we can just allow them to proceed.
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ثبت به عنوان برگزارکننده", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold) },
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
                fontFamily = DanaFontFamily,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (needsManagerName) {
                OutlinedTextField(
                    value = managerName,
                    onValueChange = { managerName = it },
                    label = { Text("نام و نام خانوادگی مدیر آکادمی", fontFamily = DanaFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                // Readonly display of name
                OutlinedTextField(
                    value = initialManagerName,
                    onValueChange = { },
                    label = { Text("نام و نام خانوادگی مدیر آکادمی", fontFamily = DanaFontFamily) },
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
                    label = { Text("کد ملی", fontFamily = DanaFontFamily) },
                    placeholder = { Text("کد ملی ۱۰ رقمی خود را وارد کنید", fontFamily = DanaFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
            } else {
                OutlinedTextField(
                    value = initialNationalCode,
                    onValueChange = { },
                    label = { Text("کد ملی", fontFamily = DanaFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "تایید و ادامه",
                onClick = {
                    if (needsManagerName || needsNationalCode) {
                        viewModel.updateProfile(
                            username = currentUser?.username ?: "",
                            displayName = managerName,
                            firstName = managerName,
                            lastName = "",
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
