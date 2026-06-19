package com.iianriverk.mercadoupiicsa.views.perfil

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.iianriverk.mercadoupiicsa.models.RolUsuario
import com.iianriverk.mercadoupiicsa.models.TipoNegocio
import com.iianriverk.mercadoupiicsa.viewModels.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    navController: NavController,
    viewModel: PerfilViewModel = viewModel()
) {
    val uiState   by viewModel.uiState.collectAsState()
    val perfil     = uiState.perfil
    val esVendedor = perfil?.rol == RolUsuario.VENDEDOR

    var nombreCompleto     by remember(perfil) { mutableStateOf(perfil?.nombreCompleto     ?: "") }
    var telefono           by remember(perfil) { mutableStateOf(perfil?.telefono           ?: "") }
    var nombreNegocio      by remember(perfil) { mutableStateOf(perfil?.nombreNegocio      ?: "") }
    var descripcionNegocio by remember(perfil) { mutableStateOf(perfil?.descripcionNegocio ?: "") }
    var tipoNegocio        by remember(perfil) { mutableStateOf(perfil?.tipoNegocio        ?: TipoNegocio.OTROS) }
    var dropdownExpanded   by remember { mutableStateOf(false) }

    val fotoPerfilPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.setFotoPerfilUri(uri) }

    val fotoNegocioPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.setFotoNegocioUri(uri) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.cargarPerfil()
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Foto de perfil ─────────────────────────────
            val fotoPerfilModel: Any? = uiState.fotoPerfilUri
                ?: perfil?.fotoPerfilUrl?.takeIf { it.isNotBlank() }

            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            fotoPerfilPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (fotoPerfilModel != null) {
                        AsyncImage(
                            model              = fotoPerfilModel,
                            contentDescription = "Foto de perfil",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                Surface(
                    shape  = CircleShape,
                    color  = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Cambiar foto",
                            tint     = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                "Foto de perfil",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // ── Datos comunes ──────────────────────────────
            Text(
                "Datos personales",
                style    = MaterialTheme.typography.labelLarge,
                color    = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
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

            Spacer(Modifier.height(4.dp))
            Text(
                "El correo y la boleta no pueden modificarse.",
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Datos del negocio (solo Vendedor) ──────────
            if (esVendedor) {
                Spacer(Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Text(
                    "Datos del negocio",
                    style    = MaterialTheme.typography.labelLarge,
                    color    = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                // Foto del negocio
                val fotoNegocioModel: Any? = uiState.fotoNegocioUri
                    ?: perfil?.fotoNegocioUrl?.takeIf { it.isNotBlank() }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            fotoNegocioPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (fotoNegocioModel != null) {
                        AsyncImage(
                            model              = fotoNegocioModel,
                            contentDescription = "Foto del negocio",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint     = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Store,
                                contentDescription = null,
                                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Toca para agregar foto del negocio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

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
                    expanded         = dropdownExpanded,
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
                        expanded         = dropdownExpanded,
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
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
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
