package com.example.easychat.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import coil.compose.rememberImagePainter
import com.example.easychat.ui.models.Message // 👈 Import correcto

@Composable
fun GroupMessageBubble(message: Message, isOwnMessage: Boolean, profileUrl: String?) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isOwnMessage && !profileUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberImagePainter(profileUrl),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 4.dp)
                        .clip(CircleShape) // 👈 Este import es el que te daba error
                )
            }

            Card(
                modifier = Modifier.padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOwnMessage) Color(0xFF00357f) else Color.LightGray
                )
            ) {
                if (message.messageType == "image") {
                    Image(
                        painter = rememberImagePainter(message.content),
                        contentDescription = "Imagen del mensaje",
                        modifier = Modifier.size(200.dp)
                    )
                } else {
                    Text(
                        text = message.content,
                        color = if (isOwnMessage) Color.White else Color.Black,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
