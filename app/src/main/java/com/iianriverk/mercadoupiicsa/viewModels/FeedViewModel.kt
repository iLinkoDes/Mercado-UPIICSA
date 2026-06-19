package com.iianriverk.mercadoupiicsa.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iianriverk.mercadoupiicsa.data.repositories.ChatRepository
import com.iianriverk.mercadoupiicsa.data.repositories.FeedRepository
import com.iianriverk.mercadoupiicsa.data.repositories.VendedorResumen
import com.iianriverk.mercadoupiicsa.models.RolUsuario
import com.iianriverk.mercadoupiicsa.models.TipoNegocio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class FeedUiState(
    val isLoading:            Boolean              = false,
    val error:                String?              = null,
    val todosLosVendedores:   List<VendedorResumen> = emptyList(),
    val vendedoresFiltrados:  List<VendedorResumen> = emptyList(),
    val filtroActivo:         TipoNegocio?          = null,
    val query:               String                = "",
    val esMiNegocioVisible:   Boolean               = false,
    val idVendedorActual:     String                = "",
    val mensajesNoLeidos:    Int                   = 0
)

class FeedViewModel : ViewModel() {

    private val repository = FeedRepository()

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val chatRepository = ChatRepository()


    init {
        cargarVendedores()
        verificarRolUsuario()
    }

    fun cargarVendedores() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = repository.getVendedores()

            if (result.isSuccess) {
                val lista = result.getOrDefault(emptyList())
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        todosLosVendedores = lista,
                        vendedoresFiltrados = lista // Sin filtro por defecto
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.localizedMessage ?: "Error al cargar vendedores"
                    )
                }
            }
        }
    }

    private fun verificarRolUsuario() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        escucharBadge(uid)
        viewModelScope.launch {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("Usuarios")
                    .document(uid)
                    .get()
                    .await()
                val esVendedor = doc.getString("rol") == RolUsuario.VENDEDOR.name
                _uiState.update {
                    it.copy(
                        esMiNegocioVisible = esVendedor,
                        idVendedorActual   = if (esVendedor) uid else ""
                    )
                }
            } catch (e: Exception) {
                Log.e("FEED_VM", "verificarRol: ${e.localizedMessage}")
            }
        }
    }

    fun buscar(query: String) {
        _uiState.update { state ->
            state.copy(
                query               = query,
                vendedoresFiltrados = filtrar(state.todosLosVendedores, state.filtroActivo, query)
            )
        }
    }

    fun aplicarFiltro(tipo: TipoNegocio?) {
        _uiState.update { state ->
            state.copy(
                filtroActivo        = tipo,
                vendedoresFiltrados = filtrar(state.todosLosVendedores, tipo, state.query)
            )
        }
    }

    // Función privada que aplica búsqueda Y filtro de categoría al mismo tiempo
    private fun filtrar(
        lista: List<VendedorResumen>,
        tipo:  TipoNegocio?,
        query: String
    ): List<VendedorResumen> {
        return lista
            .filter { tipo == null || it.tipoNegocio == tipo }
            .filter {
                query.isBlank() ||
                        it.nombreNegocio.contains(query, ignoreCase = true) ||
                        it.descripcionNegocio.contains(query, ignoreCase = true)
            }
    }

    private fun escucharBadge(uid: String) {
        viewModelScope.launch {
            chatRepository.getBadgeCount(uid)
                .catch { /* silencioso */ }
                .collect { count ->
                    _uiState.update { it.copy(mensajesNoLeidos = count) }
                }
        }
    }

}