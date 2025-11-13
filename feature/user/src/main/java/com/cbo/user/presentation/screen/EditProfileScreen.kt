package com.cbo.user.presentation.screen

import android.app.DatePickerDialog
import android.graphics.drawable.Icon
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.cbo.core.common.util.DatePattern
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppIconButton
import com.cbo.ui.components.AppOutlinedTextField
import com.cbo.ui.components.AppTitle
import com.cbo.ui.components.DatePickerField
import com.cbo.ui.components.PrimaryButton
import com.cbo.ui.components.ScreenWithTopBarAndInsets
import com.cbo.ui.components.SecondaryButton
import com.cbo.ui.components.SectionHeader
import com.cbo.ui.components.SelectionButtonStyle
import com.cbo.ui.components.SelectionOption
import com.cbo.ui.components.ShimmerBox
import com.cbo.ui.components.SingleSelectionButton
import com.cbo.ui.components.TertiaryButton
import com.cbo.ui.theme.MemCloudApplicationTheme
import com.cbo.user.R
import com.cbo.core.domain.model.Gender
import com.cbo.user.presentation.viewmodel.EditProfileViewModel
import com.cbo.user.presentation.viewmodel.EditUserProfileUiState
import kotlinx.coroutines.selects.select
import java.util.Calendar

