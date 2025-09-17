package com.cbo.user.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbo.user.domain.usecase.GetUserWithDetailUseCase
import com.cbo.user.domain.usecase.UpsertUserDetailUseCase
import com.example.core.data.model.UserDetailEntity
import com.example.core.domain.model.User
import com.example.core.domain.usecase.GetActiveUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
                        getUserWithDetailUseCase(user.id).fold(
                            onSuccess = { userWithDetail ->
                                _uiState.value = EditUserProfileUiState(
                                    userId = userWithDetail.user.id,
                                    username = userWithDetail.user.username,
                                    email = userWithDetail.user.email,
                                    fullName = userWithDetail.detail?.fullName.orEmpty(),
                                    avatarUrl = userWithDetail.detail?.avatarUrl.orEmpty(),
                                    bio = userWithDetail.detail?.bio.orEmpty(),
                                    phoneNumber = userWithDetail.detail?.phoneNumber.orEmpty(),
                                    address = userWithDetail.detail?.address.orEmpty(),
                                    isLoading = false
                                )
                            },
                            onFailure = {
                                _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                            }
                        )
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

    fun save() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val detail = UserDetailEntity(
                userId = _uiState.value.userId,
                fullName = _uiState.value.fullName,
                avatarUrl = _uiState.value.avatarUrl,
                bio = _uiState.value.bio,
                phoneNumber = _uiState.value.phoneNumber,
                address = _uiState.value.address,
                dateOfBirth = null,
                gender = null
            )
            upsertUserDetailUseCase.invoke(detail).fold(
                onSuccess = {
                    Log.i("EditProfileViewModel", "User details updated")
                    _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
                },
                onFailure = {
                    Log.e("EditProfileViewModel", "Failed to update user(id: ${_uiState.value.userId}) -> ${it.message}")
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }
}

data class EditUserProfileUiState(
    val userId: Int = -1,
    val username: String = "",
    val email: String = "",
    val fullName: String = "",
    val avatarUrl: String = "",
    val bio: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)
