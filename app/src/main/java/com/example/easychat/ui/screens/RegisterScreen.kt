package com.example.easychat.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.easychat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.util.*

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onLogin: () -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isRegistered by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
    }

    fun registerUser() {
        if (email.isNotBlank() && password.length >= 6 && selectedImageUri != null) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            // Subir imagen a Firebase Storage
                            val imageRef = storage.reference.child("profile_pictures/${userId}_${UUID.randomUUID()}.jpg")
                            selectedImageUri?.let { uri ->
                                imageRef.putFile(uri).continueWithTask { uploadTask ->
                                    if (!uploadTask.isSuccessful) throw uploadTask.exception!!
                                    imageRef.downloadUrl
                                }.addOnSuccessListener { imageUrl ->
                                    // Guardar datos en Firestore
                                    val user = hashMapOf(
                                        "firstName" to firstName,
                                        "lastName" to lastName,
                                        "email" to email,
                                        "profileUrl" to imageUrl.toString()
                                    )
                                    db.collection("users").document(userId).set(user)
                                        .addOnSuccessListener {
                                            errorMessage = "Registro exitoso. Redirigiendo..."
                                            isRegistered = true
                                        }
                                        .addOnFailureListener {
                                            errorMessage = "Error al guardar usuario en Firestore"
                                        }
                                }.addOnFailureListener {
                                    errorMessage = "Error al subir la imagen de perfil"
                                }
                            }
                        }
                    } else {
                        errorMessage = when {
                            task.exception?.message?.contains("The email address is already in use") == true ->
                                "Este correo ya está registrado. Intenta iniciar sesión."
                            else -> task.exception?.message ?: "Error al registrar"
                        }
                    }
                }
        } else {
            errorMessage = "Completa todos los campos y selecciona una imagen"
        }
    }

    if (isRegistered) {
        LaunchedEffect(Unit) {
            delay(1000)
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00357F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_easychat),
                contentDescription = "Logo de EasyChat",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Imagen de perfil seleccionada
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = "Imagen seleccionada",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Seleccionar imagen", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = Color.Red, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Nombre", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.White,

                    focusedContainerColor = Color(0xFF00357F),   //yyemis azul de fondo activo
                    unfocusedContainerColor = Color(0xFF00357F), // azul de fondo inactivo
                    cursorColor = Color.White // de morado a blanco yeymi

                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Apellido", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.White,

                    focusedContainerColor = Color(0xFF00357F),   //yyemis azul de fondo activo
                    unfocusedContainerColor = Color(0xFF00357F),// azul de fondo inactivo
                    cursorColor = Color.White // de morado a blanco yeymi

                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    focusedContainerColor = Color(0xFF00357F),   //yyemis azul de fondo activo
                    unfocusedContainerColor = Color(0xFF00357F), // azul de fondo inactivo
                    cursorColor = Color.White // de morado a blanco yeymi

                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña", color = Color.White) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    focusedContainerColor = Color(0xFF00357F),   //yyemis azul de fondo activo
                    unfocusedContainerColor = Color(0xFF00357F), // azul de fondo inactivo
                    cursorColor = Color.White // de morado a blanco yeymi

                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { registerUser() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("REGISTRARSE", color = Color(0xFF145093))
            }

            TextButton(onClick = onLogin) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = Color.White)
            }
        }
    }
}
