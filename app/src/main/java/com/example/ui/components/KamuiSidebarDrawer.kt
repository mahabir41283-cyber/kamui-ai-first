package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatSession
import com.example.ui.theme.*

fun groupSessionsByDate(sessions: List<ChatSession>): Map<String, List<ChatSession>> {
    val now = System.currentTimeMillis()
    val oneDay = 24 * 60 * 60 * 1000L
    // Approximate local day boundary
    val startOfToday = now - (now % oneDay)
    val startOfYesterday = startOfToday - oneDay
    val sevenDaysAgo = startOfToday - (6 * oneDay)
    val thirtyDaysAgo = startOfToday - (29 * oneDay)

    val grouped = linkedMapOf<String, MutableList<ChatSession>>()

    val pinned = sessions.filter { it.isPinned }
    if (pinned.isNotEmpty()) {
        grouped["Pinned"] = pinned.toMutableList()
    }

    val unpinned = sessions.filter { !it.isPinned }
    for (s in unpinned) {
        val time = s.updatedAt
        val bucket = when {
            time >= startOfToday -> "Today"
            time >= startOfYesterday -> "Yesterday"
            time >= sevenDaysAgo -> "Previous 7 Days"
            time >= thirtyDaysAgo -> "Previous 30 Days"
            else -> "Older"
        }
        grouped.getOrPut(bucket) { mutableListOf() }.add(s)
    }

    return grouped
}

