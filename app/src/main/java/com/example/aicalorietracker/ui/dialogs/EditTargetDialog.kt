package com.example.aicalorietracker.ui.dialogs

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun EditTargetDialog(
    currentTarget: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit
) {
    var text by remember { mutableStateOf(currentTarget.toString()) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Set Daily Goal") }, text = {
        TextField(
            value = text,
            onValueChange = { if (it.all { char -> char.isDigit() }) text = it },
            label = { Text("Calories") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )
    }, confirmButton = {
        TextButton(onClick = {
            val newTarget = text.toIntOrNull()
            if (newTarget != null) onConfirm(newTarget)
        }) { Text("Save") }
    }, dismissButton = {
        TextButton(onClick = onDismiss) { Text("Cancel") }
    })
}