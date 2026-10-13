package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.StudyPromptItem
import com.example.domain.KamuiPersona
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KamuiScreen(
    viewModel: KamuiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val sessions by viewModel.sessions.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isAotesMode by viewModel.isAotesMode.collectAsState()
    val studentClass by viewModel.studentClass.collectAsState()
    val favoriteSubjects by viewModel.favoriteSubjects.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val attachedImageUri by viewModel.attachedImageUri.collectAsState()
    val isImageAiMode by viewModel.isImageAiMode.collectAsState()
    val isAiEditMode by viewModel.isAiEditMode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showStudySetupDialog by remember { mutableStateOf(false) }
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }

    // Photo picker launcher (Zero-permission Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setAttachedImage(uri)
            Toast.makeText(context, "Image attached! AI Edit active.", Toast.LENGTH_SHORT).show()
        }
    }

    val listState = rememberLazyListState()

    // Auto-scroll to latest message
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    KamuiTheme(isAotesMode = isAotesMode) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                KamuiSidebarDrawer(
                    sessions = sessions,
                    activeSessionId = activeSessionId,
                    isAotesMode = isAotesMode,
                    studentClass = studentClass,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                    onSelectSession = {
                        viewModel.selectSession(it)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onNewChat = {
                        viewModel.startNewChat()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenNewProject = {
                        showNewProjectDialog = true
                        coroutineScope.launch { drawerState.close() }
                    },
                    onTogglePinSession = { viewModel.togglePinSession(it) },
                    onRenameSession = { id, title -> viewModel.renameSession(id, title) },
                    onDeleteSession = { viewModel.deleteSession(it) },
                    onClearAllSessions = { viewModel.clearAllVaultSessions() },
                    onToggleMode = { viewModel.toggleMode() },
                    onOpenStudySetup = { showStudySetupDialog = true },
                    onExportJson = {
                        coroutineScope.launch {
                            val json = viewModel.getExportJson()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, json)
                                type = "application/json"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export Vault JSON"))
                        }
                    },
                    onExportTxt = {
                        coroutineScope.launch {
                            val txt = viewModel.getExportTxt()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, txt)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export Vault TXT"))
                        }
                    },
                    onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isAotesMode) {
                                    AotesAvatar(size = 32.dp)
                                    Column {
                                        Text(
                                            text = "AOTES",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 17.sp,
                                            color = AotesDarkBlue
                                        )
                                        Text(
                                            text = studentClass,
                                            fontSize = 11.sp,
                                            color = AotesPrimaryBlue,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else {
                                    KamuiAvatar(size = 32.dp, spinMode = if (isLoading) SpinMode.FAST else SpinMode.SLOW)
                                    Column {
                                        Text(
                                            text = "KAMUI",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 17.sp,
                                            color = KamuiCrimson
                                        )
                                        Text(
                                            text = "Cyber Dimension AI",
                                            fontSize = 11.sp,
                                            color = KamuiTextSecondary
                                        )
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                // Slide bar toggle button (ChatGPT style)
                                IconButton(
                                    onClick = { coroutineScope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("menu_drawer_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Menu,
                                        contentDescription = "Open sidebar (Chat history)",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Quick New Chat button beside slide bar toggle (ChatGPT style)
                                IconButton(
                                    onClick = { viewModel.startNewChat() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("topbar_new_chat_button")
                                ) {
                                    Icon(
                                        imageVector = KamuiIcons.AddComment,
                                        contentDescription = "New chat",
                                        tint = if (isAotesMode) AotesPrimaryBlue else KamuiCrimson,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        actions = {
                            // Mode Switch Toggle
                            FilledTonalButton(
                                onClick = { viewModel.toggleMode() },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("mode_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isAotesMode) KamuiIcons.VisibilityOff else KamuiIcons.School,
                                    contentDescription = null,
                                    tint = if (isAotesMode) KamuiCrimson else AotesPrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAotesMode) "Kamui Dark" else "Notes (Study)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isAotesMode) {
                                IconButton(onClick = { showStudySetupDialog = true }) {
                                    Icon(
                                        imageVector = KamuiIcons.Tune,
                                        contentDescription = "Curriculum settings",
                                        tint = AotesPrimaryBlue
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    ChatInputBar(
                        inputText = inputText,
                        onInputTextChange = { viewModel.onInputTextChange(it) },
                        attachedImageUri = attachedImageUri,
                        onRemoveImage = { viewModel.setAttachedImage(null) },
                        isImageAiMode = isImageAiMode,
                        isAiEditMode = isAiEditMode,
                        isAotesMode = isAotesMode,
                        studentClass = studentClass,
                        isLoading = isLoading,
                        onSend = { viewModel.sendMessage() },
                        onOpenAttachmentMenu = { showAttachmentMenu = true }
                    )
                },
                modifier = modifier.fillMaxSize()
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (messages.isEmpty()) {
                        // Empty State with dynamic suggestions
                        EmptyStateView(
                            isAotesMode = isAotesMode,
                            studentClass = studentClass,
                            favoriteSubjects = favoriteSubjects,
                            onSelectPrompt = { prompt ->
                                viewModel.onInputTextChange(prompt)
                                viewModel.sendMessage(prompt)
                            },
                            onOpenStudySetup = { showStudySetupDialog = true }
                        )
                    } else {
                        // Message Stream
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(messages, key = { it.id }) { msg ->
                                MessageBubble(
                                    message = msg,
                                    isAotesMode = isAotesMode,
                                    onReactionToggle = { emoji -> viewModel.toggleReaction(msg.id, emoji) },
                                    onResend = {
                                        viewModel.onInputTextChange(msg.text)
                                        viewModel.sendMessage(msg.text)
                                    }
                                )
                            }

                            if (isLoading) {
                                item {
                                    ThinkingIndicator(isAotesMode = isAotesMode)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Attachment Popup Menu
    if (showAttachmentMenu) {
        AlertDialog(
            onDismissRequest = { showAttachmentMenu = false },
            title = { Text("Add Attachment / Features") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Pick Photo
                    ListItem(
                        headlineContent = { Text("Attach Photo / Diagram") },
                        supportingContent = { Text("Questions, book photos, visual problems") },
                        leadingContent = {
                            Icon(KamuiIcons.Image, contentDescription = null, tint = EmeraldSuccess)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showAttachmentMenu = false
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    )

                    // AI Edit Mode
                    ListItem(
                        headlineContent = { Text("AI Edit My Image (1:1 Canvas)") },
                        supportingContent = { Text("Preserves face contours with Kamoi Sign") },
                        leadingContent = {
                            Icon(KamuiIcons.AutoAwesome, contentDescription = null, tint = CyberPurple)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showAttachmentMenu = false
                                viewModel.toggleAiEditMode()
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    )

                    // Image AI Prompt Mode
                    ListItem(
                        headlineContent = { Text("Image AI Prompt Engine") },
                        supportingContent = { Text("1:1 square canvas cinematic art direction") },
                        leadingContent = {
                            Icon(KamuiIcons.Brush, contentDescription = null, tint = AmberGold)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showAttachmentMenu = false
                                viewModel.toggleImageAiMode()
                            }
                    )

                    // Study Mode setup
                    ListItem(
                        headlineContent = { Text("Curriculum & Notes Setup") },
                        supportingContent = { Text("Class 10, Boards, JEE, NEET standards") },
                        leadingContent = {
                            Icon(KamuiIcons.School, contentDescription = null, tint = AotesPrimaryBlue)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showAttachmentMenu = false
                                showStudySetupDialog = true
                            }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAttachmentMenu = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Study Setup Dialog
    if (showStudySetupDialog) {
        StudySetupDialog(
            currentClass = studentClass,
            currentSubjects = favoriteSubjects,
            onDismiss = { showStudySetupDialog = false },
            onConfirm = { chosenClass, chosenSubjects ->
                viewModel.setStudySetup(chosenClass, chosenSubjects)
                showStudySetupDialog = false
                Toast.makeText(context, "AOTES configured for $chosenClass!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        NewProjectDialog(
            onDismiss = { showNewProjectDialog = false },
            onCreate = { title, cat ->
                viewModel.createProject(title, cat)
                showNewProjectDialog = false
            }
        )
    }
}

@Composable
fun EmptyStateView(
    isAotesMode: Boolean,
    studentClass: String,
    favoriteSubjects: List<String>,
    onSelectPrompt: (String) -> Unit,
    onOpenStudySetup: () -> Unit
) {
    val prompts = remember(isAotesMode, studentClass, favoriteSubjects) {
        if (isAotesMode) {
            KamuiPersona.getStudyStarterPrompts(studentClass, favoriteSubjects)
        } else {
            KamuiPersona.getKamuiStarterPrompts()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            if (isAotesMode) {
                AotesAvatar(size = 80.dp)
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AotesLightBlue)
                        .clickable(onClick = onOpenStudySetup)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(KamuiIcons.School, contentDescription = null, tint = AotesPrimaryBlue, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Aotes Study AI • $studentClass",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AotesDarkBlue
                    )
                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = AotesPrimaryBlue, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ready to Study $studentClass",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = AotesTextPrimary
                )
                Text(
                    text = "Tailored to Board & Competitive exam standards. Math steps, balanced reactions, and formula derivations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AotesTextSecondary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
            } else {
                KamuiAvatar(size = 110.dp, spinMode = SpinMode.SLOW)
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "WHAT IS TODAY PLAN?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = KamuiCrimson
                )
                Text(
                    text = "Dual-Mode Cyber Dimension AI. Zero filler, pure architecture & visual direction.",
                    style = MaterialTheme.typography.bodySmall,
                    color = KamuiTextSecondary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        items(prompts) { item ->
            StarterPromptCard(
                promptItem = item,
                isAotesMode = isAotesMode,
                onClick = { onSelectPrompt(item.prompt) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun StarterPromptCard(
    promptItem: StudyPromptItem,
    isAotesMode: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isAotesMode) AotesSurface else KamuiDarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAotesMode) AotesCardBorder else KamuiNeonBorder
        ),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isAotesMode) AotesLightBlue else KamuiDarkRed.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        promptItem.subject.contains("Math", true) -> KamuiIcons.Calculate
                        promptItem.subject.contains("Physic", true) || promptItem.subject.contains("Science", true) -> KamuiIcons.Biotech
                        promptItem.subject.contains("Chem", true) -> KamuiIcons.Science
                        promptItem.subject.contains("Visual", true) -> KamuiIcons.Brush
                        promptItem.subject.contains("Engine", true) || promptItem.subject.contains("Code", true) -> KamuiIcons.Code
                        else -> KamuiIcons.AutoAwesome
                    },
                    contentDescription = null,
                    tint = if (isAotesMode) AotesPrimaryBlue else KamuiCrimson,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = promptItem.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isAotesMode) AotesTextPrimary else KamuiTextPrimary
                )
                Text(
                    text = promptItem.prompt,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isAotesMode) AotesTextSecondary else KamuiTextSecondary,
                    maxLines = 2
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ThinkingIndicator(isAotesMode: Boolean) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isAotesMode) AotesLightBlue else KamuiDarkCard)
            .border(
                1.dp,
                if (isAotesMode) AotesCardBorder else KamuiNeonBorder,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isAotesMode) {
            AotesAvatar(size = 24.dp)
            Text(
                text = "Aotes Study Engine analyzing...",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AotesDarkBlue
            )
        } else {
            KamuiAvatar(size = 24.dp, spinMode = SpinMode.FAST)
            Text(
                text = "KAMUI Scanning Dimension...",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = KamuiCrimson
            )
        }

        // Equalizer pulse bars
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        for (i in 0..3) {
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = i * 150, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((16 * scale).dp)
                    .clip(CircleShape)
                    .background(if (isAotesMode) AotesPrimaryBlue else KamuiCrimson)
            )
        }
    }
}

@Composable
fun ChatInputBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    attachedImageUri: Uri?,
    onRemoveImage: () -> Unit,
    isImageAiMode: Boolean,
    isAiEditMode: Boolean,
    isAotesMode: Boolean,
    studentClass: String,
    isLoading: Boolean,
    onSend: () -> Unit,
    onOpenAttachmentMenu: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Attached Image preview
            attachedImageUri?.let { uri ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Attached photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Image Attached (1:1 AI Canvas)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = onRemoveImage, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Rounded.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Mode indicator banner if specialized mode is active
            if (isImageAiMode || isAiEditMode) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberPurple.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(KamuiIcons.AutoAwesome, contentDescription = null, tint = CyberPurple, modifier = Modifier.size(12.dp))
                    Text(
                        text = if (isAiEditMode) "AI Edit Mode (Face-Lock 1:1 Square)" else "Image AI Prompt Engine (1:1 Ratio)",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            // Main Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "+" Add Button
                IconButton(
                    onClick = onOpenAttachmentMenu,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("add_attachment_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add attachment",
                        tint = if (isAotesMode) AotesPrimaryBlue else KamuiCrimson
                    )
                }

                // Input Text Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputTextChange,
                    placeholder = {
                        Text(
                            text = if (isAotesMode) "Ask Aotes for $studentClass..." else "Ask KAMUI, architecture, code, math...",
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isAotesMode) AotesPrimaryBlue else KamuiCrimson,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    maxLines = 4,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                )

                // Send Button
                IconButton(
                    onClick = onSend,
                    enabled = (inputText.isNotBlank() || attachedImageUri != null) && !isLoading,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if ((inputText.isNotBlank() || attachedImageUri != null) && !isLoading) {
                                if (isAotesMode) AotesPrimaryBlue else KamuiCrimson
                            } else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send message",
                        tint = if ((inputText.isNotBlank() || attachedImageUri != null) && !isLoading) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
