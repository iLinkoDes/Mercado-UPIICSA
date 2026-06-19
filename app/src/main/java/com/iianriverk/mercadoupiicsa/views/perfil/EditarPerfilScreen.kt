package com.iianriverk.mercadoupiicsa.views.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.iianriverk.mercadoupiicsa.models.RolUsuario
import com.iianriverk.mercadoupiicsa.models.TipoNegocio
import com.iianriverk.mercadoupiicsa.viewModels.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    navController: NavController,
    viewModel: PerfilViewModel = viewModel()
) {
    val uiState    by viewModel.uiState.collectAsState()
    val perfil      = uiState.perfil
    val esVendedor  = perfil?.rol == RolUsuario.VENDEDOR

    // Campos del formulario pre-llenados con datos actuales
    var nombreCompleto     by remember(perfil) { mutableStateOf(perfil?.nombreCompleto     ?: "") }
    var telefono           by remember(perfil) { mutableStateOf(perfil?.telefono           ?: "") }
    var nombreNegocio      by remember(perfil) { mutableStateOf(perfil?.nombreNegocio      ?: "") }
    var descripcionNegocio by remember(perfil) { mutableStateOf(perfil?.descripcionNegocio ?: "") }
    var tipoNegocio        by remember(perfil) { mutableStateOf(perfil?.tipoNegocio        ?: TipoNegocio.OTROS) }
    var dropdownExpanded   by remember { mutableStateOf(false) }

    // Regresa al perfil cuando guarda con éxito
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.cargarPerfil() // refresca datos en PerfilScreen
            viewModel.resetSuccess()
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Datos comunes ─────────────────────────────
            Text(
                "Datos personales",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value         = nombreCompleto,
                onValueChange = { nombreCompleto = it },
                label         = { Text("Nombre completo") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value         = telefono,
                onValueChange = { telefono = it },
                label         = { Text("Teléfono") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            // Nota: boleta y correo no son editables
            Spacer(Modifier.height(4.dp))
            Text(
                "El correo y la boleta no pueden modificarse.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── Datos del negocio (solo Vendedor) ─────────
            if (esVendedor) {
                Spacer(Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Text(
                    "Datos del negocio",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value         = nombreNegocio,
                    onValueChange = { nombreNegocio = it },
                    label         = { Text("Nombre del negocio") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded        = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value         = tipoNegocio.name
                            .lowercase()
                            .replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Tipo de negocio") },
                        trailingIcon  = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded        = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        TipoNegocio.entries.forEach { tipo ->
                            DropdownMenuItem(
                                text    = {
                                    Text(tipo.name.lowercase().replaceFirstChar { it.uppercase() })
                                },
                                onClick = {
                                    tipoNegocio      = tipo
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value         = descripcionNegocio,
                    onValueChange = { descripcionNegocio = it },
                    label         = { Text("Descripción del negocio") },
                    maxLines      = 3,
                    modifier      = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))

            uiState.error?.let { error ->
                Text(
                    error,
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

            val camposValidos = nombreCompleto.isNotBlank() && telefono.isNotBlank() &&
                    if (esVendedor) nombreNegocio.isNotBlank() && descripcionNegocio.isNotBlank()
                    else true

            Button(
                onClick = {
                    viewModel.guardarCambios(
                        nombreCompleto     = nombreCompleto,
                        telefono           = telefono,
                        nombreNegocio      = nombreNegocio,
                        descripcionNegocio = descripcionNegocio,
                        tipoNegocio        = tipoNegocio
                    )
                },
                enabled  = !uiState.isSaving && camposValidos,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = MaterialTheme.shapes.extraLarge
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar cambios")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}