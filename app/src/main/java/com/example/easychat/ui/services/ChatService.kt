package com.example.easychat.ui.services

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

object ChatService {

    fun sendMessage(
        db: FirebaseFirestore,
        chatId: String,
        senderId: String,
        receiverId: String,
        content: String,
        type: String
    ) {
        val message = mapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "content" to content,
            "messageType" to type,
            "timestamp" to Timestamp.now()
        )

        db.collection("chats").document(chatId).collection("messages").add(message)
    }

    fun uploadImageToFirebase(
        uri: android.net.Uri,
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
}