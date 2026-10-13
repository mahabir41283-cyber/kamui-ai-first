package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChatMessage
import com.example.ui.theme.*

val EMOJI_REACTIONS = listOf("🔥", "💯", "✨", "😂", "💀", "🤔", "🤝", "📚")

@Composable
fun MessageBubble(
    message: ChatMessage,
    isAotesMode: Boolean,
    onReactionToggle: (String) -> Unit,
    onResend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showReactionPicker by remember { mutableStateOf(false) }
    val isUser = message.role == "user"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.92f else 0.95f),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isUser) {
                if (isAotesMode) {
                    AotesAvatar(size = 32.dp, modifier = Modifier.padding(bottom = 4.dp, end = 8.dp))
                } else {
                    KamuiAvatar(size = 32.dp, spinMode = SpinMode.SLOW, modifier = Modifier.padding(bottom = 4.dp, end = 8.dp))
                }
            }

            // Bubble container
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                color = if (isUser) {
                    if (isAotesMode) AotesPrimaryBlue else KamuiDarkRed
                } else {
                    if (isAotesMode) AotesSurface else KamuiDarkCard
                },
                shadowElevation = if (isAotesMode) 2.dp else 6.dp,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .border(
                        width = 1.dp,
                        color = if (isUser) {
                            if (isAotesMode) AotesPrimaryBlue.copy(alpha = 0.5f) else KamuiCrimson.copy(alpha = 0.4f)
                        } else {
                            if (isAotesMode) AotesCardBorder else KamuiNeonBorder
                        },
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp
                        )
                    )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Attached Image
                    message.imageUri?.let { uriStr ->
                        AsyncImage(
                            model = uriStr,
                            contentDescription = "Attached Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .padding(bottom = 8.dp)
                        )
                    }

                    // Content Formatted Parsing (handles code blocks and text)
                    FormattedMessageContent(
                        content = message.text,
                        isUser = isUser,
                        isAotesMode = isAotesMode
                    )

                    // Message Action Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // React Icon Button
                        IconButton(
                            onClick = { showReactionPicker = !showReactionPicker },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = KamuiIcons.AddReaction,
                                contentDescription = "Add reaction",
                                tint = if (isUser) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Copy Button
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("kamui_message", message.text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = KamuiIcons.ContentCopy,
                                contentDescription = "Copy text",
                                tint = if (isUser) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Share Button
                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, message.text)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share with..."))
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Share text",
                                tint = if (isUser) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        if (isUser) {
                            // Resend Button
                            IconButton(
                                onClick = onResend,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Resend",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Reactions List
        if (message.reactions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(start = if (isUser) 0.dp else 40.dp, top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                message.reactions.forEach { (emoji, count) ->
                    Surface(
                        shape = CircleShape,
                        color = if (isAotesMode) Color(0xFFE2E8F0) else Color(0xFF27272A),
                        modifier = Modifier
                            .clickable { onReactionToggle(emoji) }
                    ) {
                        Text(
                            text = "$emoji $count",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Floating Reaction Picker
        AnimatedVisibility(visible = showReactionPicker) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isAotesMode) Color.White else Color(0xFF1E1E24),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .padding(top = 4.dp, start = if (isUser) 0.dp else 40.dp)
                    .border(
                        1.dp,
                        if (isAotesMode) Color(0xFFCBD5E1) else Color(0x33FF1744),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EMOJI_REACTIONS.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clickable {
                                    onReactionToggle(emoji)
                                    showReactionPicker = false
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormattedMessageContent(
    content: String,
    isUser: Boolean,
    isAotesMode: Boolean
) {
    val textColor = if (isUser) {
        Color.White
    } else {
        if (isAotesMode) AotesTextPrimary else KamuiTextPrimary
    }

    // Split text into code blocks and normal markdown segments
    val parts = remember(content) { parseMarkdownCodeBlocks(content) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        parts.forEach { part ->
            when (part) {
                is ContentPart.Code -> {
                    CodeBlockView(
                        code = part.code,
                        language = part.language,
                        isAotesMode = isAotesMode
                    )
                }
                is ContentPart.Text -> {
                    Text(
                        text = part.text,
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp
                    )
                }
            }
        }
    }
}

sealed class ContentPart {
    data class Text(val text: String) : ContentPart()
    data class Code(val code: String, val language: String) : ContentPart()
}

fun parseMarkdownCodeBlocks(raw: String): List<ContentPart> {
    val result = mutableListOf<ContentPart>()
    val regex = "```([a-zA-Z0-9_-]*)\\n?([\\s\\S]*?)```".toRegex()
    var currentIndex = 0

    val matches = regex.findAll(raw)
    for (match in matches) {
        val range = match.range
        if (range.first > currentIndex) {
            val textBefore = raw.substring(currentIndex, range.first).trim()
            if (textBefore.isNotEmpty()) {
                result.add(ContentPart.Text(textBefore))
            }
        }
        val lang = match.groupValues[1].ifBlank { "code" }
        val code = match.groupValues[2].trimEnd()
        result.add(ContentPart.Code(code = code, language = lang))
        currentIndex = range.last + 1
    }

    if (currentIndex < raw.length) {
        val remaining = raw.substring(currentIndex).trim()
        if (remaining.isNotEmpty()) {
            result.add(ContentPart.Text(remaining))
        }
    }

    if (result.isEmpty()) {
        result.add(ContentPart.Text(raw))
    }
    return result
}
