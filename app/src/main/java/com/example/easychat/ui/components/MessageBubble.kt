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
import coil.compose.rememberImagePainter
import com.example.easychat.ui.models.Message
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape


@Composable
fun MessageBubble(message: Message, isOwnMessage: Boolean, senderProfileUrl: String?) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 🔹 Mostrar imagen del remitente si no es el usuario actual
            if (!isOwnMessage && !senderProfileUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberImagePainter(senderProfileUrl),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Card(
                modifier = Modifier.padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOwnMessage) Color(0xFF0040FF) else Color.LightGray
                )
            ) {
                if (message.messageType == "image") {
                    Image(
                        painter = rememberImagePainter(message.content),
                        contentDescription = "Imagen enviada",
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