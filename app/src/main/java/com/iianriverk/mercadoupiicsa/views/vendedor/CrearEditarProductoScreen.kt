package com.iianriverk.mercadoupiicsa.views.vendedor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.iianriverk.mercadoupiicsa.viewModels.ProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEditarProductoScreen(
    idProducto: String?,   // null = nuevo producto
    navController: NavController,
    viewModel: ProductoViewModel = viewModel()
) {
    val uiState   by viewModel.uiState.collectAsState()
    val modoEditar = idProducto != null
    var confirmEliminar by remember { mutableStateOf(false) }

    // Campos del formulario
    var nombre      by remember { mutableStateOf("") }
    var precio      by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var disponible  by remember { mutableStateOf(true) }

    // Si es modo editar, carga el producto y pre-llena los campos
    LaunchedEffect(idProducto) {
        if (idProducto != null) viewModel.cargarProducto(idProducto)
    }
    LaunchedEffect(uiState.productoEditando) {
        uiState.productoEditando?.let { p ->
            nombre      = p.nombreProducto
            precio      = p.precioProducto.toString()
            descripcion = p.descripcionProducto
            disponible  = p.estadoProducto
        }
    }

    // Regresa al catálogo cuando guarda o elimina con éxito
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigateUp()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (modoEditar) "Editar producto" else "Agregar producto") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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

            OutlinedTextField(
                value         = nombre,
                onValueChange = { nombre = it },
                label         = { Text("Nombre del producto") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value         = precio,
                onValueChange = { precio = it },
                label         = { Text("Precio (MXN)") },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix        = { Text("$") },
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value         = descripcion,
                onValueChange = { descripcion = it },
                label         = { Text("Descripción") },
                maxLines      = 4,
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Toggle disponible
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Disponible", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (disponible) "Visible para los alumnos"
                        else "Oculto para los alumnos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked         = disponible,
                    onCheckedChange = { disponible = it }
                )
            }

            Spacer(Modifier.height(24.dp))

            uiState.error?.let { error ->
                Text(
                    text     = error,
                    color    = MaterialTheme.colorScheme.error,
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            val precioValido = precio.toDoubleOrNull() != null && precio.isNotBlank()

            Button(
                onClick = {
                    viewModel.guardar(
                        idProducto  = idProducto,
                        nombre      = nombre.trim(),
                        precio      = precio.toDoubleOrNull() ?: 0.0,
                        descripcion = descripcion.trim(),
                        disponible  = disponible
                    )
                },
                enabled  = !uiState.isLoading && nombre.isNotBlank() && precioValido,
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
                    Text(if (modoEditar) "Guardar cambios" else "Agregar producto")
                }
            }

            // Botón eliminar solo en modo edición
            if (modoEditar) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick  = { confirmEliminar = true },
                    enabled  = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = MaterialTheme.shapes.extraLarge,
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar producto")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Diálogo de confirmación para eliminar
    if (confirmEliminar && idProducto != null) {
        AlertDialog(
            onDismissRequest = { confirmEliminar = false },
            title            = { Text("¿Eliminar producto?") },
            text             = { Text("Esta acción no se puede deshacer.") },
            confirmButton    = {
                TextButton(
                    onClick = {
                        confirmEliminar = false
                        viewModel.guardar(
                            idProducto  = idProducto,
                            nombre      = nombre,
                            precio      = precio.toDoubleOrNull() ?: 0.0,
                            descripcion = descripcion,
                            disponible  = false
                        )
                        // Usamos un método dedicado en el ViewModel
                    }
                ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton    = {
                TextButton(onClick = { confirmEliminar = false }) { Text("Cancelar") }
            }
        )
    }
}