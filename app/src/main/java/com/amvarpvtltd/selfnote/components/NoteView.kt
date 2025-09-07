package com.amvarpvtltd.selfnote.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amvarpvtltd.selfnote.design.NoteTheme
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.Material3RichText

@Composable
fun NoteView(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = NoteTheme.OnSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content with markdown support
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = NoteTheme.Surface
        ) {
            Material3RichText(
                modifier = Modifier.padding(16.dp)
            ) {
                Markdown(content)
            }
        }
    }
}
