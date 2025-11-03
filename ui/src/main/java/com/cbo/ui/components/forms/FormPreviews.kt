package com.cbo.ui.components.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cbo.ui.components.AppOutlinedTextField
import com.cbo.ui.theme.MemCloudApplicationTheme

@Preview(showBackground = true, name = "Form Field Group")
@Composable
fun FormFieldGroupPreview() {
    MemCloudApplicationTheme {
        AppFormFieldGroup(
            title = "User Information",
            description = "Enter your personal details"
        ) {
            AppOutlinedTextField(
                value = "",
                onValueChange = { },
                label = "Name",
                placeholder = "Enter your name"
            )
            
            AppOutlinedTextField(
                value = "",
                onValueChange = { },
                label = "Email",
                placeholder = "Enter your email"
            )
        }
    }
}

@Preview(showBackground = true, name = "Personal Info Fields")
@Composable
fun PersonalInfoFieldsPreview() {
    MemCloudApplicationTheme {
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        
        AppPersonalInfoFields(
            firstName = firstName,
            onFirstNameChange = { firstName = it },
            lastName = lastName,
            onLastNameChange = { lastName = it },
            email = email,
            onEmailChange = { email = it },
            phone = phone,
            onPhoneChange = { phone = it }
        )
    }
}

@Preview(showBackground = true, name = "Address Fields")
@Composable
fun AddressFieldsPreview() {
    MemCloudApplicationTheme {
        var street by remember { mutableStateOf("") }
        var city by remember { mutableStateOf("") }
        var state by remember { mutableStateOf("") }
        var zipCode by remember { mutableStateOf("") }
        var country by remember { mutableStateOf("") }
        
        AppAddressFields(
            street = street,
            onStreetChange = { street = it },
            city = city,
            onCityChange = { city = it },
            state = state,
            onStateChange = { state = it },
            zipCode = zipCode,
            onZipCodeChange = { zipCode = it },
            country = country,
            onCountryChange = { country = it }
        )
    }
}

@Preview(showBackground = true, name = "Password Fields")
@Composable
fun PasswordFieldsPreview() {
    MemCloudApplicationTheme {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        
        AppPasswordFields(
            currentPassword = currentPassword,
            onCurrentPasswordChange = { currentPassword = it },
            newPassword = newPassword,
            onNewPasswordChange = { newPassword = it },
            confirmPassword = confirmPassword,
            onConfirmPasswordChange = { confirmPassword = it }
        )
    }
}

@Preview(showBackground = true, name = "Contact Fields")
@Composable
fun ContactFieldsPreview() {
    MemCloudApplicationTheme {
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var website by remember { mutableStateOf("") }
        
        AppContactFields(
            email = email,
            onEmailChange = { email = it },
            phone = phone,
            onPhoneChange = { phone = it },
            website = website,
            onWebsiteChange = { website = it }
        )
    }
}

@Preview(showBackground = true, name = "Form Actions")
@Composable
fun FormActionsPreview() {
    MemCloudApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppFormActions(
                onSave = { },
                onCancel = { },
                saveText = "Save Changes",
                cancelText = "Cancel"
            )
            
            AppFormActions(
                onSave = { },
                onCancel = { },
                saveText = "Create Account",
                cancelText = "Cancel",
                showCancel = false
            )
        }
    }
}

@Preview(showBackground = true, name = "Form Container")
@Composable
fun FormContainerPreview() {
    MemCloudApplicationTheme {
        AppFormContainer {
            AppFormSection(
                title = "Account Settings",
                description = "Manage your account preferences"
            ) {
                AppOutlinedTextField(
                    value = "",
                    onValueChange = { },
                    label = "Display Name",
                    placeholder = "Enter display name"
                )
                
                AppOutlinedTextField(
                    value = "",
                    onValueChange = { },
                    label = "Bio",
                    placeholder = "Tell us about yourself",
                    maxLines = 3
                )
            }
        }
    }
}



