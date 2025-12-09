package com.example.smartview.auth

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    
    var uiState by mutableStateOf(ProfileUiState())
        private set
    
    // Store selected photo URI locally
    private var selectedPhotoUri: Uri? = null
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            uiState = uiState.copy(
                displayName = user.displayName ?: "",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString(),
                originalDisplayName = user.displayName ?: "",
                originalEmail = user.email ?: ""
            )
        }
    }
    
    fun onDisplayNameChange(newName: String) {
        uiState = uiState.copy(
            displayName = newName,
            hasChanges = newName != uiState.originalDisplayName || uiState.email != uiState.originalEmail
        )
    }
    
    fun onEmailChange(newEmail: String) {
        uiState = uiState.copy(
            email = newEmail,
            hasChanges = uiState.displayName != uiState.originalDisplayName || newEmail != uiState.originalEmail
        )
    }
    
    fun onPhotoSelected(uri: Uri) {
        selectedPhotoUri = uri
        uiState = uiState.copy(
            localPhotoUri = uri.toString(),
            hasChanges = true
        )
    }
    
    fun saveChanges(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser ?: return
        
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                // Update display name if changed
                if (uiState.displayName != uiState.originalDisplayName) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(uiState.displayName)
                        .build()
                    user.updateProfile(profileUpdates).await()
                }
                
                // Update email if changed
                if (uiState.email != uiState.originalEmail) {
                    user.verifyBeforeUpdateEmail(uiState.email).await()
                }
                
                uiState = uiState.copy(
                    isLoading = false,
                    hasChanges = false,
                    originalDisplayName = uiState.displayName,
                    successMessage = "Perfil actualizado correctamente"
                )
                onSuccess()
                
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al actualizar perfil"
                )
                onError(uiState.errorMessage ?: "Error desconocido")
            }
        }
    }
    
    fun clearMessages() {
        uiState = uiState.copy(successMessage = null, errorMessage = null)
    }
}

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val localPhotoUri: String? = null,
    val originalDisplayName: String = "",
    val originalEmail: String = "",
    val isLoading: Boolean = false,
    val hasChanges: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
