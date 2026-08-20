package com.aurora.store.patch.compose.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aurora.store.patch.ConstantsPatch
import com.aurora.store.util.Preferences
import com.aurora.store.util.save

object WelcomeDialog {
    private var hideDialog: MutableState<Boolean>? = null

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Dialog() {
        val context = LocalContext.current
        hideDialog = remember {
            hideDialog ?: mutableStateOf(
                Preferences.getBoolean(context, ConstantsPatch.HIDE_SHOW_WELCOME_DIALOG)
            )
        }

        val dontShowDialog by remember { hideDialog!! }
        if (dontShowDialog) return

        val uriHandler = LocalUriHandler.current
        var dontShowAgain by remember { mutableStateOf(false) }

        BasicAlertDialog(onDismissRequest = {  }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    // Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Welcome to Aurora Store (BYD Variant)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "This build is a community fork adapted to run on BYD Android " +
                                "(AOSP) head units. Before you continue, please read where " +
                                "this app comes from and what it does.",
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(Modifier.height(20.dp))
                    SectionHeader(icon = Icons.Filled.Code, title = "Source")
                    InfoRow(
                        label = "This fork",
                        value = "AuroraStore-BYD, maintained by KangRio",
                    )
                    InfoRow(
                        label = "Upstream",
                        value = "Aurora Store, by whyorean (AuroraOSS)",
                    )
                    InfoRow(
                        label = "License",
                        value = "GPLv3 — free and open source software",
                    )
                    LinkRow("This fork: https://github.com/kangrio/AuroraStore-BYD", "https://github.com/kangrio/AuroraStore-BYD", uriHandler)
                    LinkRow("Original: https://github.com/whyorean/AuroraStore", "https://github.com/whyorean/AuroraStore", uriHandler)
                    LinkRow("Official Site: https://auroraoss.com/", "https://auroraoss.com/", uriHandler)

                    Spacer(Modifier.height(20.dp))
                    SectionHeader(icon = Icons.Filled.Policy, title = "This BYD Build")
                    Text(
                        text = "Packaged and signed independently for BYD head units, with rootless " +
                                "microG/GmsCore patching so apps run without root access. See the " +
                                "README.md in the repository for build-specific notes and known " +
                                "limitations on this platform.",
                        style = MaterialTheme.typography.bodySmall,
                    )

                    Spacer(Modifier.height(24.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Checkbox(
                            checked = dontShowAgain,
                            onCheckedChange = { dontShowAgain = it },
                        )
                        Text(
                            text = "Don't show this again",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            context.save(
                                ConstantsPatch.HIDE_SHOW_WELCOME_DIALOG,
                                dontShowAgain,
                            )
                            hideDialog?.value = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                    ) {
                        Text("Got it, continue")
                    }
                }
            }
        }
    }

    @Composable
    private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(8.dp))
    }

    @Composable
    private fun InfoRow(label: String, value: String) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(140.dp),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

    @Composable
    private fun LinkRow(label: String, url: String, uriHandler: androidx.compose.ui.platform.UriHandler) {
        Text(
            text = "• $label",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.primary,
            ),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .padding(vertical = 2.dp)
                .clickable { uriHandler.openUri(url) },
        )
    }
}