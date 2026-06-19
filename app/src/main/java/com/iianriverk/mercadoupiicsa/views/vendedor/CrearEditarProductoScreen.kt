package com.iianriverk.mercadoupiicsa.views.vendedor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.iianriverk.mercadoupiicsa.viewModels.ProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEditarProductoScreen(
    idProducto: String?,
    navController: NavController,
    viewModel: ProductoViewModel = viewModel()
) {
    val uiState    by viewModel.uiState.collectAsState()
    val modoEditar  = idProducto != null
    var confirmEliminar by remember { mutableStateOf(false) }

    var nombre      by remember { mutableStateOf("") }
    var precio      by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var disponible  by remember { mutableStateOf(true) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.setImageUri(uri) }

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

            // ── Imagen del producto ────────────────────────
            val imageModel: Any? = uiState.imageUri
                ?: uiState.productoEditando?.fotoProductoUrl?.takeIf { it.isNotBlank() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageModel != null) {
                    AsyncImage(
                        model             = imageModel,
                        contentDescription = "Foto del producto",
                        contentScale      = ContentScale.Crop,
                        modifier          = Modifier.fillMaxSize()
                    )
                    // Overlay para indicar que se puede cambiar
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
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Toca para agregar imagen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

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
                value           = precio,
                onValueChange   = { precio = it },
                label           = { Text("Precio (MXN)") },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix          = { Text("$") },
                modifier        = Modifier.fillMaxWidth()
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
                Switch(checked = disponible, onCheckedChange = { disponible = it })
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
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

            if (modoEditar) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick  = { confirmEliminar = true },
                    enabled  = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
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

    if (confirmEliminar && idProducto != null) {
        AlertDialog(
            onDismissRequest = { confirmEliminar = false },
            title            = { Text("¿Eliminar producto?") },
            text             = { Text("Se eliminará el producto y su imagen. Esta acción no se puede deshacer.") },
            confirmButton    = {
                TextButton(
                    onClick = {
                        confirmEliminar = false
                        viewModel.eliminar(idProducto)
                    }
                ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmEliminar = false }) { Text("Cancelar") }
            }
        )
    }
}
