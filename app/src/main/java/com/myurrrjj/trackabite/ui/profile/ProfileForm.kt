package com.myurrrjj.trackabite.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.myurrrjj.trackabite.Misc.BmrCalculator
import com.myurrrjj.trackabite.local.Sex
import com.myurrrjj.trackabite.local.UserProfile
import com.myurrrjj.trackabite.ui.Utils.ExpressiveTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileForm(
    initial: UserProfile?,
    submitLabel: String,
    onSubmit: (UserProfile) -> Unit,
    modifier: Modifier = Modifier,
    onSkip: (() -> Unit)? = null
) {
    var weight by remember { mutableStateOf(initial?.weightKg?.let(::trimNumber) ?: "") }
    var height by remember { mutableStateOf(initial?.heightCm?.let(::trimNumber) ?: "") }
    var age by remember { mutableStateOf(initial?.age?.toString() ?: "") }
    var sex by remember { mutableStateOf(initial?.sex) }

    val candidate: UserProfile? = run {
        val w = weight.toFloatOrNull()
        val h = height.toFloatOrNull()
        val a = age.toIntOrNull()
        val s = sex
        if (w != null && h != null && a != null && s != null) UserProfile(w, h, a, s) else null
    }
    val isValid = candidate?.isComplete == true
    val bmrPreview = candidate?.takeIf { it.isComplete }?.let(BmrCalculator::bmrCalc)

    Column(modifier = modifier) {
        ExpressiveTextField(
            value = weight,
            onValueChange = { input -> weight = input.filter { it.isDigit() || it == '.' } },
            label = "Weight (kg)",
            leadingIcon = Icons.Rounded.MonitorWeight,
            keyboardType = KeyboardType.Decimal
        )
        Spacer(Modifier.height(14.dp))
        ExpressiveTextField(
            value = height,
            onValueChange = { input -> height = input.filter { it.isDigit() || it == '.' } },
            label = "Height (cm)",
            leadingIcon = Icons.Rounded.Straighten,
            keyboardType = KeyboardType.Decimal
        )
        Spacer(Modifier.height(14.dp))
        ExpressiveTextField(
            value = age,
            onValueChange = { input -> age = input.filter { it.isDigit() } },
            label = "Age",
            leadingIcon = Icons.Rounded.Cake,
            keyboardType = KeyboardType.Number
        )

        Spacer(Modifier.height(20.dp))
        Text(
            text = "Sex",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = sex == Sex.MALE,
                onClick = { sex = Sex.MALE },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                label = { Text("Male") }
            )
            SegmentedButton(
                selected = sex == Sex.FEMALE,
                onClick = { sex = Sex.FEMALE },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                label = { Text("Female") }
            )
        }

        AnimatedVisibility(
            visible = bmrPreview != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(Modifier.height(20.dp))
                BmrPreviewCard(kcal = bmrPreview ?: 0)
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { candidate?.let(onSubmit) },
            enabled = isValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(22.dp)
        ) {
            Text(submitLabel, fontWeight = FontWeight.Bold)
        }

        if (onSkip != null) {
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text("Skip for now")
            }
        }
    }
}

@Composable
private fun BmrPreviewCard(kcal: Int) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = "Estimated basal burn",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "≈ $kcal kcal/day at rest",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

private fun trimNumber(v: Float): String =
    if (v % 1f == 0f) v.toInt().toString() else v.toString()