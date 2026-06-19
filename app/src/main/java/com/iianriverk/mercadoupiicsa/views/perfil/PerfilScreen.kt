package com.iianriverk.mercadoupiicsa.views.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.iianriverk.mercadoupiicsa.models.RolUsuario
import com.iianriverk.mercadoupiicsa.navigation.Screen
import com.iianriverk.mercadoupiicsa.viewModels.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavController,
    viewModel: PerfilViewModel = viewModel()
) {
    val uiState         by viewModel.uiState.collectAsState()
    var confirmarLogout by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    // Botón editar
                    IconButton(onClick = { navController.navigate(Screen.EditarPerfil.route) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar perfil")
                    }

                }
            )
        }
    ) { innerPadding ->

        when {
            uiState.isLoading -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            uiState.perfil == null -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            uiState.error ?: "No se pudo cargar el perfil",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { viewModel.cargarPerfil() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            else -> {
                val perfil     = uiState.perfil!!
                val esVendedor = perfil.rol == RolUsuario.VENDEDOR

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(24.dp))

                    // ── Avatar ────────────────────────────────
                    Surface(
                        modifier = Modifier.size(88.dp),
                        shape    = CircleShape,
                        color    = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint     = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(perfil.nombreCompleto, style = MaterialTheme.typography.titleLarge)

                    Spacer(Modifier.height(4.dp))

                    // Badge del rol
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            if (esVendedor) "Vendedor" else "Alumno",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Datos personales ──────────────────────
                    SeccionPerfil(titulo = "Datos personales") {
                        FilaInfo("Boleta",   perfil.boleta)
                        FilaInfo("Correo",   perfil.correo)
                        FilaInfo("Teléfono", perfil.telefono)
                    }

                    // ── Datos del negocio (solo Vendedor) ─────
                    if (esVendedor) {
                        Spacer(Modifier.height(16.dp))
                        SeccionPerfil(titulo = "Datos del negocio") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Store,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint     = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    perfil.nombreNegocio,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            FilaInfo(
                                "Tipo",
                                perfil.tipoNegocio.name
                                    .lowercase()
                                    .replaceFirstChar { it.uppercase() }
                            )
                            FilaInfo("Descripción", perfil.descripcionNegocio)
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // ── Cerrar sesión ─────────────────────────
                    OutlinedButton(
                        onClick  = { confirmarLogout = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape    = MaterialTheme.shapes.extraLarge,
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Cerrar sesión")
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    // ── Diálogo confirmar logout ──────────────────────────
    if (confirmarLogout) {
        AlertDialog(
            onDismissRequest = { confirmarLogout = false },
            title            = { Text("¿Cerrar sesión?") },
            text             = { Text("Tendrás que volver a iniciar sesión para acceder.") },
            confirmButton    = {
                TextButton(
                    onClick = {
                        confirmarLogout = false
                        viewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLogout = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ── Componentes reutilizables ─────────────────────────────

@Composable
private fun SeccionPerfil(
    titulo:   String,
    content:  @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            titulo,
            style    = MaterialTheme.typography.labelLarge,
            color    = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(12.dp),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun FilaInfo(label: String, valor: String) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            valor.ifBlank { "—" },
            style = MaterialTheme.typography.bodySmall
        )
    }
    HorizontalDivider(
        modifier  = Modifier.padding(vertical = 4.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.outlineVariant
    )
}