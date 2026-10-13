package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AotesPrimaryBlue

val AVAILABLE_CLASSES = listOf(
    "Class 10 (Board Exam)",
    "Class 9",
    "Class 11 (Science PCM/PCB)",
    "Class 11 (Commerce & Arts)",
    "Class 12 (Board Exam)",
    "JEE Main & Advanced Prep",
    "NEET Medical Entrance Prep",
    "Senior Secondary Foundation"
)

val AVAILABLE_SUBJECTS = listOf(
    "Mathematics",
    "Physics",
    "Chemistry",
    "Biology",
    "Computer Science",
    "Social Studies",
    "Current Affairs",
    "English"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudySetupDialog(
    currentClass: String,
    currentSubjects: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (selectedClass: String, selectedSubjects: List<String>) -> Unit
) {
    var selectedClass by remember { mutableStateOf(currentClass) }
    var selectedSubjects by remember { mutableStateOf(currentSubjects.toMutableSet()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AotesAvatar(size = 32.dp)
                        Column {
                            Text(
                                text = "AOTES Study Setup",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Class & Subject Curriculum",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Class Selection Section
                    item {
                        Text(
                            text = "Select Student Class / Standard",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = AotesPrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(AVAILABLE_CLASSES) { cls ->
                        val isSelected = cls == selectedClass
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) AotesPrimaryBlue.copy(alpha = 0.12f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) AotesPrimaryBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedClass = cls }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = cls,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AotesPrimaryBlue else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = AotesPrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Favorite Subjects Multi-Select
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Target Subject Priorities",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = AotesPrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AVAILABLE_SUBJECTS.forEach { subject ->
                                val isSelected = selectedSubjects.contains(subject)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedSubjects = if (isSelected) {
                                            selectedSubjects.toMutableSet().apply { remove(subject) }
                                        } else {
                                            selectedSubjects.toMutableSet().apply { add(subject) }
                                        }
                                    },
                                    label = { Text(subject, fontSize = 12.sp) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Button
                Button(
                    onClick = {
                        val finalSubjects = if (selectedSubjects.isEmpty()) listOf("Mathematics", "Physics") else selectedSubjects.toList()
                        onConfirm(selectedClass, finalSubjects)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AotesPrimaryBlue)
                ) {
                    Text("Apply & Activate AOTES Study Engine", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
