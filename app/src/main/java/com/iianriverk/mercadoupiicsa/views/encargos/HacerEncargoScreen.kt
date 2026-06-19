package com.iianriverk.mercadoupiicsa.views.encargos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
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
import com.iianriverk.mercadoupiicsa.viewModels.EncargosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HacerEncargoScreen(
    idProducto: String,
    idVendedor: String,
    navController: NavController,
    viewModel: EncargosViewModel = viewModel()
) {
    val uiState  by viewModel.hacerState.collectAsState()
    var cantidad by remember { mutableStateOf(1) }
    var notas    by remember { mutableStateOf("") }

    LaunchedEffect(idProducto) {
        viewModel.cargarProducto(idProducto)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigateUp()
            viewModel.resetHacerState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hacer encargo") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->

        when {
            uiState.isLoading && uiState.producto == null -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            uiState.producto == null -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { Text("No se pudo cargar el producto") }
            }

            else -> {
                val producto = uiState.producto!!
                val total    = producto.precioProducto * cantidad

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    // ── Resumen del producto ──────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors   = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (producto.fotoProductoUrl.isNotBlank()) {
                            AsyncImage(
                                model              = producto.fotoProductoUrl,
                                contentDescription = null,
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            )
                        }
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                producto.nombreProducto,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                producto.descripcionProducto,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "$${producto.precioProducto} c/u",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Selector de cantidad ──────────────────
                    Text("Cantidad", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilledIconButton(
                            onClick  = { if (cantidad > 1) cantidad-- },
                            enabled  = cantidad > 1,
                            colors   = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Menos")
                        }

                        Text(
                            "$cantidad",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        FilledIconButton(
                            onClick = { if (cantidad < 20) cantidad++ },
                            colors  = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Más")
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Total ─────────────────────────────────
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Total estimado", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "$${"%.2f".format(total)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        "El pago se realiza en efectivo al momento de recoger",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(24.dp))

                    // ── Notas ─────────────────────────────────
                    OutlinedTextField(
                        value         = notas,
                        onValueChange = { notas = it },
                        label         = { Text("Notas para el vendedor (opcional)") },
                        placeholder   = { Text("Ej: Sin azúcar, para las 2pm...") },
                        maxLines      = 3,
                        modifier      = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    // Nota de efectivo
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color    = MaterialTheme.colorScheme.primaryContainer,
                        shape    = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "💵  Pago en efectivo al recoger tu pedido",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
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

                    Button(
                        onClick  = { viewModel.enviarEncargo(idVendedor, cantidad, notas) },
                        enabled  = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape    = MaterialTheme.shapes.extraLarge
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                color       = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Enviar encargo")
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}