package com.cbo.ui.components

import android.util.Log
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.cbo.ui.R
import com.cbo.ui.theme.MemCloudApplicationTheme

@Composable
fun PhoneNumber(
    modifier: Modifier = Modifier,
    phoneNumber: String = "",
    onPhoneNumberChange: (String) -> Unit,
    phoneNumberError: String = stringResource(R.string.phone_number_error),
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val numericRegex = Regex("[^0-9]")

    AppOutlinedTextField(
        modifier = modifier,
        value = phoneNumber,
        onValueChange = {
            Log.d("PhoneNumber", "onValueChange: $it")
            // Remove non-numeric characters.
            val stripped = numericRegex.replace(it, "")
            onPhoneNumberChange(
                if (stripped.length >= 10) {
                    Log.d("PhoneNumber", "onValueChange if: $stripped")
                    stripped.substring(0..9)
                } else {
                    Log.d("PhoneNumber", "onValueChange else: $stripped")
                    stripped
                }
            )
        },
        isError = phoneNumber.length < 10 && phoneNumber.isNotEmpty(),
        isValid = phoneNumber.length == 10 || phoneNumber.isEmpty(),
        validationErrorMessage = phoneNumberError,
        label = stringResource(R.string.phone_number),
        visualTransformation = NanpVisualTransformation(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}

class NanpVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(10)

        val out = buildString {
            if (trimmed.isNotEmpty()) append("(")
            for (i in trimmed.indices) {
                when (i) {
                    3 -> append(") ")
                    6 -> append(" ")
                    8 -> append(" ")
                }
                append(trimmed[i])
            }
        }

        return TransformedText(AnnotatedString(out), PhoneOffsetMapping)
    }

    private object PhoneOffsetMapping : OffsetMapping {

        override fun originalToTransformed(offset: Int): Int = when {
            offset <= 0 -> 0
            offset <= 3 -> offset + 1        // "("
            offset <= 6 -> offset + 3        // ") "
            offset <= 8 -> offset + 4        // space after 456
            offset <= 10 -> offset + 5       // space after 78
            else -> 15                       // max
        }

        override fun transformedToOriginal(offset: Int): Int = when {
            offset <= 1 -> 0                 // '('
            offset <= 4 -> offset - 1        // after ")"
            offset <= 8 -> offset - 3        // after ") "
            offset <= 11 -> offset - 4       // after 456 + space
            offset <= 14 -> offset - 5       // after 78 + space
            else -> 10                       // cap
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PhoneNumber_Preview() {
     MemCloudApplicationTheme {
        var phone by rememberSaveable { mutableStateOf("") }

        PhoneNumber(
            phoneNumber = phone,
            onPhoneNumberChange = { phone = it }
        )
    }
}
