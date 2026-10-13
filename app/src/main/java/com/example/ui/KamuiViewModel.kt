package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.KamuiDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class KamuiViewModel(application: Application) : AndroidViewModel(application) {

    private val db = KamuiDatabase.getInstance(application)
    private val repository = ChatRepository(application, db.chatDao())

    private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val sessions: StateFlow<List<ChatSession>> = _sessions.asStateFlow()

    private val _activeSessionId = MutableStateFlow<String>("")
    val activeSessionId: StateFlow<String> = _activeSessionId.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isAotesMode = MutableStateFlow(false)
    val isAotesMode: StateFlow<Boolean> = _isAotesMode.asStateFlow()

    private val _studentClass = MutableStateFlow("Class 10 (Board Exam)")
    val studentClass: StateFlow<String> = _studentClass.asStateFlow()

    private val _favoriteSubjects = MutableStateFlow(listOf("Mathematics", "Physics", "Chemistry"))
    val favoriteSubjects: StateFlow<List<String>> = _favoriteSubjects.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _attachedImageUri = MutableStateFlow<Uri?>(null)
    val attachedImageUri: StateFlow<Uri?> = _attachedImageUri.asStateFlow()

    private val _isImageAiMode = MutableStateFlow(false)
    val isImageAiMode: StateFlow<Boolean> = _isImageAiMode.asStateFlow()

    private val _isAiEditMode = MutableStateFlow(false)
    val isAiEditMode: StateFlow<Boolean> = _isAiEditMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        // Collect sessions from Room
        viewModelScope.launch {
            repository.allSessions.collectLatest { sessionList ->
                _sessions.value = sessionList
                if (sessionList.isEmpty()) {
                    // Create default initial chat
                    val defaultSession = repository.createNewSession(
                        title = "New Chat",
                        isAotesMode = _isAotesMode.value
                    )
                    _activeSessionId.value = defaultSession.id
                } else if (_activeSessionId.value.isBlank() || sessionList.none { it.id == _activeSessionId.value }) {
                    _activeSessionId.value = sessionList.first().id
                }
            }
        }

        // Collect messages whenever activeSessionId changes
        viewModelScope.launch {
            _activeSessionId.collectLatest { sId ->
                if (sId.isNotBlank()) {
                    repository.getMessagesForSession(sId).collectLatest { msgs ->
                        _messages.value = msgs
                    }
                }
            }
        }
    }

    fun onInputTextChange(text: String) {
        _inputText.value = text
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setAttachedImage(uri: Uri?) {
        _attachedImageUri.value = uri
        if (uri != null) {
            _isAiEditMode.value = true
        }
    }

    fun toggleImageAiMode() {
        _isImageAiMode.value = !_isImageAiMode.value
        if (_isImageAiMode.value) {
            _isAiEditMode.value = false
        }
    }

    fun toggleAiEditMode() {
        _isAiEditMode.value = !_isAiEditMode.value
        if (_isAiEditMode.value) {
            _isImageAiMode.value = false
        }
    }

    fun toggleMode() {
        val next = !_isAotesMode.value
        _isAotesMode.value = next
    }

    fun setStudySetup(selectedClass: String, subjects: List<String>) {
        _studentClass.value = selectedClass
        _favoriteSubjects.value = subjects
        _isAotesMode.value = true
    }

    fun startNewChat() {
        viewModelScope.launch {
            val session = repository.createNewSession(
                title = "New Chat",
                isAotesMode = _isAotesMode.value
            )
            _activeSessionId.value = session.id
            _inputText.value = ""
            _attachedImageUri.value = null
            _isAiEditMode.value = false
        }
    }

    fun createProject(title: String, category: String) {
        viewModelScope.launch {
            val project = repository.createNewSession(
                title = title,
                isProject = true,
                projectCategory = category,
                isAotesMode = _isAotesMode.value
            )
            _activeSessionId.value = project.id
            _inputText.value = ""
            _attachedImageUri.value = null
            _isAiEditMode.value = false
        }
    }

    fun selectSession(sessionId: String) {
        _activeSessionId.value = sessionId
        _inputText.value = ""
        _attachedImageUri.value = null
        _isAiEditMode.value = false
    }

    fun togglePinSession(sessionId: String) {
        viewModelScope.launch {
            repository.togglePinSession(sessionId)
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            repository.renameSession(sessionId, newTitle)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun clearAllVaultSessions() {
        viewModelScope.launch {
            repository.clearAllVaultSessions()
            startNewChat()
        }
    }

    fun toggleReaction(messageId: String, emoji: String) {
        val msg = _messages.value.find { it.id == messageId } ?: return
        viewModelScope.launch {
            repository.toggleReaction(messageId, msg.reactions, emoji)
        }
    }

    fun sendMessage(overridePrompt: String? = null) {
        val promptToSend = (overridePrompt ?: _inputText.value).trim()
        val currentImage = _attachedImageUri.value
        val sessionId = _activeSessionId.value

        if (promptToSend.isBlank() && currentImage == null) return
        if (_isLoading.value) return

        val userMessage = ChatMessage(
            id = "msg_" + UUID.randomUUID().toString().take(8),
            sessionId = sessionId,
            role = "user",
            text = promptToSend.ifBlank { "Analyze attached image." },
            imageUri = currentImage?.toString(),
            isAiEdited = _isAiEditMode.value,
            isAotes = _isAotesMode.value
        )

        // Clear input bar immediately for responsiveness
        _inputText.value = ""
        _attachedImageUri.value = null
        _isLoading.value = true

        viewModelScope.launch {
            // 1. Save user message to Room
            repository.addMessage(userMessage)

            // Auto-rename session title if default
            val currentSession = _sessions.value.find { it.id == sessionId }
            if (currentSession != null && currentSession.title.startsWith("New Chat")) {
                val newTitle = promptToSend.take(28).replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }
                repository.renameSession(sessionId, newTitle)
            }

            // 2. Call Gemini
            val aiResponseText = repository.sendPrompt(
                sessionId = sessionId,
                userPrompt = promptToSend,
                imageUri = currentImage,
                history = _messages.value,
                isAotesMode = _isAotesMode.value,
                studentClass = _studentClass.value,
                favoriteSubjects = _favoriteSubjects.value
            )

            // 3. Save AI message to Room
            val aiMessage = ChatMessage(
                id = "msg_ai_" + UUID.randomUUID().toString().take(8),
                sessionId = sessionId,
                role = "ai",
                text = aiResponseText,
                isAiEdited = userMessage.isAiEdited,
                isAotes = _isAotesMode.value
            )
            repository.addMessage(aiMessage)

            _isLoading.value = false
            _isAiEditMode.value = false
        }
    }

    suspend fun getExportJson(): String {
        return repository.exportVaultAsJson()
    }

    suspend fun getExportTxt(): String {
        return repository.exportVaultAsTxt()
    }
}
