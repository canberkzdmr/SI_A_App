package com.cbo.user.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.core.domain.model.Gender
import com.cbo.core.domain.model.User
import com.cbo.core.domain.model.UserDetail
import com.cbo.core.session.domain.usecase.GetActiveUserUseCase
import com.cbo.user.domain.usecase.GetUserWithDetailUseCase
import com.cbo.user.domain.usecase.SaveImageUseCase
import com.cbo.user.domain.usecase.UpsertUserDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel
@Inject
constructor(
    private val getActiveUserUseCase: GetActiveUserUseCase,
    private val getUserWithDetailUseCase: GetUserWithDetailUseCase,
    private val upsertUserDetailUseCase: UpsertUserDetailUseCase,
    private val saveImageUseCase: SaveImageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditUserProfileUiState(isLoading = true))
    val uiState: StateFlow<EditUserProfileUiState> = _uiState.asStateFlow()

    fun loadUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getActiveUserUseCase()
                .catch { _uiState.update { it.copy(isLoading = false, error = "User informations could not retrieved") } }
                .collect { user: User? ->
                    user?.let { user ->
                        Log.d("EditProfileViewModel", "Retrieved User: ${user.username}(${user.id})")
                        getUserWithDetailUseCase.invoke(user.id)
                            .catch { error ->
                                _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
                            }
                            .map { userWithDetail ->
                                userWithDetail?.let {
                                    Log.d("EditProfileViewModel", "dateOfBirth: ${userWithDetail.userDetail?.dateOfBirth}")
                                    Log.d("EditProfileViewModel", "gender: ${userWithDetail.userDetail?.gender}")
                                    EditUserProfileUiState(
                                        id = userWithDetail.userDetail?.id,
                                        userId = userWithDetail.user.id,
                                        username = userWithDetail.user.username,
                                        email = userWithDetail.user.email,
                                        fullName = userWithDetail.userDetail?.fullName.orEmpty(),
                                        gender = userWithDetail.userDetail?.gender,
                                        dateOfBirth = userWithDetail.userDetail?.dateOfBirth,
                                        avatarUrl = userWithDetail.userDetail?.avatarUrl.orEmpty(),
                                        bio = userWithDetail.userDetail?.bio.orEmpty(),
                                        phoneNumber = userWithDetail.userDetail?.phoneNumber.orEmpty(),
                                        address = userWithDetail.userDetail?.address.orEmpty(),
                                        isLoading = false
                                    )
                                } ?: run {
                                    Log.e("EditProfileViewModel", "Error while retrieving user details. User detail is null")
                                    EditUserProfileUiState(isLoading = false, error = "Could not get user details")
                                }
                            }
                            .collect { detail ->
                                _uiState.value = detail
                            }
                    }
                }
        }
    }

    fun updateFullName(fullName: String) {
        _uiState.value = _uiState.value.copy(
            fullName = fullName
        )
    }

    fun updateAvatarUrl(avatarUrl: String) {
        _uiState.value = _uiState.value.copy(
            avatarUrl = avatarUrl
        )
    }

    /**
     * Handles image selection from gallery picker
     * Copies the image to internal storage for persistence
     */
    fun onImageSelected(contentUri: Uri) {
        viewModelScope.launch {
            try {
                Log.d("EditProfileViewModel", "Image selected: $contentUri")
                
                // Set image loading state
                _uiState.update { it.copy(isImageLoading = true, error = null) }
                
                val currentAvatarUrl = _uiState.value.avatarUrl
                
                // Copy image to internal storage
                val savedImagePath = saveImageUseCase(contentUri, currentAvatarUrl)
                
                if (savedImagePath != null) {
                    Log.d("EditProfileViewModel", "Image saved to: $savedImagePath")
                    updateAvatarUrl(savedImagePath)
                    _uiState.update { it.copy(isImageLoading = false) }
                } else {
                    Log.e("EditProfileViewModel", "Failed to save image")
                    _uiState.update { it.copy(isImageLoading = false, error = "Failed to save image") }
                }
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Error processing image", e)
                _uiState.update { it.copy(isImageLoading = false, error = "Error processing image: ${e.message}") }
            }
        }
    }

    fun updateBio(bio: String) {
        _uiState.value = _uiState.value.copy(
            bio = bio
        )
    }

    fun updatePhoneNumber(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(
            phoneNumber = phoneNumber
        )
    }

    fun updateAddress(address: String) {
        _uiState.value = _uiState.value.copy(
            address = address
        )
    }

    fun updateGender(gender: Gender?) {
        Log.d("EditProfileViewModel", "updateGender: $gender")
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun updateDateOfBirth(dob: Long?) {
        Log.d("EditProfileViewModel", "update dob: $dob")
        _uiState.value = _uiState.value.copy(dateOfBirth = dob)
    }

    fun save() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val detail = UserDetail(
                id = _uiState.value.id,
                userId = _uiState.value.userId,
                fullName = _uiState.value.fullName,
                avatarUrl = _uiState.value.avatarUrl,
                bio = _uiState.value.bio,
                phoneNumber = _uiState.value.phoneNumber,
                address = _uiState.value.address,
                dateOfBirth = _uiState.value.dateOfBirth,
                gender = _uiState.value.gender,
            )
            upsertUserDetailUseCase.invoke(detail).fold(
                onSuccess = {
                    Log.i("EditProfileViewModel", "User details updated")
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                    Log.d("EditProfileViewModel", "uiState.isSaved: ${_uiState.value.isSaved}")
                },
                onFailure = { error ->
                    Log.e("EditProfileViewModel", "Failed to update user(id: ${_uiState.value.userId}) -> ${error.message}")
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }
}

data class EditUserProfileUiState(
    val id: Int? = null,
    val userId: Int = -1,
    val username: String = "",
    val email: String = "",
    val fullName: String = "",
    val gender: Gender? = null,
    val dateOfBirth: Long? = null, // Epoch millis for DatePicker
    val avatarUrl: String = "",
    val bio: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val isLoading: Boolean = false,
    val isImageLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)