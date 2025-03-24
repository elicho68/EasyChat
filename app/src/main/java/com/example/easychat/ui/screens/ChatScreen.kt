package com.example.easychat.ui.screens

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.easychat.ui.models.Message
import com.example.easychat.ui.components.MessageBubble
import com.example.easychat.ui.utils.getChatId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(contactId: String, contactName: String, onBack: () -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) } // ✅ Tipo explícito
    val coroutineScope = rememberCoroutineScope()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val chatId = getChatId(currentUser, contactId)
    var contactProfileUrl by remember { mutableStateOf<String?>(null) }

    // 🔹 Obtener la foto de perfil del contacto
    LaunchedEffect(contactId) {
        db.collection("users").document(contactId).get()
            .addOnSuccessListener { document ->
                contactProfileUrl = document.getString("profileUrl") ?: ""
            }
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            selectedImageUri = uri
            uri?.let { uploadImageToFirebase(it, currentUser, contactId, chatId, db, storage) }
        }

    LaunchedEffect(chatId) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot: QuerySnapshot?, _ -> // ✅ Tipo explícito
                if (snapshot != null) {
                    messages = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Message::class.java)
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!contactProfileUrl.isNullOrEmpty()) {
                            Image(
                                painter = rememberImagePainter(contactProfileUrl),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(contactName, color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFF00357F))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        isOwnMessage = message.senderId == currentUser,
                        senderProfileUrl = if (message.senderId == contactId) contactProfileUrl else null
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("Escribe un mensaje...") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Icon(Icons.Filled.AttachFile, contentDescription = "Adjuntar imagen")
                }
                Button(onClick = {
                    if (messageText.isNotBlank()) {
                        sendMessage(db, chatId, currentUser, contactId, messageText, "text")
                        messageText = ""
                    }
                }) {
                    Text("Enviar")
                }
            }
        }
    }
}

fun uploadImageToFirebase(
    uri: Uri,
    senderId: String,
    receiverId: String,
    chatId: String,
    db: FirebaseFirestore,
    storage: FirebaseStorage
) {
    val fileName = "images/${UUID.randomUUID()}.jpg"
    val storageRef = storage.reference.child(fileName)

    storageRef.putFile(uri).addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
            sendMessage(db, chatId, senderId, receiverId, downloadUrl.toString(), "image")
        }
    }
}

fun sendMessage(
    db: FirebaseFirestore,
    chatId: String,
    senderId: String,
    receiverId: String,
    content: String,
    type: String
) {
    val message = Message(
        senderId = senderId,
        receiverId = receiverId,
        content = content,
        messageType = type,
        timestamp = Timestamp.now()
    )
    db.collection("chats").document(chatId).collection("messages").add(message)
}

