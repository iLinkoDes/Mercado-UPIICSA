package com.iianriverk.mercadoupiicsa.views.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.iianriverk.mercadoupiicsa.models.Producto
import com.iianriverk.mercadoupiicsa.navigation.Screen
import com.iianriverk.mercadoupiicsa.viewModels.CatalogoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoVendedorScreen(
    idVendedor: String,
    navController: NavController,
    viewModel: CatalogoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(idVendedor) {
        viewModel.cargar(idVendedor)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.vendedor?.nombreNegocio ?: "Catálogo")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            // FAB solo visible para el propio vendedor
            if (uiState.esPropioVendedor) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.ProductoNuevo.route) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar producto")
                }
            }
        }
    ) { innerPadding ->

        when {
            uiState.isLoading -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            uiState.error != null -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { viewModel.cargar(idVendedor) }) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = innerPadding.calculateTopPadding() + 8.dp,
                        bottom = 80.dp // espacio para el FAB
                    ),
                    verticalArrangement   = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header del negocio
                    item(span = { GridItemSpan(2) }) {
                        VendedorHeader(
                            nombreNegocio      = uiState.vendedor?.nombreNegocio ?: "",
                            tipoNegocio        = uiState.vendedor?.tipoNegocio?.name
                                ?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "",
                            descripcionNegocio = uiState.vendedor?.descripcionNegocio ?: "",
                            fotoNegocioUrl     = uiState.vendedor?.fotoNegocioUrl ?: "",
                            totalProductos     = uiState.productos.size
                        )
                    }

                    if (uiState.productos.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                Modifier.fillMaxWidth().padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (uiState.esPropioVendedor)
                                        "Aún no tienes productos.\nToca + para agregar uno."
                                    else
                                        "Este vendedor no tiene productos disponibles.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(uiState.productos, key = { it.idProducto }) { producto ->
                            ProductoCard(
                                producto         = producto,
                                esPropioVendedor = uiState.esPropioVendedor,
                                onEditar         = {
                                    navController.navigate(Screen.ProductoEditar.createRoute(producto.idProducto))
                                },
                                onEncargar       = {
                                    navController.navigate(
                                        Screen.HacerEncargo.createRoute(producto.idProducto, idVendedor)
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
private fun VendedorHeader(
    nombreNegocio:      String,
    tipoNegocio:        String,
    descripcionNegocio: String,
    fotoNegocioUrl:     String,
    totalProductos:     Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                if (fotoNegocioUrl.isNotBlank()) {
                    AsyncImage(
                        model              = fotoNegocioUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(nombreNegocio, style = MaterialTheme.typography.titleMedium)
                Text(
                    tipoNegocio,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    descripcionNegocio,
                    style    = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text     = "$totalProductos producto(s) disponible(s)",
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )
    }
}

@Composable
private fun ProductoCard(
    producto: Producto,
    esPropioVendedor: Boolean,
    onEditar: () -> Unit,
    onEncargar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Foto del producto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (producto.fotoProductoUrl.isNotBlank()) {
                    AsyncImage(
                        model              = producto.fotoProductoUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Badge "No disponible" solo visible para el vendedor
                if (esPropioVendedor && !producto.estadoProducto) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                        shape    = RoundedCornerShape(4.dp),
                        color    = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            "No disponible",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    producto.nombreProducto,
                    style    = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    producto.descripcionProducto,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "$${producto.precioProducto}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (esPropioVendedor) {
                        IconButton(onClick = onEditar, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Edit, "Editar", modifier = Modifier.size(16.dp))
                        }
                    } else {
                        // Botón de encargo para alumnos
                        FilledTonalButton(
                            onClick       = onEncargar,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier      = Modifier.height(28.dp)
                        ) {
                            Text("Encargar", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

            }
        }
    }
}