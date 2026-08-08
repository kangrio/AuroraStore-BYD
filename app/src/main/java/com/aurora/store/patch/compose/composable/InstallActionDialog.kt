package com.aurora.store.patch.compose.composable

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aurora.gplayapi.data.models.App
import com.aurora.store.patch.UtilPatch

object InstallActionDialog {
    private var showContinueDialog by mutableStateOf(false)
    private var showDialog = true
    private var requestedApp: App? = null
    private var accountId: String? = null

    private fun isShouldShow(): Boolean {
        return UtilPatch.isBydFlavour() && showDialog
    }

    fun showDialog(app: App, accountId: String? = null): Boolean {
        if (!isShouldShow()) {
            return false
        }
        showContinueDialog = true
        this.requestedApp = app
        this.accountId = accountId
        return true
    }

    @Composable
    fun Dialog(
        onAccept: (App, Boolean, String?) -> Unit = { _, _, _-> }
    ) {
        if (!showContinueDialog || !showDialog) {
            return
        }
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("Accept?")
            },
            text = {
                Text("This app will be modified to add MicroG compatibility.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        onAccept(requestedApp!!, false, accountId)
                        showDialog = true
                        showContinueDialog = false
                    }
                ) {
                    Text("Accept")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showContinueDialog = false
                    }
                ) {
                    Text("Cancel")
                    showDialog = true
                }
            }
        )
    }
}