package com.aurora.store.patch.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

enum class StepStatus { PENDING, RUNNING, DONE, FAILED }

data class PatchStep(
    val label: String,
    val status: StepStatus = StepStatus.PENDING
)

class PatchProgressState {
    var isVisible by mutableStateOf(false)
        private set
    var currentMessage by mutableStateOf("")
        private set
    var isComplete by mutableStateOf(false)
        private set
    var isError by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var packageName by mutableStateOf("")

    val steps = mutableStateListOf<PatchStep>()

    fun start(pkg: String, stepLabels: List<String>) {
        packageName = pkg
        steps.clear()
        steps.addAll(stepLabels.map { PatchStep(it) })
        currentMessage = "Starting..."
        isComplete = false
        isError = false
        errorMessage = null
        isVisible = true
    }

    fun beginStep(index: Int, message: String = steps[index].label) {
        if (index !in steps.indices) return
        steps[index] = steps[index].copy(status = StepStatus.RUNNING)
        currentMessage = message
    }

    fun completeStep(index: Int) {
        if (index !in steps.indices) return
        steps[index] = steps[index].copy(status = StepStatus.DONE)
    }

    fun failStep(index: Int, error: String) {
        if (index in steps.indices) {
            steps[index] = steps[index].copy(status = StepStatus.FAILED)
        }
        isError = true
        errorMessage = error
    }

    fun finish() {
        isComplete = true
        currentMessage = "Done"
    }

    fun dismiss() {
        isVisible = false
    }
}