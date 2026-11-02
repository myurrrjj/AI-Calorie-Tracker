package com.myurrrjj.trackabite.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.myurrrjj.trackabite.local.Sex
import com.myurrrjj.trackabite.local.UserProfile
import kotlin.math.max

class TargetFormState(
    initialCal: String, initialCarb: String, initialFat: String,
    initialFib: String, initialPro: String, initialSug: String
) {
    var cal by mutableStateOf(initialCal)
    var carb by mutableStateOf(initialCarb)
    var fat by mutableStateOf(initialFat)
    var fib by mutableStateOf(initialFib)
    var pro by mutableStateOf(initialPro)
    var sug by mutableStateOf(initialSug)

    fun applyScientificDefaults(targetCals: Int, profile: UserProfile?) {
        val weight = profile?.weightKg ?: 60f
        val requiredProteinMin = (weight * 0.8f).toInt()

        carb = (targetCals * 0.50f / 4f).toInt().toString()
        fat = (targetCals * 0.30f / 9f).toInt().toString()
        pro = max((targetCals * 0.20f / 4f).toInt(), requiredProteinMin).toString()
        sug = (targetCals * 0.10f / 4f).toInt().toString()

        fib = if (profile != null) {
            when (profile.sex) {
                Sex.MALE -> if (profile.age <= 50) "38" else "30"
                Sex.FEMALE -> if (profile.age <= 50) "25" else "21"
            }
        } else {
            (targetCals / 1000f * 14f).toInt().toString()
        }
    }
}

@Composable
fun EditTargetDialog(
    targetCalories: Int,
    targetCarbs: Int,
    targetFat: Int,
    targetFiber: Int,
    targetProtein: Int,
    targetSugar: Int,
    userProfile: UserProfile?,
    onDismiss: () -> Unit,
    onConfirm: (cal: Int, pro: Int, carb: Int, fat: Int, fib: Int, sug: Int) -> Unit
) {
    val formState = remember {
        TargetFormState(
            targetCalories.toString(), targetCarbs.toString(), targetFat.toString(),
            targetFiber.toString(), targetProtein.toString(), targetSugar.toString()
        )
    }

    val userWeight = userProfile?.weightKg ?: 60f
    val requiredProteinMin = (userWeight * 0.8f).toInt()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Daily Targets",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Set your goals or let science calculate them.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = formState.cal,
                    onValueChange = { formState.cal = it.filter { char -> char.isDigit() } },
                    label = { Text("Total Calories (kcal)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                FilledTonalButton(
                    onClick = { formState.applyScientificDefaults(formState.cal.toIntOrNull() ?: 2000, userProfile) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto-calculate macros", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MacroInput("Protein (g)", formState.pro, Modifier.weight(1f)) { formState.pro = it }
                    MacroInput("Carbs (g)", formState.carb, Modifier.weight(1f)) { formState.carb = it }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MacroInput("Fat (g)", formState.fat, Modifier.weight(1f)) { formState.fat = it }
                    MacroInput("Fiber (g)", formState.fib, Modifier.weight(1f)) { formState.fib = it }
                }
                Spacer(modifier = Modifier.height(12.dp))
                MacroInput("Sugar Limit (g)", formState.sug, Modifier.fillMaxWidth()) { formState.sug = it }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(
                                formState.cal.toIntOrNull() ?: 2000,
                                formState.pro.toIntOrNull() ?: requiredProteinMin,
                                formState.carb.toIntOrNull() ?: 130,
                                formState.fat.toIntOrNull() ?: 20,
                                formState.fib.toIntOrNull() ?: 25,
                                formState.sug.toIntOrNull() ?: 50
                            )
                        },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Save Targets", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MacroInput(label: String, value: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter { char -> char.isDigit() }) },
        label = { Text(label, fontSize = 13.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    )
}