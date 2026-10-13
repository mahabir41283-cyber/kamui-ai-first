package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.EmeraldSuccess
import kotlinx.coroutines.delay

import com.example.ui.theme.JetBrainsMono

@Composable
fun CodeBlockView(
    code: String,
    language: String = "code",
    isAotesMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    val bgColor = if (isAotesMode) Color(0xFF1E293B) else Color(0xFF0F0F14)
    val borderColor = if (isAotesMode) Color(0xFF334155) else Color(0x33FF1744)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        // Header bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor.copy(alpha = 0.8f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isAotesMode) Color(0xFF94A3B8) else CyberPurple,
                fontFamily = JetBrainsMono,
                fontSize = 11.sp
            )

            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
                    copied = true
                    Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(28.dp)
                    .testTag("copy_code_button")
            ) {
                Icon(
                    imageVector = if (copied) Icons.Rounded.Check else KamuiIcons.ContentCopy,
                    contentDescription = "Copy code",
                    tint = if (copied) EmeraldSuccess else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        HorizontalDivider(color = borderColor.copy(alpha = 0.5f), thickness = 0.5.dp)

        // Code Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = code,
                fontFamily = JetBrainsMono,
                fontSize = 12.5.sp,
                color = Color(0xFFE2E8F0),
                lineHeight = 18.sp
            )
        }
    }
}
