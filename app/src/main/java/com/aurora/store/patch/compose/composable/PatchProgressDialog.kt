package com.aurora.store.patch.compose.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aurora.store.patch.state.StepStatus
import com.aurora.store.patch.util.Patcher
import kotlinx.coroutines.delay

@Composable
fun PatchProgressDialog(packageName: String, displayName: String) {
    val state = Patcher.progressState
    if (!state.isVisible || packageName != state.packageName) return

    // Auto-dismiss: short delay on success, longer delay on error so it's readable
    LaunchedEffect(state.isComplete, state.isError) {
        when {
            state.isComplete -> {
                delay(1000)
                state.dismiss()
            }
            state.isError -> {
                delay(5000)
                state.dismiss()
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            // Allow tap-outside/back dismiss once finished or failed; block while running
            if (state.isComplete || state.isError) state.dismiss()
        },
        title = {
            Text(
                when {
                    state.isError -> "$displayName: Supporting MicroG Failed"
                    state.isComplete -> "$displayName: Supporting MicroG Complete"
                    else -> "$displayName: Supporting MicroG..."
                }
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!state.isComplete && !state.isError) {
                    LinearProgressIndicator(
                        progress = {
                            val done = state.steps.count { it.status == StepStatus.DONE }
                            if (state.steps.isEmpty()) 0f else done / state.steps.size.toFloat()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                }

                state.steps.forEach { step ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (step.status) {
                            StepStatus.DONE -> Icon(
                                Icons.Filled.CheckCircle, contentDescription = null,
                                tint = Color(0xFF4CAF50)
                            )
                            StepStatus.FAILED -> Icon(
                                Icons.Filled.Error, contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            StepStatus.RUNNING -> CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            StepStatus.PENDING -> Icon(
                                Icons.Filled.RadioButtonUnchecked, contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(
                            step.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (step.status == StepStatus.PENDING)
                                MaterialTheme.colorScheme.outline
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (state.isError && state.errorMessage != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        state.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else if (!state.isComplete && !state.isError) {
                    Text(
                        state.currentMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = null
    )
}