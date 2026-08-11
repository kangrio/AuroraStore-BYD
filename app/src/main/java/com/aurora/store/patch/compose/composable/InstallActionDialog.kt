package com.aurora.store.patch.compose.composable

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aurora.gplayapi.data.models.App
import com.aurora.store.patch.UtilPatch
import com.aurora.store.util.Preferences

object InstallActionDialog {
    private const val PREFERENCES_ALWAYS_ACCEPT_MODIFY = "PREFERENCES_ALWAYS_ACCEPT_MODIFY"
    private var showAcceptDialog by mutableStateOf(false)
    private var showDialog = true
    private var requestedApp: App? = null
    private var accountId: String? = null

    private fun isShouldShow(context: Context): Boolean {
        val alwaysAccept = Preferences.getBoolean(
            context,
            PREFERENCES_ALWAYS_ACCEPT_MODIFY,
            false
        )
        return UtilPatch.isBydFlavour() && showDialog && !alwaysAccept
    }

    fun showDialog(context: Context, app: App, accountId: String? = null): Boolean {
        if (!isShouldShow(context)) {
            return false
        }
        showAcceptDialog = true
        this.requestedApp = app
        this.accountId = accountId
        return true
    }

    @Composable
    fun Dialog(
        onAccept: (App, Boolean, String?) -> Unit = { _, _, _ -> }
    ) {
        if (!showAcceptDialog || !showDialog) return

        fun accept() {
            showDialog = false
            showAcceptDialog = false
            onAccept(requestedApp!!, false, accountId)
            showDialog = true
        }

        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = {
                showAcceptDialog = false
                showDialog = true
            },
            title = {
                Text("Accept?")
            },
            text = {
                Column {
                    Text("This app will be modified to add MicroG compatibility.")
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showAcceptDialog = false
                                showDialog = true
                            }
                        ) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))

                        FilledTonalButton(
                            onClick = {
                                accept()
                            }
                        ) {
                            Text("Accept")
                        }
                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = {
                                Preferences.putBoolean(
                                    context,
                                    PREFERENCES_ALWAYS_ACCEPT_MODIFY,
                                    true
                                )
                                accept()
                            }
                        ) {
                            Text("Always")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}