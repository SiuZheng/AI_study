package com.example.aistudy.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aistudy.data.StreakManager
import com.example.aistudy.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private lateinit var streakManager: StreakManager
    
    // Flag to track if StreakManager has been initialized
    private var isStreakManagerInitialized = false
    
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser
    
    private val _userData = MutableLiveData<UserData?>()
    val userData: LiveData<UserData?> = _userData
    
    private val _loginStatus = MutableLiveData<LoginStatus>()
    val loginStatus: LiveData<LoginStatus> = _loginStatus
    
    private val _signupStatus = MutableLiveData<SignupStatus>()
    val signupStatus: LiveData<SignupStatus> = _signupStatus
    
    private val _userDataStatus = MutableLiveData<UserDataStatus>()
    val userDataStatus: LiveData<UserDataStatus> = _userDataStatus
    
    init {
        _currentUser.value = auth.currentUser
        if (auth.currentUser != null) {
            fetchUserData(auth.currentUser!!.uid)
        }
    }
    
    /**
     * Initialize the StreakManager with application context
     * This must be called before using streak-related features
     */
    fun initializeStreakManager(context: Context) {
        if (!isStreakManagerInitialized) {
            streakManager = StreakManager(context.applicationContext)
            isStreakManagerInitialized = true
        }
    }
    
    /**
     * Update streak count both in SharedPreferences and Firestore
     * Should be called when app is opened
     */
    fun updateStreak() {
        if (!isStreakManagerInitialized) {
            return
        }
        
        val updatedStreak = streakManager.checkAndUpdateStreak()
        
        // Only update Firestore if there's a logged in user
        val currentUserId = auth.currentUser?.uid ?: return
        
        // Update streak in Firestore
        viewModelScope.launch {
            try {
                // Get current user data first
                val documentSnapshot = firestore.collection("users")
                    .document(currentUserId)
                    .get()
                    .await()
                
                if (documentSnapshot.exists()) {
                    val currentUserData = documentSnapshot.toObject(UserData::class.java)
                    
                    // Only update if streak value actually changed
                    if (currentUserData != null && currentUserData.streak != updatedStreak) {
                        // Update just the streak field in Firestore
                        firestore.collection("users")
                            .document(currentUserId)
                            .update("streak", updatedStreak)
                            .await()
                        
                        // Update the local userData object
                        _userData.value = currentUserData.copy(streak = updatedStreak)
                    }
                }
            } catch (e: Exception) {
                // Silently handle error - we don't want to disturb user experience
                // for streak tracking failures
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _loginStatus.value = LoginStatus.Loading
                
                auth.signInWithEmailAndPassword(email, password).await()
                _currentUser.value = auth.currentUser
                
                // Update last login time
                auth.currentUser?.let { user ->
                    updateLastLogin(user.uid)
                    fetchUserData(user.uid)
                    
                    // Update streak after login if streakManager is initialized
                    if (isStreakManagerInitialized) {
                        updateStreak()
                    }
                }
                
                _loginStatus.value = LoginStatus.Success
            } catch (e: Exception) {
                _loginStatus.value = LoginStatus.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    fun signup(email: String, password: String, username: String, dateOfBirth: Date) {
        viewModelScope.launch {
            try {
                _signupStatus.value = SignupStatus.Loading
                
                // Create the user authentication
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user
                
                // After authentication, save user data to Firestore
                if (user != null) {
                    val userData = UserData(
                        userId = user.uid,
                        username = username,
                        email = email,
                        dateOfBirth = dateOfBirth,
                        createdAt = Date(),
                        lastLogin = Date(),
                        totalStudyHours = 0,
                        flashcardsCreated = 0,
                        streak = 0
                    )
                    saveUserDataToFirestore(userData)
                    _userData.value = userData
                    _currentUser.value = user
                    _signupStatus.value = SignupStatus.Success
                } else {
                    throw Exception("Failed to create user")
                }
            } catch (e: Exception) {
                _signupStatus.value = SignupStatus.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    private suspend fun saveUserDataToFirestore(userData: UserData) {
        firestore.collection("users")
            .document(userData.userId)
            .set(userData)
            .await()
    }
    
    private suspend fun updateLastLogin(userId: String) {
        firestore.collection("users")
            .document(userId)
            .update("lastLogin", Date())
            .await()
    }
    
    fun fetchUserData(userId: String) {
        viewModelScope.launch {
            try {
                _userDataStatus.value = UserDataStatus.Loading
                
                val documentSnapshot = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                
                if (documentSnapshot.exists()) {
                    val userData = documentSnapshot.toObject(UserData::class.java)
                    _userData.value = userData
                    _userDataStatus.value = UserDataStatus.Success
                } else {
                    // If document doesn't exist, create a basic one with available auth data
                    val user = auth.currentUser
                    if (user != null) {
                        val basicUserData = UserData(
                            userId = user.uid,
                            username = user.displayName ?: "AI Study User",
                            email = user.email ?: "",
                            createdAt = Date(),
                            lastLogin = Date()
                        )
                        saveUserDataToFirestore(basicUserData)
                        _userData.value = basicUserData
                        _userDataStatus.value = UserDataStatus.Success
                    } else {
                        throw Exception("User not found")
                    }
                }
            } catch (e: Exception) {
                _userDataStatus.value = UserDataStatus.Error(e.message ?: "Failed to fetch user data")
            }
        }
    }
    
    fun updateUserData(updatedData: UserData) {
        viewModelScope.launch {
            try {
                _userDataStatus.value = UserDataStatus.Loading
                
                // Ensure we have the current user ID
                val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
                
                // Make sure we're updating the correct user
                val dataToUpdate = updatedData.copy(userId = currentUserId)
                
                // Update in Firestore
                saveUserDataToFirestore(dataToUpdate)
                
                // Update local data
                _userData.value = dataToUpdate
                _userDataStatus.value = UserDataStatus.Success
            } catch (e: Exception) {
                _userDataStatus.value = UserDataStatus.Error(e.message ?: "Failed to update user data")
            }
        }
    }
    
    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _userData.value = null
    }
    
    fun resetPassword(email: String, onComplete: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }
    
    /**
     * Update the user's password
     * 
     * @param currentPassword The user's current password
     * @param newPassword The new password to set
     * @param onComplete Callback with success status and optional error message
     */
    fun updatePassword(currentPassword: String, newPassword: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                // Get current user
                val user = auth.currentUser ?: throw Exception("User not authenticated")
                val email = user.email ?: throw Exception("User email not available")
                
                // Re-authenticate the user first (required for security-sensitive operations)
                try {
                    // Create credentials with current password
                    val credential = com.google.firebase.auth.EmailAuthProvider
                        .getCredential(email, currentPassword)
                    
                    // Re-authenticate
                    user.reauthenticate(credential).await()
                    
                    // Now update the password
                    user.updatePassword(newPassword).await()
                    
                    // Success
                    onComplete(true, null)
                } catch (e: Exception) {
                    // Authentication failed
                    onComplete(false, "Current password is incorrect")
                }
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Failed to update password")
            }
        }
    }
}

sealed class LoginStatus {
    object Initial : LoginStatus()
    object Loading : LoginStatus()
    object Success : LoginStatus()
    data class Error(val message: String) : LoginStatus()
}

sealed class SignupStatus {
    object Initial : SignupStatus()
    object Loading : SignupStatus()
    object Success : SignupStatus()
    data class Error(val message: String) : SignupStatus()
}

sealed class UserDataStatus {
    object Initial : UserDataStatus()
    object Loading : UserDataStatus()
    object Success : UserDataStatus()
    data class Error(val message: String) : UserDataStatus()
} 