package com.flixclusive.feature.mobile.provider.info.component.author

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.flixclusive.core.ui.common.util.ifElse
import com.flixclusive.core.ui.mobile.component.ImageWithSmallPlaceholder
import com.flixclusive.feature.mobile.provider.info.SUB_LABEL_SIZE
import com.flixclusive.gradle.entities.Author
import com.flixclusive.core.ui.common.R as UiCommonR
import com.flixclusive.core.util.R as UtilR

@Composable
internal fun AuthorCard(
    modifier: Modifier = Modifier,
    author: Author,
) {
    val uriHandler = LocalUriHandler.current
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .ifElse(
                condition = author.socialLink != null,
                ifTrueModifier = Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        author.socialLink?.let(uriHandler::openUri)
                    }
                )
            )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.CenterStart)
        ) {
            val avatarSize = 45.dp
            ImageWithSmallPlaceholder(
                modifier = Modifier
                    .size(avatarSize)
                    .ifElse(
                        condition = author.socialLink != null,
                        ifTrueModifier = Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = rememberRipple(
                                bounded = false,
                                radius = avatarSize / 2
                            ),
                            onClick = {
                                author.socialLink?.let(uriHandler::openUri)
                            }
                        )
                    ),
                placeholderModifier = Modifier.fillMaxSize(),
                urlImage = "${author.socialLink}.png",
                placeholderId = UiCommonR.drawable.profile_placeholder,
                contentDescId = UtilR.string.author_icon_content_desc,
            )

            Text(
                text = author.name,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = SUB_LABEL_SIZE
                )
            )
        }
    }
}