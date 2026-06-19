package com.iianriverk.mercadoupiicsa.views.encargos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.iianriverk.mercadoupiicsa.models.Encargo
import com.iianriverk.mercadoupiicsa.models.EstadoEncargo
import com.iianriverk.mercadoupiicsa.models.RolUsuario
import com.iianriverk.mercadoupiicsa.viewModels.EncargosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.Chat
import androidx.compose.ui.text.font.FontWeight
import com.iianriverk.mercadoupiicsa.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisEncargosScreen(
    navController: NavController,
    viewModel: EncargosViewModel = viewModel()
) {
    val uiState by viewModel.listaState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarMisEncargos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.rolUsuario == RolUsuario.VENDEDOR)
                            "Encargos recibidos"
                        else
                            "Mis encargos"
                    )
                },
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
        ) {

            // Chips de filtro
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected  = uiState.filtroActivo == null,
                        onClick   = { viewModel.aplicarFiltro(null) },
                        label     = { Text("Todos") }
                    )
                }
                items(EstadoEncargo.entries) { estado ->
                    FilterChip(
                        selected = uiState.filtroActivo == estado,
                        onClick  = { viewModel.aplicarFiltro(estado) },
                        label    = {
                            Text(
                                when (estado) {
                                    EstadoEncargo.PENDIENTE  -> "Pendientes"
                                    EstadoEncargo.CONFIRMADO -> "Confirmados"
                                    EstadoEncargo.RECHAZADO  -> "Rechazados"
                                }
                            )
                        }
                    )
                }
            }

            HorizontalDivider()

            // Contenido
            when {
                uiState.isLoading -> { /* ... */ }
                uiState.error != null -> { /* ... */ }
                uiState.encargos.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (uiState.filtroActivo == null) {
                                if (uiState.rolUsuario == RolUsuario.VENDEDOR)
                                    "Aún no has recibido encargos"
                                else
                                    "Aún no has hecho ningún encargo"
                            } else {
                                "No hay encargos con este estado"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.encargos,
                            key   = { it.idEncargo }
                        ) { encargo ->
                            EncargoCard(
                                encargo            = encargo,
                                esVendedor         = uiState.rolUsuario == RolUsuario.VENDEDOR,
                                tienesMensajeNuevo = encargo.idEncargo in uiState.encargosConMensaje, // ← nuevo
                                onConfirmar        = {
                                    viewModel.actualizarEstado(encargo.idEncargo, EstadoEncargo.CONFIRMADO)
                                },
                                onRechazar         = {
                                    viewModel.actualizarEstado(encargo.idEncargo, EstadoEncargo.RECHAZADO)
                                },
                                onChat             = {
                                    navController.navigate(
                                        Screen.Chat.createRoute(
                                            idEncargo      = encargo.idEncargo,
                                            idReceptor     = if (uiState.rolUsuario == RolUsuario.VENDEDOR)
                                                encargo.idAlumno
                                            else
                                                encargo.idVendedor,
                                            nombreProducto = encargo.nombreProducto
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EncargoCard(
    encargo:     Encargo,
    esVendedor:  Boolean,
    tienesMensajeNuevo: Boolean,
    onConfirmar: () -> Unit,
    onRechazar:  () -> Unit,
    onChat: () -> Unit
) {
    val (containerColor, labelColor, estadoLabel) = when (encargo.estado) {
        EstadoEncargo.PENDIENTE   -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Pendiente"
        )
        EstadoEncargo.CONFIRMADO  -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Confirmado"
        )
        EstadoEncargo.RECHAZADO   -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Rechazado"
        )
    }

    val fecha = remember(encargo.fechaCreacion) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "MX"))
            .format(Date(encargo.fechaCreacion))
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Thumbnail del producto
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (encargo.fotoProductoUrl.isNotBlank()) {
                        AsyncImage(
                            model              = encargo.fotoProductoUrl,
                            contentDescription = null,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.Store,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    encargo.nombreProducto,
                    style    = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    if (tienesMensajeNuevo) {
                        Badge { Text("1") }
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = containerColor
                    ) {
                        Text(
                            estadoLabel,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = labelColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }


            Spacer(Modifier.height(8.dp))

            // ── Detalle del encargo ───────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                DetalleItem("Cantidad",  "${encargo.cantidad}")
                DetalleItem("Precio c/u", "$${encargo.precioUnitario}")
                DetalleItem("Total",     "$${"%.2f".format(encargo.total)}")
            }

            if (encargo.notas.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color    = MaterialTheme.colorScheme.surfaceVariant,
                    shape    = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "Notas: ${encargo.notas}",
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                fecha,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )


// ── Botón Chat — ambos roles, solo PENDIENTE ──────────
            if (encargo.estado == EstadoEncargo.PENDIENTE) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick  = onChat,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Abrir chat")
                }
            }

            if (esVendedor && encargo.estado == EstadoEncargo.PENDIENTE) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick  = onRechazar,
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Rechazar") }

                    Button(
                        onClick  = onConfirmar,
                        modifier = Modifier.weight(1f)
                    ) { Text("Confirmar") }
                }
            }
        }
    }
}

@Composable
private fun DetalleItem(label: String, valor: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(valor, style = MaterialTheme.typography.bodyMedium)
    }
}

