package com.iianriverk.mercadoupiicsa.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iianriverk.mercadoupiicsa.data.repositories.ProductoRepository
import com.iianriverk.mercadoupiicsa.data.repositories.VendedorResumen
import com.iianriverk.mercadoupiicsa.models.Producto
import com.iianriverk.mercadoupiicsa.models.TipoNegocio
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class CatalogoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val vendedor: VendedorResumen? = null,
    val productos: List<Producto> = emptyList(),
    val esPropioVendedor: Boolean = false
)

class CatalogoViewModel : ViewModel() {

    private val productoRepo = ProductoRepository()
    private val firestore    = FirebaseFirestore.getInstance()
    private val auth         = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    fun cargar(idVendedor: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val esPropioVendedor = auth.currentUser?.uid == idVendedor

            // Info pública del negocio
            val vendedor = try {
                val doc = firestore.collection("Usuarios").document(idVendedor).get().await()
                VendedorResumen(
                    idVendedor         = doc.id,
                    nombreNegocio      = doc.getString("nombreNegocio").orEmpty(),
                    descripcionNegocio = doc.getString("descripcionNegocio").orEmpty(),
                    fotoNegocioUrl     = doc.getString("fotoNegocioUrl").orEmpty(),
                    tipoNegocio        = TipoNegocio.valueOf(
                        doc.getString("tipoNegocio") ?: TipoNegocio.OTROS.name
                    )
                )
            } catch (e: Exception) { null }

            // Productos: el vendedor ve todos, el alumno solo disponibles
            val productos = if (esPropioVendedor) {
                productoRepo.getMisProductos(idVendedor)
            } else {
                productoRepo.getProductosDeVendedor(idVendedor)
            }.getOrDefault(emptyList())

            _uiState.update {
                it.copy(
                    isLoading        = false,
                    vendedor         = vendedor,
                    productos        = productos,
                    esPropioVendedor = esPropioVendedor,
                    error            = if (vendedor == null) "No se pudo cargar el vendedor" else null
                )
            }
        }
    }

    fun eliminarProducto(idProducto: String, idVendedor: String) {
        viewModelScope.launch {
            productoRepo.eliminarProducto(idProducto)
            cargar(idVendedor) // refresca la lista
        }
    }
}