package com.example.easychat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easychat.ui.components.AddContactDialog
import com.example.easychat.ui.components.BottomNavigationBar
import com.example.easychat.ui.components.ContactItem
import com.example.easychat.ui.components.CreateGroupDialog
import com.example.easychat.ui.services.ContactService
import com.example.easychat.ui.services.GroupService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onProfile: () -> Unit,
    onChatSelected: (String, String) -> Unit,
    onGroupSelected: (String) -> Unit
) {
    val contactService = remember { ContactService() }
    val groupService = remember { GroupService() }

    var expanded by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }

    var contacts by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var groups by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    var selectedTab by remember { mutableStateOf("chats") }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(selectedTab) {
        if (selectedTab == "chats") {
            contactService.getContacts { result -> contacts = result }
        } else if (selectedTab == "grupos") {
            groupService.getGroups { result -> groups = result }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EasyChat", color = Color.White) },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menú")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Perfil") },
                            onClick = {
                                expanded = false
                                onProfile()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Agregar contacto") },
                            onClick = {
                                expanded = false
                                showAddContactDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Crear grupo") },
                            onClick = {
                                expanded = false
                                showCreateGroupDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                expanded = false
                                onLogout()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color(0xFF00357f))
            )
        },
        bottomBar = {
            BottomNavigationBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectedTab == "chats") {
                Text("Tus contactos", fontSize = 20.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(contacts) { contact ->
                        val profileUrl = contact["profileUrl"] as? String ?: ""
                        val firstName = contact["firstName"] as? String ?: ""
                        val lastName = contact["lastName"] as? String ?: ""
                        val userId = contact["id"] as? String ?: ""

                        ContactItem(
                            firstName = firstName,
                            lastName = lastName,
                            profileImageUrl = profileUrl
                        ) {
                            onChatSelected(userId, "$firstName $lastName")
                        }
                    }
                }
            } else if (selectedTab == "grupos") {
                Text("Tus grupos", fontSize = 20.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(groups) { group ->
                        val groupId = group["groupId"] as? String ?: return@items
                        val groupName = group["groupName"] as? String ?: "Sin nombre"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    onGroupSelected(groupId)
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Text(
                                text = groupName,
                                fontSize = 18.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddContactDialog) {
        AddContactDialog(
            onDismiss = { showAddContactDialog = false },
            onAddContact = { email ->
                contactService.addContactByEmail(email) { success, msg ->
                    message = msg
                    showAddContactDialog = false
                }
            }
        )
    }

    if (showCreateGroupDialog) {
        CreateGroupDialog(
            contacts = contacts,
            onDismiss = { showCreateGroupDialog = false },
            onCreateGroup = { name, members ->
                groupService.createGroup(name, members) { success, msg ->
                    showCreateGroupDialog = false
                }
            }
        )
    }
}
