package com.example.easychat.ui.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ContactService {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getContacts(onResult: (List<Map<String, Any>>) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return
        val contactsRef = db.collection("users").document(currentUserId).collection("contacts")

        contactsRef.get().addOnSuccessListener { snapshot ->
            val contacts = snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    data["id"] = doc.id  // añadimos el ID del documento como 'id'
                    data
                } else {
                    null
                }
            }
            Log.d("DEBUG_CONTACTOS", "Contactos cargados: $contacts")
            onResult(contacts)
        }.addOnFailureListener {
            Log.e("DEBUG_CONTACTOS", "Error al obtener contactos", it)
            onResult(emptyList())
        }
    }

    fun addContactByEmail(email: String, onResult: (Boolean, String) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return

        val userQuery = db.collection("users")
            .whereEqualTo("email", email)
            .limit(1)

        userQuery.get().addOnSuccessListener { snapshot ->
            val userDoc = snapshot.documents.firstOrNull()
            if (userDoc != null) {
                val contactId = userDoc.id
                val contactData = mapOf(
                    "firstName" to (userDoc.getString("firstName") ?: ""),
                    "lastName" to (userDoc.getString("lastName") ?: ""),
                    "profileUrl" to (userDoc.getString("profileUrl") ?: ""),
                    "email" to (userDoc.getString("email") ?: "")
                )

                db.collection("users")
                    .document(currentUserId)
                    .collection("contacts")
                    .document(contactId)
                    .set(contactData)
                    .addOnSuccessListener {
                        onResult(true, "Contacto agregado exitosamente")
                    }
                    .addOnFailureListener {
                        onResult(false, "Error al agregar contacto")
                    }
            } else {
                onResult(false, "Usuario no encontrado")
            }
        }.addOnFailureListener {
            onResult(false, "Error al buscar el contacto")
        }
    }
}
