package com.iianriverk.mercadoupiicsa.views.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import com.iianriverk.mercadoupiicsa.data.repositories.VendedorResumen
import com.iianriverk.mercadoupiicsa.models.TipoNegocio
import com.iianriverk.mercadoupiicsa.navigation.Screen
import com.iianriverk.mercadoupiicsa.viewModels.FeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: FeedViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mercado UPIICSA") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    if (uiState.esMiNegocioVisible) {
                        IconButton(onClick = {
                            navController.navigate(
                                Screen.Catalogo.createRoute(uiState.idVendedorActual)
                            )
                        }) {
                            Icon(Icons.Default.Store, contentDescription = "Mi negocio")
                        }
                    }
                    // Chat y Botones de Navegacion
                    BadgedBox(
                        badge = {
                            if (uiState.mensajesNoLeidos > 0) {
                                Badge { Text("${uiState.mensajesNoLeidos}") }
                            }
                        }
                    ) {
                        IconButton(onClick = { navController.navigate(Screen.MisEncargos.route) }) {
                            Icon(Icons.Default.Receipt, contentDescription = "Mis encargos")
                        }
                    }
                    IconButton(onClick = { navController.navigate(Screen.Perfil.route) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Mi perfil")
                    }
                    IconButton(onClick = { viewModel.cargarVendedores() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
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
            OutlinedTextField(
                value         = uiState.query,
                onValueChange = { viewModel.buscar(it) },
                placeholder   = { Text("Buscar vendedores...") },
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon  = {
                    if (uiState.query.isNotBlank()) {
                        IconButton(onClick = { viewModel.buscar("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape      = MaterialTheme.shapes.extraLarge,
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.filtroActivo == null,
                        onClick  = { viewModel.aplicarFiltro(null) },
                        label    = { Text("Todos") }
                    )
                }
                items(TipoNegocio.entries) { tipo ->
                    FilterChip(
                        selected = uiState.filtroActivo == tipo,
                        onClick  = { viewModel.aplicarFiltro(tipo) },
                        label    = {
                            Text(tipo.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    )
                }
            }

            HorizontalDivider()

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    ErrorFeed(
                        mensaje     = uiState.error!!,
                        onReintento = { viewModel.cargarVendedores() }
                    )
                }
                uiState.vendedoresFiltrados.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = when {
                                uiState.query.isNotBlank()      -> "Sin resultados para \"${uiState.query}\""
                                uiState.filtroActivo != null    -> "No hay vendedores en esta categoría"
                                else                            -> "No hay vendedores registrados aún"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.vendedoresFiltrados,
                            key   = { it.idVendedor }
                        ) { vendedor ->
                            VendedorCard(
                                vendedor = vendedor,
                                onClick  = {
                                    navController.navigate(
                                        Screen.Catalogo.createRoute(vendedor.idVendedor)
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
private fun VendedorCard(
    vendedor: VendedorResumen,
    onClick: () -> Unit
) {
    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (vendedor.fotoNegocioUrl.isNotBlank()) {
                    AsyncImage(
                        model              = vendedor.fotoNegocioUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector        = Icons.Default.Store,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(28.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = vendedor.nombreNegocio,
                    style    = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text     = vendedor.tipoNegocio.name
                            .lowercase().replaceFirstChar { it.uppercase() },
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = vendedor.descripcionNegocio,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ErrorFeed(mensaje: String, onReintento: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text  = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onReintento) { Text("Reintentar") }
        }
    }
}