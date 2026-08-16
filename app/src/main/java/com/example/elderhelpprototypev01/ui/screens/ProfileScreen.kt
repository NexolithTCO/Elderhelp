package com.example.elderhelpprototypev01.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.example.elderhelpprototypev01.profile.BasicProfile
import com.example.elderhelpprototypev01.profile.ProfileRepository
import com.example.elderhelpprototypev01.ui.theme.SahaaySpacing
import com.example.elderhelpprototypev01.ui.theme.SahaayCorners
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        ProfileRepository.init(context)
    }

    val profileState by ProfileRepository.profile.collectAsState(initial = BasicProfile())

    var fullName by remember(profileState) { mutableStateOf(profileState.fullName) }
    var mobileNumber by remember(profileState) { mutableStateOf(profileState.mobileNumber) }
    var email by remember(profileState) { mutableStateOf(profileState.email) }
    var address by remember(profileState) { mutableStateOf(profileState.address) }
    var dateOfBirth by remember(profileState) { mutableStateOf(profileState.dateOfBirth) }
    var preferredLanguage by remember(profileState) { mutableStateOf(profileState.preferredLanguage) }

    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(SahaaySpacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(SahaaySpacing.md)
        ) {
            Text(
                text = "Your Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(SahaayCorners.medium)
            ) {
                Text(
                    text = "Your saved details can help Sahaay fill common forms faster. We never store passwords, OTPs, or PINs.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(SahaaySpacing.md)
                )
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(SahaayCorners.medium)
            )

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = { Text("Mobile Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(SahaayCorners.medium)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(SahaayCorners.medium)
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Home Address") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(SahaayCorners.medium)
            )

            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                label = { Text("Date of Birth") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(SahaayCorners.medium)
            )

            OutlinedTextField(
                value = preferredLanguage,
                onValueChange = { preferredLanguage = it },
                label = { Text("Preferred Language") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(SahaayCorners.medium)
            )

            Spacer(modifier = Modifier.height(SahaaySpacing.md))

            Button(
                onClick = {
                    val updatedProfile = BasicProfile(
                        fullName = fullName,
                        mobileNumber = mobileNumber,
                        email = email,
                        address = address,
                        dateOfBirth = dateOfBirth,
                        preferredLanguage = preferredLanguage
                    )
                    ProfileRepository.save(updatedProfile)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Profile saved successfully")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SahaayCorners.medium)
            ) {
                Text(
                    text = "Save Changes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = SahaaySpacing.sm)
                )
            }

            OutlinedButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SahaayCorners.medium)
            ) {
                Text(
                    text = "Clear All Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = SahaaySpacing.sm)
                )
            }

            Spacer(modifier = Modifier.height(SahaaySpacing.lg))
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Clear Profile Details?") },
                text = { Text("This will remove all your saved personal details from this app. Are you sure?", style = MaterialTheme.typography.bodyLarge) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            ProfileRepository.clear()
                            showClearDialog = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Profile cleared")
                            }
                        }
                    ) {
                        Text("Clear", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Cancel", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        }
    }
}