@Composable
fun KamuiSidebarDrawer(
    sessions: List<ChatSession>,
    activeSessionId: String,
    isAotesMode: Boolean,
    studentClass: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectSession: (String) -> Unit,
    onNewChat: () -> Unit,
    onOpenNewProject: () -> Unit,
    onTogglePinSession: (String) -> Unit,
    onRenameSession: (String, String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onClearAllSessions: () -> Unit,
    onToggleMode: () -> Unit,
    onOpenStudySetup: () -> Unit,
    onExportJson: () -> Unit,
    onExportTxt: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var sessionToRename by remember { mutableStateOf<ChatSession?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var sessionToDelete by remember { mutableStateOf<String?>(null) }
    var showClearVaultConfirm by remember { mutableStateOf(false) }
    var showVaultTools by remember { mutableStateOf(false) }

    val filteredSessions = remember(sessions, searchQuery) {
        if (searchQuery.isBlank()) sessions
        else sessions.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    val dateGroupedSessions = remember(filteredSessions) {
        groupSessionsByDate(filteredSessions)
    }

    ModalDrawerSheet(
        modifier = modifier.widthIn(max = 320.dp),
        drawerContainerColor = if (isAotesMode) Color(0xFFF8FAFC) else Color(0xFF09090C),
        drawerContentColor = if (isAotesMode) AotesTextPrimary else KamuiTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Header Bar: Logo, Vault Badge, and Close Side Panel Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isAotesMode) {
                        AotesAvatar(size = 28.dp)
                    } else {
                        KamuiAvatar(size = 28.dp, spinMode = SpinMode.SLOW)
                    }

                    Text(
                        text = if (isAotesMode) "AOTES STUDY" else "KAMUI AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (isAotesMode) AotesDarkBlue else KamuiCrimson
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmeraldSuccess.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(10.dp))
                            Text(
                                text = "VAULT",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("close_sidebar_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close sidebar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ChatGPT Action Buttons: New Chat & New Project
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNewChat,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sidebar_new_chat_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAotesMode) AotesPrimaryBlue else KamuiDarkRed
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenNewProject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(KamuiIcons.CreateNewFolder, contentDescription = null, tint = CyberPurple, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New project", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Search History Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search chat history & vault...", fontSize = 11.5.sp) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search", modifier = Modifier.size(16.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear search", modifier = Modifier.size(14.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textStyle = MaterialTheme.typography.bodySmall
            )

            // ChatGPT Style Categorized Chat History (Today, Yesterday, 7 Days, Older)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (filteredSessions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = KamuiIcons.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No chats match '$searchQuery'" else "Private Vault is empty",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    dateGroupedSessions.forEach { (dateGroup, groupSessions) ->
                        item(key = "header_$dateGroup") {
                            Text(
                                text = dateGroup.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (dateGroup == "Pinned") AmberGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp, start = 6.dp)
                            )
                        }

                        items(groupSessions, key = { it.id }) { session ->
                            SessionItemRow(
                                session = session,
                                isActive = session.id == activeSessionId,
                                isAotesMode = isAotesMode,
                                onSelect = { onSelectSession(session.id) },
                                onTogglePin = { onTogglePinSession(session.id) },
                                onRename = {
                                    sessionToRename = session
                                    renameInput = session.title
                                },
                                onDelete = { sessionToDelete = session.id }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            // Private Vault Tools & Exports
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isAotesMode) Color(0xFFF1F5F9) else Color(0xFF14141A),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showVaultTools = !showVaultTools }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(15.dp))
                            Text(
                                text = "Private Vault (${sessions.size} chats)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = if (showVaultTools) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (showVaultTools) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = onExportJson,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Icon(KamuiIcons.FileDownload, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("JSON", fontSize = 10.5.sp)
                            }

                            OutlinedButton(
                                onClick = onExportTxt,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Icon(KamuiIcons.Description, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("TXT Log", fontSize = 10.5.sp)
                            }

                            IconButton(
                                onClick = { showClearVaultConfirm = true },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = KamuiIcons.DeleteSweep,
                                    contentDescription = "Clear all chats",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Footer: Mode Switcher & Curriculum Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = onToggleMode,
                    label = {
                        Text(
                            text = if (isAotesMode) "AOTES Active" else "KAMUI Cyber Dark",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isAotesMode) KamuiIcons.School else KamuiIcons.Visibility,
                            contentDescription = null,
                            tint = if (isAotesMode) AotesPrimaryBlue else KamuiCrimson,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )

                IconButton(
                    onClick = onOpenStudySetup,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Study Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Rename Dialog
    sessionToRename?.let { targetSession ->
        AlertDialog(
            onDismissRequest = { sessionToRename = null },
            title = { Text("Rename Chat") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            onRenameSession(targetSession.id, renameInput.trim())
                            sessionToRename = null
                        }
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    sessionToDelete?.let { sessionId ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Chat") },
            text = { Text("Delete this conversation from your private vault?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSession(sessionId)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Vault Confirmation Dialog
    if (showClearVaultConfirm) {
        AlertDialog(
            onDismissRequest = { showClearVaultConfirm = false },
            title = { Text("Clear All Chat History?") },
            text = { Text("This will permanently remove all conversations from your private vault.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllSessions()
                        showClearVaultConfirm = false
                        Toast.makeText(context, "Vault cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearVaultConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SessionItemRow(
    session: ChatSession,
    isActive: Boolean,
    isAotesMode: Boolean,
    onSelect: () -> Unit,
    onTogglePin: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isActive) {
                    if (isAotesMode) AotesLightBlue else Color(0x33FF1744)
                } else Color.Transparent
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = when {
                    session.isProject -> KamuiIcons.Folder
                    session.isPinned -> KamuiIcons.PushPin
                    else -> KamuiIcons.ChatBubbleOutline
                },
                contentDescription = null,
                tint = when {
                    session.isPinned -> AmberGold
                    session.isProject -> CyberPurple
                    isActive -> if (isAotesMode) AotesPrimaryBlue else KamuiCrimson
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = session.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                color = if (isActive) {
                    if (isAotesMode) AotesDarkBlue else Color.White
                } else MaterialTheme.colorScheme.onSurface
            )
        }

        Box {
            IconButton(
                onClick = { expandedMenu = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = expandedMenu,
                onDismissRequest = { expandedMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(if (session.isPinned) "Unpin" else "Pin to top") },
                    onClick = {
                        onTogglePin()
                        expandedMenu = false
                    },
                    leadingIcon = {
                        Icon(KamuiIcons.PushPin, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rename") },
                    onClick = {
                        onRename()
                        expandedMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        onDelete()
                        expandedMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
    }
}

