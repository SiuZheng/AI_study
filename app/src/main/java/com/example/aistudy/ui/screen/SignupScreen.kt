package com.example.aistudy.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aistudy.ui.navigation.Screen
import com.example.aistudy.ui.viewmodel.AuthViewModel
import com.example.aistudy.ui.viewmodel.SignupStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf<Date?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    
    // For date picker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val formatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    
    // Format date string for display
    val dateString = remember(dateOfBirth) {
        dateOfBirth?.let { formatter.format(it) } ?: "Select date of birth"
    }
    
    val signupStatus by authViewModel.signupStatus.observeAsState(SignupStatus.Initial)
    val currentUser by authViewModel.currentUser.observeAsState()
    
    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = it
                        dateOfBirth = calendar.time
                    }
                    showDatePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // If user is signed up and logged in, navigate to home screen
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate(Screen.StudyPlanner.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }
    
    // Show error message if signup fails
    LaunchedEffect(signupStatus) {
        if (signupStatus is SignupStatus.Error) {
            snackbarHostState.showSnackbar(
                message = (signupStatus as SignupStatus.Error).message
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Sign up for AI Study",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Username Field
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    placeholder = { Text("Enter your username") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Username Icon"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("Enter your email") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date of Birth Field
                OutlinedTextField(
                    value = dateString,
                    onValueChange = { },
                    label = { Text("Date of Birth") },
                    placeholder = { Text("Select your date of birth") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar Icon"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    enabled = false,
                    readOnly = true,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("Enter your password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password Icon"
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Confirm Password Field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    placeholder = { Text("Confirm your password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Confirm Password Icon"
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = "Toggle confirm password visibility"
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    isError = password != confirmPassword && confirmPassword.isNotEmpty()
                )
                
                if (password != confirmPassword && confirmPassword.isNotEmpty()) {
                    Text(
                        text = "Passwords do not match",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Signup Button
                Button(
                    onClick = {
                        if (username.isNotBlank() && email.isNotBlank() && password.isNotBlank() && 
                            password == confirmPassword && dateOfBirth != null) {
                            // Use the updated signup method that includes username and dateOfBirth
                            authViewModel.signup(email, password, username, dateOfBirth!!)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = username.isNotBlank() && email.isNotBlank() && password.isNotBlank() && 
                        password == confirmPassword && dateOfBirth != null && signupStatus !is SignupStatus.Loading
                ) {
                    if (signupStatus is SignupStatus.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Create Account")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Login Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Already have an account? ")
                    Text(
                        text = "Login",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Signup.route) { inclusive = true }
                            }
                        }
                    )
                }
                
                // Extra space at bottom to ensure all content is visible
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
} 