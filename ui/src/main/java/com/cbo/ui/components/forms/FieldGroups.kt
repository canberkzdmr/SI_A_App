package com.cbo.ui.components.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.cbo.ui.components.AppBody
import com.cbo.ui.components.AppOutlinedTextField
import com.cbo.ui.components.AppTitle

/**
 * Personal information field group
 */
@Composable
fun AppPersonalInfoFields(
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String? = null,
    onPhoneChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    firstNameError: String? = null,
    lastNameError: String? = null,
    emailError: String? = null,
    phoneError: String? = null
) {
    AppFormFieldGroup(
        title = "Personal Information",
        description = "Enter your personal details",
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppOutlinedTextField(
                value = firstName,
                onValueChange = onFirstNameChange,
                label = "First Name",
                isError = firstNameError != null,
                validationErrorMessage = firstNameError ?: "",
                modifier = Modifier.weight(1f)
            )
            
            AppOutlinedTextField(
                value = lastName,
                onValueChange = onLastNameChange,
                label = "Last Name",
                isError = lastNameError != null,
                validationErrorMessage = lastNameError ?: "",
                modifier = Modifier.weight(1f)
            )
        }
        
        AppOutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            isError = emailError != null,
            validationErrorMessage = emailError ?: ""
        )
        
        if (phone != null && onPhoneChange != null) {
            AppOutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = "Phone",
                isError = phoneError != null,
                validationErrorMessage = phoneError ?: ""
            )
        }
    }
}

/**
 * Address field group
 */
@Composable
fun AppAddressFields(
    street: String,
    onStreetChange: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit,
    state: String,
    onStateChange: (String) -> Unit,
    zipCode: String,
    onZipCodeChange: (String) -> Unit,
    country: String,
    onCountryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    streetError: String? = null,
    cityError: String? = null,
    stateError: String? = null,
    zipCodeError: String? = null,
    countryError: String? = null
) {
    AppFormFieldGroup(
        title = "Address",
        description = "Enter your address information",
        modifier = modifier
    ) {
        AppOutlinedTextField(
            value = street,
            onValueChange = onStreetChange,
            label = "Street Address",
            isError = streetError != null,
            validationErrorMessage = streetError ?: ""
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppOutlinedTextField(
                value = city,
                onValueChange = onCityChange,
                label = "City",
                isError = cityError != null,
                validationErrorMessage = cityError ?: "",
                modifier = Modifier.weight(1f)
            )
            
            AppOutlinedTextField(
                value = state,
                onValueChange = onStateChange,
                label = "State",
                isError = stateError != null,
                validationErrorMessage = stateError ?: "",
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppOutlinedTextField(
                value = zipCode,
                onValueChange = onZipCodeChange,
                label = "ZIP Code",
                isError = zipCodeError != null,
                validationErrorMessage = zipCodeError ?: "",
                modifier = Modifier.weight(1f)
            )
            
            AppOutlinedTextField(
                value = country,
                onValueChange = onCountryChange,
                label = "Country",
                isError = countryError != null,
                validationErrorMessage = countryError ?: "",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Password field group
 */
@Composable
fun AppPasswordFields(
    currentPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    currentPasswordError: String? = null,
    newPasswordError: String? = null,
    confirmPasswordError: String? = null,
    showCurrentPassword: Boolean = true
) {
    AppFormFieldGroup(
        title = "Password",
        description = "Update your password",
        modifier = modifier
    ) {
        if (showCurrentPassword) {
            AppOutlinedTextField(
                value = currentPassword,
                onValueChange = onCurrentPasswordChange,
                label = "Current Password",
                visualTransformation = PasswordVisualTransformation(),
                isError = currentPasswordError != null,
                validationErrorMessage = currentPasswordError ?: ""
            )
        }
        
        AppOutlinedTextField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            label = "New Password",
            visualTransformation = PasswordVisualTransformation(),
            isError = newPasswordError != null,
            validationErrorMessage = newPasswordError ?: ""
        )
        
        AppOutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Confirm Password",
            visualTransformation = PasswordVisualTransformation(),
            isError = confirmPasswordError != null,
            validationErrorMessage = confirmPasswordError ?: ""
        )
    }
}

/**
 * Contact information field group
 */
@Composable
fun AppContactFields(
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    website: String? = null,
    onWebsiteChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    emailError: String? = null,
    phoneError: String? = null,
    websiteError: String? = null
) {
    AppFormFieldGroup(
        title = "Contact Information",
        description = "Enter your contact details",
        modifier = modifier
    ) {
        AppOutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            isError = emailError != null,
            validationErrorMessage = emailError ?: ""
        )
        
        AppOutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = "Phone",
            isError = phoneError != null,
            validationErrorMessage = phoneError ?: ""
        )
        
        if (website != null && onWebsiteChange != null) {
            AppOutlinedTextField(
                value = website,
                onValueChange = onWebsiteChange,
                label = "Website",
                isError = websiteError != null,
                validationErrorMessage = websiteError ?: ""
            )
        }
    }
}
