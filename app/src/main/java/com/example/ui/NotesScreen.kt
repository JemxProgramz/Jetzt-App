package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Note
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val notes by viewModel.notes.collectAsState()
    
    var activeNote by remember { mutableStateOf<Note?>(null) }
    var isAdding by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        if (activeNote != null || isAdding) {
            NoteEditor(
                initialTitle = activeNote?.title ?: "",
                initialContent = activeNote?.content ?: "",
                initialPinned = activeNote?.pinned ?: false,
                isExistingNote = activeNote != null,
                onClose = {
                    activeNote = null
                    isAdding = false
                },
                onDelete = {
                    activeNote?.let { viewModel.deleteNote(it.id) }
                    activeNote = null
                    isAdding = false
                },
                onSave = { title, content, pinned ->
                    if (isAdding) {
                        if (title.isNotBlank() || content.isNotBlank()) {
                            viewModel.addNote(title.ifBlank { "Untitled Note" }, content, pinned)
                        }
                    } else {
                        activeNote?.let {
                            viewModel.updateNote(it.copy(title = title, content = content, pinned = pinned))
                        }
                    }
                    activeNote = null
                    isAdding = false
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp)
            ) {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                if (notes.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.EditNote, contentDescription = null, tint = Slate600, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No notes yet. Tap + to create one.", color = Slate500)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(notes) { note ->
                            NoteCard(note = note, onClick = { activeNote = note })
                        }
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }

            FloatingActionButton(
                onClick = { isAdding = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .padding(bottom = 80.dp),
                containerColor = Indigo600,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    }
}

val Slate600 = Color(0xFF475569)

@Composable
private fun NoteCard(note: Note, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Slate800.copy(alpha = 0.8f))
            .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = note.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = note.content,
            style = MaterialTheme.typography.bodySmall,
            color = Slate400,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = dateFormat.format(Date(note.updatedAt)),
            style = MaterialTheme.typography.labelSmall,
            color = Slate500,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun NoteEditor(
    initialTitle: String,
    initialContent: String,
    initialPinned: Boolean,
    isExistingNote: Boolean,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onSave: (String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }
    var pinned by remember { mutableStateOf(initialPinned) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate400)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { pinned = !pinned }) {
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = "Pin",
                        tint = if (pinned) Amber500 else Slate400
                    )
                }
                if (isExistingNote) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Rose500)
                    }
                }
                TextButton(
                    onClick = { onSave(title, content, pinned) },
                    colors = ButtonDefaults.textButtonColors(contentColor = Indigo400)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save", fontWeight = FontWeight.Medium)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold),
                cursorBrush = SolidColor(Indigo500),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) Text("Note Title", color = Slate600, style = MaterialTheme.typography.headlineMedium)
                    innerTextField()
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            BasicTextField(
                value = content,
                onValueChange = { content = it },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Slate300, lineHeight = 24.sp),
                cursorBrush = SolidColor(Indigo500),
                modifier = Modifier.fillMaxSize(),
                decorationBox = { innerTextField ->
                    if (content.isEmpty()) Text("Start typing...", color = Slate600, style = MaterialTheme.typography.bodyLarge)
                    innerTextField()
                }
            )
        }
    }
}
