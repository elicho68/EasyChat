package com.example.easychat.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.easychat.ui.components.GroupMessageBubble
import com.example.easychat.ui.models.Message
import com.example.easychat.ui.services.GroupChatService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    groupId: String,
    onBack: () -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val service = remember { GroupChatService() }
    val coroutineScope = rememberCoroutineScope()

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Message>()) }
    var profileUrls by remember { mutableStateOf(mapOf<String, String?>()) }
    var groupName by remember { mutableStateOf("Grupo") }

    // Cargar nombre del grupo desde Firestore
    LaunchedEffect(groupId) {
        FirebaseFirestore.getInstance()
            .collection("groups")
            .document(groupId)
            .get()
            .addOnSuccessListener { doc ->
                groupName = doc.getString("name") ?: "Grupo sin nombre"
            }
    }

    // Obtener mensajes en tiempo real
    LaunchedEffect(groupId) {
        service.getGroupMessages(groupId) { result ->
            messages = result
        }
    }

    // Cargar URLs de perfil
    LaunchedEffect(messages) {
        messages.forEach { msg ->
            if (!profileUrls.containsKey(msg.senderId)) {
                service.getProfileUrl(msg.senderId) { url ->
                    profileUrls = profileUrls + (msg.senderId to url)
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                uploadImageAndSend(it, groupId, service)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(groupName, color = androidx.compose.ui.graphics.Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color(0xFF00357f))
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
                    GroupMessageBubble(
                        message = message,
                        isOwnMessage = message.senderId == currentUser,
                        profileUrl = profileUrls[message.senderId]
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
                IconButton(onClick = {
                    imagePickerLauncher.launch("image/*")
                }) {
                    Icon(Icons.Filled.AttachFile, contentDescription = "Adjuntar imagen")
                }
                Button(onClick = {
                    if (messageText.isNotBlank()) {
                        service.sendMessageToGroup(messageText, "text", groupId)
                        messageText = ""
                    }
                }) {
                    Text("Enviar")
                }
            }
        }
    }
}

suspend fun uploadImageAndSend(uri: Uri, groupId: String, service: GroupChatService) {
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("group_images/${java.util.UUID.randomUUID()}.jpg")

    imageRef.putFile(uri).await()
    val downloadUrl = imageRef.downloadUrl.await().toString()

    service.sendImageToGroup(downloadUrl, groupId)
}
