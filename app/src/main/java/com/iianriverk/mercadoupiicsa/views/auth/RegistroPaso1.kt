package com.iianriverk.mercadoupiicsa.views.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.iianriverk.mercadoupiicsa.models.RolUsuario
import com.iianriverk.mercadoupiicsa.navigation.Screen
import com.iianriverk.mercadoupiicsa.viewModels.AuthViewModel

@Composable
fun RegistroPaso1Screen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var selectedRol by remember { mutableStateOf(RolUsuario.ALUMNO) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val form by viewModel.registerForm.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        IconButton(onClick = { navController.navigateUp() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
        }

        Spacer(Modifier.height(8.dp))

        Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Regístrate para comenzar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        // Selector de rol
        Text("Tipo de cuenta", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selectedRol == RolUsuario.ALUMNO,
                onClick = { selectedRol = RolUsuario.ALUMNO },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                label = { Text("Alumno") }
            )
            SegmentedButton(
                selected = selectedRol == RolUsuario.VENDEDOR,
                onClick = { selectedRol = RolUsuario.VENDEDOR },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                label = { Text("Vendedor") }
            )
        }

        Spacer(Modifier.height(20.dp))

        // En OAuth el correo viene del proveedor (Google/Facebook) y no se edita.
        // En registro normal se captura email + contraseña.
        if (form.isOAuth) {
            OutlinedTextField(
                value = form.email,
                onValueChange = {},
                label = { Text("Correo electrónico") },
                readOnly = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = null },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; passwordError = null },
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                isError = passwordError != null,
                supportingText = passwordError?.let { msg -> { Text(msg) } },
                visualTransformation = if (confirmVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(
                            imageVector = if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (form.isOAuth) {
                    // Ya está autenticado, solo guardamos el rol
                    viewModel.saveStep1(form.email, "", selectedRol)
                    navController.navigate(Screen.RegistroPaso2.route)
                } else {
                    when {
                        password.length < 6        -> passwordError = "Mínimo 6 caracteres"
                        password != confirmPassword -> passwordError = "Las contraseñas no coinciden"
                        else -> {
                            viewModel.saveStep1(email.trim(), password, selectedRol)
                            navController.navigate(Screen.RegistroPaso2.route)
                        }
                    }
                }
            },
            enabled = if (form.isOAuth) true
            else email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Text("Siguiente")
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿Ya tienes cuenta?", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { navController.navigateUp() }) {
                Text("Inicia sesión")
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}