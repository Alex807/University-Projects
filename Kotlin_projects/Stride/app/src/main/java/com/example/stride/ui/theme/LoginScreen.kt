package com.example.stride.ui.theme

import android.graphics.BitmapFactory
import android.util.Base64
import com.example.stride.R
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stride.auth.SavedAccount
import com.example.stride.viewmodel.AuthState
import com.example.stride.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var showAccountsDropdown by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableStateOf(0L) }

    val authState by authViewModel.authState.collectAsState()
    val savedAccounts by authViewModel.savedAccounts.collectAsState()
    val focusManager = LocalFocusManager.current
    val usernameFocusRequester = remember { FocusRequester() }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
            authViewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Stride Logo",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Stride",
                fontSize = 33.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "Trace every move!",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(44.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Username field with smart click detection
                    Box {
                        Column {
                            val usernameInteractionSource = remember { MutableInteractionSource() }

                            // Handle username field clicks
                            LaunchedEffect(usernameInteractionSource) {
                                usernameInteractionSource.interactions.collect { interaction ->
                                    when (interaction) {
                                        is PressInteraction.Press -> {
                                            val currentTime = System.currentTimeMillis()
                                            val timeDifference = currentTime - lastClickTime

                                            if (savedAccounts.isNotEmpty()) {
                                                if (timeDifference < 15000 && showAccountsDropdown) {
                                                    // Second click within 15 seconds - close dropdown and allow typing
                                                    showAccountsDropdown = false
                                                    focusManager.clearFocus()
                                                    // Request focus again to show keyboard
                                                    kotlinx.coroutines.delay(100)
                                                    usernameFocusRequester.requestFocus()
                                                } else {
                                                    // First click or after 15 seconds - show dropdown
                                                    showAccountsDropdown = true
                                                }
                                                lastClickTime = currentTime
                                            }
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it.trim() },
                                label = { Text("Username") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(usernameFocusRequester),
                                singleLine = true,
                                interactionSource = usernameInteractionSource,
                                readOnly = showAccountsDropdown
                            )

                            // Saved Accounts Dropdown
                            AnimatedVisibility(
                                visible = showAccountsDropdown && savedAccounts.isNotEmpty(),
                                enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column {
                                        // Header with close button
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Recently Used",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            IconButton(
                                                onClick = {
                                                    showAccountsDropdown = false
                                                    focusManager.clearFocus()
                                                    // Request focus again to show keyboard
                                                    kotlinx.coroutines.MainScope().launch {
                                                        kotlinx.coroutines.delay(100)
                                                        usernameFocusRequester.requestFocus()
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Close",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        HorizontalDivider()

                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 300.dp)
                                        ) {
                                            items(savedAccounts) { account ->
                                                SavedAccountItem(
                                                    account = account,
                                                    onClick = {
                                                        val decryptedPassword = authViewModel.getDecryptedPassword(
                                                            account.encryptedPassword
                                                        )

                                                        if (decryptedPassword != null) {
                                                            // Auto-login immediately with decrypted credentials
                                                            authViewModel.login(account.username, decryptedPassword, true)
                                                            showAccountsDropdown = false
                                                        } else {
                                                            // Fallback: just fill the fields if decryption fails
                                                            username = account.username
                                                            password = ""
                                                            rememberMe = true
                                                            showAccountsDropdown = false
                                                        }
                                                    }
                                                )
                                                if (account != savedAccounts.last()) {
                                                    HorizontalDivider()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it.trim() },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Remember Me Checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it }
                        )
                        Text(
                            text = "Remember Me",
                            modifier = Modifier.clickable { rememberMe = !rememberMe },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (authState is AuthState.Error) {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Button(
                        onClick = { authViewModel.login(username, password, rememberMe) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = authState !is AuthState.Loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Login")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Don't have an account? Register")
                    }
                }
            }
        }
    }
}

@Composable
fun SavedAccountItem(
    account: SavedAccount,
    onClick: () -> Unit
) {
    // Process image outside composable context
    val profileBitmap = remember(account.profileImageBase64) {
        account.profileImageBase64?.let {
            try {
                val imageBytes = Base64.decode(it, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } catch (e: Exception) {
                null
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Image
        if (profileBitmap != null) {
            Image(
                bitmap = profileBitmap.asImageBitmap(),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            PlaceholderProfileImage()
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Username
        Text(
            text = account.username,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PlaceholderProfileImage() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Default profile",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
    }
}