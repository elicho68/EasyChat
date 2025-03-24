package com.example.easychat.ui.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupService {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun createGroup(groupName: String, selectedMembers: List<String>, onResult: (Boolean, String) -> Unit) {
        val currentUser = auth.currentUser ?: run {
            onResult(false, "Usuario no autenticado")
            return
        }

        val validMembers = (selectedMembers + currentUser.email).filterNot { it.isNullOrBlank() }.distinct()

        val groupData = mapOf(
            "name" to groupName,
            "members" to validMembers
        )

        db.collection("groups")
            .add(groupData)
            .addOnSuccessListener {
                onResult(true, "Grupo creado exitosamente")
            }
            .addOnFailureListener { exception ->
                Log.e("GroupService", "Error al crear grupo", exception)
                onResult(false, "Error al crear el grupo")
            }
    }

    fun getGroups(onResult: (List<Map<String, Any>>) -> Unit) {
        val currentUserEmail = auth.currentUser?.email ?: return

        db.collection("groups")
            .whereArrayContains("members", currentUserEmail)
            .get()
            .addOnSuccessListener { result ->
                val groups = result.documents.map { doc ->
                    mapOf(
                        "groupId" to doc.id,
                        "groupName" to (doc.getString("name") ?: "Grupo sin nombre")
                    )
                }
                onResult(groups)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun getContacts(onResult: (List<Map<String, Any>>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("contacts")
            .get()
            .addOnSuccessListener { result ->
                val contacts = result.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    data + ("id" to doc.id) // 👈 agregamos el id
                }
                onResult(contacts)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

}
