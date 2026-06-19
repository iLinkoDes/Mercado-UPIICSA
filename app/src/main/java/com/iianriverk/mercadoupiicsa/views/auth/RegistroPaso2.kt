package com.iianriverk.mercadoupiicsa.views.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.iianriverk.mercadoupiicsa.models.RolUsuario
import com.iianriverk.mercadoupiicsa.models.TipoNegocio
import com.iianriverk.mercadoupiicsa.navigation.Screen
import com.iianriverk.mercadoupiicsa.viewModels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroPaso2Screen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val form by viewModel.registerForm.collectAsState()
    val isVendedor = form.rol == RolUsuario.VENDEDOR

    //var nombreCompleto by remember { mutableStateOf("") }
    var boleta by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    // Campos solo para Vendedor
    var nombreNegocio by remember { mutableStateOf("") }
    var descripcionNegocio by remember { mutableStateOf("") }
    var tipoNegocioSeleccionado by remember { mutableStateOf(TipoNegocio.OTROS) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var nombreCompleto by remember(form.nombrePrefill) {
        mutableStateOf(form.nombrePrefill)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate(Screen.Feed.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
            viewModel.resetState()
        }
    }

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

        Text("Completa tu perfil", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            if (isVendedor) "Datos de alumno y negocio" else "Datos de alumno",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        // ── Datos comunes ────────────────────────────────
        OutlinedTextField(
            value = nombreCompleto,
            onValueChange = { nombreCompleto = it },
            label = { Text("Nombre completo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = boleta,
            onValueChange = { boleta = it },
            label = { Text("Boleta") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        // ── Datos solo para Vendedor ──────────────────────
        if (isVendedor) {
            Spacer(Modifier.height(24.dp))

            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Text(
                "Datos del negocio",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = nombreNegocio,
                onValueChange = { nombreNegocio = it },
                label = { Text("Nombre del negocio") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Dropdown tipo de negocio
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = tipoNegocioSeleccionado.name
                        .lowercase()
                        .replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de negocio") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    TipoNegocio.entries.forEach { tipo ->
                        DropdownMenuItem(
                            text = {
                                Text(tipo.name.lowercase().replaceFirstChar { it.uppercase() })
                            },
                            onClick = {
                                tipoNegocioSeleccionado = tipo
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = descripcionNegocio,
                onValueChange = { descripcionNegocio = it },
                label = { Text("Descripción del negocio") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }

        val camposValidos = nombreCompleto.isNotBlank() && boleta.isNotBlank() && telefono.isNotBlank() &&
                if (isVendedor) nombreNegocio.isNotBlank() && descripcionNegocio.isNotBlank() else true

        Button(
            onClick = {
                viewModel.register(
                    nombreCompleto     = nombreCompleto.trim(),
                    boleta             = boleta.trim(),
                    telefono           = telefono.trim(),
                    nombreNegocio      = nombreNegocio.trim(),
                    descripcionNegocio = descripcionNegocio.trim(),
                    tipoNegocio        = tipoNegocioSeleccionado
                )
            },
            enabled = !uiState.isLoading && camposValidos,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Guardar y continuar")
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}