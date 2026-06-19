package com.iianriverk.mercadoupiicsa.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.iianriverk.mercadoupiicsa.data.repositories.ProductoRepository
import com.iianriverk.mercadoupiicsa.models.Producto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProductoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    // Para pre-llenar el form en modo edición
    val productoEditando: Producto? = null
)

class ProductoViewModel : ViewModel() {

    private val repository = ProductoRepository()
    private val auth       = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    // Carga un producto existente para editar
    fun cargarProducto(idProducto: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getProductoPorId(idProducto)
            _uiState.update {
                it.copy(
                    isLoading       = false,
                    productoEditando = result.getOrNull(),
                    error           = if (result.isFailure) "No se pudo cargar el producto" else null
                )
            }
        }
    }

    fun guardar(
        idProducto: String?,   // null = nuevo producto
        nombre: String,
        precio: Double,
        descripcion: String,
        disponible: Boolean
    ) {
        val idVendedor = auth.currentUser?.uid ?: run {
            _uiState.update { it.copy(error = "Sesión no válida") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val producto = Producto(
                idProducto          = idProducto ?: "",
                idVendedor          = idVendedor,
                nombreProducto      = nombre,
                precioProducto      = precio,
                descripcionProducto = descripcion,
                fotoProductoUrl     = _uiState.value.productoEditando?.fotoProductoUrl ?: "",
                estadoProducto      = disponible
            )

            val result = if (idProducto == null) repository.agregarProducto(producto)
            else repository.editarProducto(producto)

            _uiState.update {
                if (result.isSuccess) it.copy(isLoading = false, isSuccess = true)
                else it.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage ?: "Error al guardar"
                )
            }
        }
    }

    fun resetState() = _uiState.update { ProductoUiState() }
}