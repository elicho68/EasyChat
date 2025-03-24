package com.example.easychat.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easychat.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00357F)) // Restaurar color azul de fondo
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Restaurar el logo
            Image(
                painter = painterResource(id = R.drawable.logo_chatsnapv),
                contentDescription = "Logo de EasyChat",
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.Gray,

                    cursorColor = Color.White // de morado a blanco yeymi

                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.Gray,
                    cursorColor = Color.White // de morado a blanco yeymi

                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            onLoginSuccess() // Redirigir a Home si el login es exitoso
                        }
                        .addOnFailureListener { exception ->
                            errorMessage = when {
                                exception.localizedMessage?.contains("There is no user record") == true ->
                                    "El correo no está registrado."
                                exception.localizedMessage?.contains("The password is invalid") == true ->
                                    "La contraseña es incorrecta."
                                exception.localizedMessage?.contains("A network error") == true ->
                                    "Error de red. Verifica tu conexión."
                                else -> "Error al iniciar sesión. Verifica tus datos."
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("INICIA SESIÓN", color = Color(0xFF00357F))
            }

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            TextButton(onClick = onForgotPassword) {
                Text("¿Olvidaste tu contraseña?", color = Color.White)
            }

            TextButton(onClick = onRegister) {
                Text("¿No tienes cuenta? Regístrate", color = Color.White)
            }
        }
    }
}