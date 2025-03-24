package com.example.easychat.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun BottomNavigationBar(selectedTab: String, onTabSelected: (String) -> Unit) {
    NavigationBar(containerColor = Color(0xFF00357f)) {
        NavigationBarItem(
            selected = selectedTab == "chats",
            onClick = { onTabSelected("chats") },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Chats") },
            label = { Text("Chats") }
        )
        NavigationBarItem(
            selected = selectedTab == "grupos",
            onClick = { onTabSelected("grupos") },
            icon = { Icon(Icons.Filled.Group, contentDescription = "Grupos") },
            label = { Text("Grupos") }
        )
    }
}
