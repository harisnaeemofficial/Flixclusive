package com.flixclusive.feature.mobile.settings.settings_group

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.flixclusive.feature.mobile.settings.KEY_DOH_DIALOG
import com.flixclusive.feature.mobile.settings.SettingsItem
import com.flixclusive.core.util.R as UtilR

@Composable
internal fun currentNetworkSettings(
    onOpenUserAgentDialog: () -> Unit
): List<SettingsItem> {
    return listOf(
        SettingsItem(
            title = stringResource(UtilR.string.doh),
            description = stringResource(UtilR.string.dns_label),
            dialogKey = KEY_DOH_DIALOG,
        ),
        SettingsItem(
            title = stringResource(UtilR.string.default_user_agent),
            description = stringResource(UtilR.string.default_user_agent_description),
            onClick = onOpenUserAgentDialog
        ),
    )
}