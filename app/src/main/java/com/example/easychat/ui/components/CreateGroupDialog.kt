package com.example.easychat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CreateGroupDialog(
    contacts: List<Map<String, Any>>,
    onDismiss: () -> Unit,
    onCreateGroup: (String, List<String>) -> Unit
) {
    var groupName by remember { mutableStateOf(TextFieldValue("")) }
    var selectedContacts by remember { mutableStateOf(setOf<String>()) }
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear grupo") },
        text = {
            Column {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Nombre del grupo") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Selecciona contactos:")

                LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                    items(contacts.filter {
                        (it["email"] as? String) != currentUserEmail
                    }) { contact ->
                        val email = contact["email"] as? String ?: return@items
                        val firstName = contact["firstName"] as? String ?: ""
                        val lastName = contact["lastName"] as? String ?: ""
                        val isSelected = selectedContacts.contains(email)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .toggleable(
                                    value = isSelected,
                                    onValueChange = {
                                        selectedContacts = if (it) {
                                            selectedContacts + email
                                        } else {
                                            selectedContacts - email
                                        }
                                    }
                                )
                        ) {
                            Checkbox(checked = isSelected, onCheckedChange = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$firstName $lastName")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val name = groupName.text.trim()
                if (name.isNotBlank() && selectedContacts.isNotEmpty()) {
                    val finalMembers = listOfNotNull(currentUserEmail) + selectedContacts.toList()
                    onCreateGroup(name, finalMembers)
                }
                onDismiss()
            }) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