@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = hiltViewModel(),
    onSaveSuccess: () -> Unit,
    onCancelEditProfile: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUser()
    }

    EditProfileScreenContent(
        uiState,
        onCancel = { onCancelEditProfile() },
        updateFullName = viewModel::updateFullName,
        updateGender = viewModel::updateGender,
        updateDateOfBirth = viewModel::updateDateOfBirth,
        updateAddress = viewModel::updateAddress,
        updateBio = viewModel::updateBio,
        updatePhoneNumber = viewModel::updatePhoneNumber,
        onImageSelected = viewModel::onImageSelected,
        save = { viewModel.save() },
    )

    // Show error
    uiState.error?.let { errorMsg ->
        Text(
            text = errorMsg,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 8.dp),
        )
    }

    // Trigger success callback
    if (uiState.isSaved) {
        Log.d("EditProfileScreen", "uiState.isSaved set to TRUE")
        LaunchedEffect(Unit) {
            onSaveSuccess()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreenContent(
    uiState: EditUserProfileUiState,
    onCancel: () -> Unit,
    updateFullName: (String) -> Unit,
    updateGender: (Gender?) -> Unit,
    updateDateOfBirth: (Long?) -> Unit,
    updateBio: (String) -> Unit,
    updatePhoneNumber: (String) -> Unit,
    updateAddress: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    save: () -> Unit,
) {
    ScreenWithTopBarAndInsets(
        topBar = {
            CenterAlignedTopAppBar(
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                title = {
                    AppTitle(
                        stringResource(id = R.string.edit_profile_title),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                },
                navigationIcon = {
                    AppIconButton(
                        onClick = { onCancel() },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = stringResource(id = com.cbo.user.R.string.back),
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        },
                    )
                },
                actions = {
                    AppIconButton(onClick = save, icon = {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    })
                }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    EditableProfileImage(
                        modifier = Modifier,
                        isLoading = uiState.isLoading,
                        isImageLoading = uiState.isImageLoading,
                        avatarUrl = uiState.avatarUrl,
                        username = uiState.username,
                        onImageSelected = onImageSelected,
                    )
                }
            }

            // Account Information
            item {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp, bottom = 8.dp)) {
                    SectionHeader(
                        title = stringResource(R.string.account_information_section).uppercase(),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    AppOutlinedTextField(
                        value = uiState.username,
                        onValueChange = { },
                        label = stringResource(id = R.string.username),
                        readOnly = true,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .focusRequester(remember { FocusRequester() }),
                    )

                    AppOutlinedTextField(
                        value = uiState.email,
                        onValueChange = { },
                        label = stringResource(id = R.string.email),
                        readOnly = true,
                    )
                }
            }

            // Personal Details
            item {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)) {
                    SectionHeader(
                        title = stringResource(R.string.personal_details_section).uppercase(),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    AppOutlinedTextField(
                        value = uiState.fullName,
                        label = stringResource(R.string.full_name),
                        onValueChange = updateFullName,
                    )

                    AppBody(stringResource(R.string.gender))
                    SingleSelectionButton(
                        modifier = Modifier.padding(vertical = 8.dp),
                        options = listOf(
                            SelectionOption(value = Gender.MALE, label = stringResource(R.string.male)),
                            SelectionOption(value = Gender.FEMALE, label = stringResource(R.string.female)),
                        ),
                        selectedValue = uiState.gender,
                        onSelectionChange = updateGender,
                        style = SelectionButtonStyle.Filled
                    )

                    DatePickerField(
                        modifier = Modifier.padding(top = 8.dp),
                        selectedDate = uiState.dateOfBirth,
                        onDateSelected = { selectedDate ->
                            updateDateOfBirth(selectedDate)
                         },
                        label = stringResource(R.string.date_of_birth),
                        placeholder = stringResource(R.string.date_of_birth),
                        datePattern = DatePattern.READABLE
                    )
                }
            }
        }
    }

}

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreenContent(
    uiState: EditUserProfileUiState,
    onCancel: () -> Unit,
    updateFullName: (String) -> Unit,
    updateGender: (String) -> Unit,
    updateDateOfBirth: (String) -> Unit,
    updateBio: (String) -> Unit,
    updatePhoneNumber: (String) -> Unit,
    updateAddress: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    save: () -> Unit,
) {
    // Launcher for gallery picker
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let { selectedUri ->
                Log.i("EditProfileScreen", "Image selected: $selectedUri")
                onImageSelected(selectedUri)
            }
        }

    val focusRequesterGender = remember { FocusRequester() }
    val focusRequesterDateOfBirth = remember { FocusRequester() }
    val focusRequesterBio = remember { FocusRequester() }
    val focusRequesterPhoneNumber = remember { FocusRequester() }
    val focusRequesterAddress = remember { FocusRequester() }

    ScreenWithTopBarAndInsets(
        topBar = {
            CenterAlignedTopAppBar(
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                title = {
                    AppTitle(
                        stringResource(id = com.cbo.user.R.string.edit_profile_title),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                },
                navigationIcon = {
                    AppIconButton(
                        onClick = { onCancel() },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = stringResource(id = com.cbo.user.R.string.back),
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        },
                    )
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
        ) {
            // ------------------------
            // Profile Header with Avatar + Picker
            // ------------------------
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Container Box for avatar and edit button - no clipping here
                    Box(
                        modifier = Modifier.size(100.dp), // Larger to accommodate edit button
                        contentAlignment = Alignment.Center,
                    ) {
                        // Avatar container with clipping
                        Box(
                            modifier =
                                Modifier
                                    .size(200.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                uiState.isLoading -> {
                                    // Overall loading state
                                    ShimmerBox(
                                        modifier = Modifier.fillMaxSize(),
                                        cornerRadius = 60.dp, // Circular shape
                                    )
                                }

                                uiState.isImageLoading -> {
                                    // Image loading state with shimmer
                                    ShimmerBox(
                                        modifier = Modifier.fillMaxSize(),
                                        cornerRadius = 60.dp, // Circular shape
                                    )
                                }

                                uiState.avatarUrl.isNotEmpty() -> {
                                    SubcomposeAsyncImage(
                                        model = uiState.avatarUrl,
                                        contentDescription = stringResource(id = com.cbo.user.R.string.profile_picture),
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        loading = {
                                            ShimmerBox(
                                                modifier = Modifier.fillMaxSize(),
                                                cornerRadius = 60.dp, // Circular shape
                                            )
                                        },
                                    )
                                }

                                else -> {
                                    AppTitle(
                                        text =
                                            uiState.username
                                                .firstOrNull()
                                                ?.uppercaseChar()
                                                ?.toString() ?: "?",
                                    )
                                }
                            }
                        }

                        // Change Avatar Button - positioned outside the clipped avatar
                        IconButton(
                            onClick = {
                                if (!uiState.isImageLoading) {
                                    launcher.launch("image/*")
                                }
                            },
                            enabled = !uiState.isImageLoading,
                            modifier =
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(24.dp)
                                    .background(
                                        if (uiState.isImageLoading) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                        CircleShape,
                                    ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(id = com.cbo.user.R.string.change_avatar),
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AppTitle(uiState.username)
                    Spacer(Modifier.height(4.dp))
                    AppBody(
                        text = uiState.email,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .padding(top = 16.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
                    )
                }
            }

            // ------------------------
            // Editable Fields Section (no Avatar URL anymore)
            // ------------------------
            item {
                AppOutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = updateFullName,
                    label = stringResource(id = com.cbo.user.R.string.full_name),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .focusRequester(remember { FocusRequester() }),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusRequesterGender.requestFocus() }),
                )

                // Gender
                GenderSelector(
                    selectedGender = uiState.gender,
                    onGenderSelected = updateGender,
                    modifier = Modifier.focusRequester(focusRequesterGender),
                )

                // Date of birth
                DateOfBirthPicker(
                    dob = uiState.dateOfBirth,
                    onDobSelected = updateDateOfBirth,
                )

                // Bio
                AppOutlinedTextField(
                    value = uiState.bio,
                    onValueChange = updateBio,
                    label = stringResource(id = com.cbo.user.R.string.bio),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                )
                AppOutlinedTextField(
                    value = uiState.phoneNumber,
                    onValueChange = updatePhoneNumber,
                    label = stringResource(id = com.cbo.user.R.string.phone_number),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                )
                AppOutlinedTextField(
                    value = uiState.address,
                    onValueChange = updateAddress,
                    label = stringResource(id = com.cbo.user.R.string.address),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                )
            }

            // ------------------------
            // Save Button
            // ------------------------
            item {
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = stringResource(id = com.cbo.user.R.string.save),
                    onClick = save,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    isLoading = uiState.isLoading,
                )
            }
        }
    }
}*/*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderSelector(
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    val options = listOf(stringResource(id = com.cbo.user.R.string.male), stringResource(id = com.cbo.user.R.string.female))
    var expanded by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        AppOutlinedTextField(
            value = selectedGender,
            onValueChange = {},
            modifier =
                modifier
                    .menuAnchor()
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        Log.d("EditProfileScreen", "Dropdown focus state: ${state.isFocused}")
                        if (state.isFocused) {
                            expanded = true
                            keyboardController?.hide()
                        }
                    },
            label = stringResource(id = com.cbo.user.R.string.gender),
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            onClick = {
                expanded = true
                keyboardController?.hide()
            },
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { gender ->
                DropdownMenuItem(
                    text = { Text(gender) },
                    onClick = {
                        onGenderSelected(gender)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun EditableProfileImage(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isImageLoading: Boolean = false,
    avatarUrl: String,
    username: String,
    onImageSelected: (Uri) -> Unit,
) {
    // Launcher for gallery picker
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let { selectedUri ->
                Log.i("EditProfileScreen", "Image selected: $selectedUri")
                onImageSelected(selectedUri)
            }
        }

    // Container Box for avatar and edit button - no clipping here
    Box(
        modifier = modifier.size(100.dp), // Larger to accommodate edit button
        contentAlignment = Alignment.Center,
    ) {
        // Avatar container with clipping
        Box(
            modifier =
                Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isLoading -> {
                    // Overall loading state
                    ShimmerBox(
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 60.dp, // Circular shape
                    )
                }

                isImageLoading -> {
                    // Image loading state with shimmer
                    ShimmerBox(
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 60.dp, // Circular shape
                    )
                }

                avatarUrl.isNotEmpty() -> {
                    SubcomposeAsyncImage(
                        model = avatarUrl,
                        contentDescription = stringResource(id = R.string.profile_picture),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            ShimmerBox(
                                modifier = Modifier.fillMaxSize(),
                                cornerRadius = 60.dp, // Circular shape
                            )
                        },
                    )
                }

                else -> {
                    AppTitle(
                        text =
                            username
                                .firstOrNull()
                                ?.uppercaseChar()
                                ?.toString() ?: "?",
                    )
                }
            }
        }

        // Change Avatar Button - positioned outside the clipped avatar
        AppIconButton(
            onClick = {
                if (!isImageLoading) {
                    launcher.launch("image/*")
                }
            },
            enabled = !isImageLoading,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(24.dp)
                .background(
                    if (isImageLoading) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    CircleShape,
                ),
            icon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.change_avatar),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        )
    }
}

@Composable
fun DateOfBirthPicker(
    dob: String,
    onDobSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog =
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val selected = "%04d-%02d-%02d".format(year, month + 1, day)
                onDobSelected(selected)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        )

    AppOutlinedTextField(
        value = dob,
        onValueChange = {},
        label = stringResource(id = com.cbo.user.R.string.date_of_birth),
        readOnly = true,
        onClick = { datePickerDialog.show() },
        modifier = modifier,
    )
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun EditProfileScreenPreview() {
    MemCloudApplicationTheme {
        EditProfileScreenContent(
            uiState =
                EditUserProfileUiState(
                    username = "canberk",
                    email = "canberk@example.com",
                    fullName = "Can Berk",
                    avatarUrl = "https://example.com/avatar.png",
                    bio = "Android Developer, coffee lover ☕",
                    phoneNumber = "+90 555 555 55 55",
                    address = "Istanbul, Türkiye",
                    isLoading = false,
                    isImageLoading = false,
                    error = null,
                    isSaved = false,
                ),
            onCancel = {},
            updateFullName = {},
            updateGender = {},
            updateDateOfBirth = {},
            updateBio = {},
            updateAddress = {},
            onImageSelected = {},
            updatePhoneNumber = {},
            save = {},
        )
    }
}