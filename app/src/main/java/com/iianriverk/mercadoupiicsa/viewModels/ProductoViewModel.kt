package com.iianriverk.mercadoupiicsa.viewModels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.iianriverk.mercadoupiicsa.data.repositories.ProductoRepository
import com.iianriverk.mercadoupiicsa.data.repositories.StorageRepository
import com.iianriverk.mercadoupiicsa.models.Producto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProductoUiState(
    val isLoading:        Boolean   = false,
    val error:            String?   = null,
    val isSuccess:        Boolean   = false,
    val productoEditando: Producto? = null,
    val imageUri:         Uri?      = null   // imagen seleccionada aún no subida
)

class ProductoViewModel : ViewModel() {

    private val repository        = ProductoRepository()
    private val storageRepository = StorageRepository()
    private val auth              = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    fun cargarProducto(idProducto: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getProductoPorId(idProducto)
            _uiState.update {
                it.copy(
                    isLoading        = false,
                    productoEditando = result.getOrNull(),
                    error            = if (result.isFailure) "No se pudo cargar el producto" else null
                )
            }
        }
    }

    fun setImageUri(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun guardar(
        idProducto:  String?,
        nombre:      String,
        precio:      Double,
        descripcion: String,
        disponible:  Boolean
    ) {
        val idVendedor = auth.currentUser?.uid ?: run {
            _uiState.update { it.copy(error = "Sesión no válida") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val esNuevo   = idProducto == null
            val docId     = idProducto ?: repository.generarId()
            val imageUri  = _uiState.value.imageUri
            val urlActual = _uiState.value.productoEditando?.fotoProductoUrl ?: ""

            val fotoUrl = if (imageUri != null) {
                val uploadResult = storageRepository.uploadImage(
                    uri  = imageUri,
                    path = "productos/$idVendedor/$docId.jpg"
                )
                if (uploadResult.isFailure) {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Error al subir la imagen")
                    }
                    return@launch
                }
                uploadResult.getOrThrow()
            } else {
                urlActual
            }

            val producto = Producto(
                idProducto          = docId,
                idVendedor          = idVendedor,
                nombreProducto      = nombre,
                precioProducto      = precio,
                descripcionProducto = descripcion,
                fotoProductoUrl     = fotoUrl,
                estadoProducto      = disponible
            )

            val result = if (esNuevo) repository.agregarProducto(producto)
                         else repository.editarProducto(producto)

            _uiState.update {
                if (result.isSuccess) it.copy(isLoading = false, isSuccess = true)
                else it.copy(
                    isLoading = false,
                    error     = result.exceptionOrNull()?.localizedMessage ?: "Error al guardar"
                )
            }
        }
    }

    fun eliminar(idProducto: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val fotoUrl = _uiState.value.productoEditando?.fotoProductoUrl ?: ""
            if (fotoUrl.isNotBlank()) {
                storageRepository.deleteImage(fotoUrl)
            }

            val result = repository.eliminarProducto(idProducto)
            _uiState.update {
                if (result.isSuccess) it.copy(isLoading = false, isSuccess = true)
                else it.copy(
                    isLoading = false,
                    error     = result.exceptionOrNull()?.localizedMessage ?: "Error al eliminar"
                )
            }
        }
    }

    fun resetState() = _uiState.update { ProductoUiState() }
}
