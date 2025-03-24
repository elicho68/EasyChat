package com.example.easychat.ui.services

import com.example.easychat.ui.models.Message
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class GroupChatService {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun sendMessageToGroup(content: String, type: String, groupId: String) {
        val currentUser = auth.currentUser ?: return

        val message = Message(
            senderId = currentUser.uid,
            receiverId = groupId,
            content = content,
            messageType = type,
            timestamp = Timestamp.now()
        )

        db.collection("group_chats")
            .document(groupId)
            .collection("messages")
            .add(message)
    }

    fun getGroupMessages(groupId: String, onResult: (List<Message>) -> Unit) {
        db.collection("group_chats")
            .document(groupId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                    onResult(messages)
                }
            }
    }

    fun getProfileUrl(userId: String, onResult: (String?) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                onResult(doc.getString("profileUrl"))
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun sendImageToGroup(imageUrl: String, groupId: String) {
        sendMessageToGroup(imageUrl, "image", groupId)
    }
}
