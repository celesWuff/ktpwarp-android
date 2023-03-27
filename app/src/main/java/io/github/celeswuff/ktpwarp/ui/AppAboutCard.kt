package io.github.celeswuff.ktpwarp.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.celeswuff.ktpwarp.BuildConfig

@Composable
fun AppAboutCard(modifier: Modifier = Modifier) {
    OutlinedCard(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "关于", style = MaterialTheme.typography.headlineSmall)

            Text("ktpwarp-android ${BuildConfig.VERSION_NAME}")

            val context = LocalContext.current
            val annotatedString = buildAnnotatedString {
                pushStringAnnotation(
                    tag = "github",
                    annotation = "https://github.com/celesWuff/ktpwarp-android"
                )
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("https://github.com/celesWuff/ktpwarp-android")
                }
                pop()
            }

            ClickableText(text = annotatedString, onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "github", start = offset, end = offset)
                    .firstOrNull()?.let {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.item)))
                    }
            })

            Text(
                "copyright (c) 2023 celesWuff, licensed under MIT License",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}