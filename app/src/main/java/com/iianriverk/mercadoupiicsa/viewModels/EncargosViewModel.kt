package com.iianriverk.mercadoupiicsa.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iianriverk.mercadoupiicsa.data.repositories.ChatRepository
import com.iianriverk.mercadoupiicsa.data.repositories.EncargosRepository
import com.iianriverk.mercadoupiicsa.data.repositories.ProductoRepository
import com.iianriverk.mercadoupiicsa.models.Encargo
import com.iianriverk.mercadoupiicsa.models.EstadoEncargo
import com.iianriverk.mercadoupiicsa.models.Producto
import com.iianriverk.mercadoupiicsa.models.RolUsuario
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class HacerEncargoUiState(
    val isLoading:  Boolean  = false,
    val isSuccess:  Boolean  = false,
    val error:      String?  = null,
    val producto:   Producto? = null
)

data class MisEncargosUiState(
    val isLoading:  Boolean         = false,
    val error:      String?         = null,
    val todosLosEncargos: List<Encargo> = emptyList(),
    val encargos:   List<Encargo>   = emptyList(),
    val rolUsuario: RolUsuario      = RolUsuario.ALUMNO,
    val filtroActivo:     EstadoEncargo? = null,
    val encargosConMensaje:  Set<String>     = emptySet()
)

class EncargosViewModel : ViewModel() {

    private val encargosRepo  = EncargosRepository()
    private val productoRepo  = ProductoRepository()
    private val auth          = FirebaseAuth.getInstance()
    private val firestore     = FirebaseFirestore.getInstance()

    private val _hacerState = MutableStateFlow(HacerEncargoUiState())
    val hacerState: StateFlow<HacerEncargoUiState> = _hacerState.asStateFlow()

    private val chatRepository = ChatRepository()

    fun cargarProducto(idProducto: String) {
        viewModelScope.launch {
            _hacerState.update { it.copy(isLoading = true) }
            val result = productoRepo.getProductoPorId(idProducto)
            _hacerState.update {
                it.copy(
                    isLoading = false,
                    producto  = result.getOrNull(),
                    error     = if (result.isFailure) "No se pudo cargar el producto" else null
                )
            }
        }
    }

    fun enviarEncargo(
        idVendedor: String,
        cantidad:   Int,
        notas:      String
    ) {
        val producto  = _hacerState.value.producto ?: return
        val idAlumno  = auth.currentUser?.uid ?: run {
            _hacerState.update { it.copy(error = "Sesión no válida") }
            return
        }

        viewModelScope.launch {
            _hacerState.update { it.copy(isLoading = true, error = null) }

            val encargo = Encargo(
                idVendedor     = idVendedor,
                idAlumno       = idAlumno,
                nombreProducto = producto.nombreProducto,
                precioUnitario = producto.precioProducto,
                cantidad       = cantidad,
                total          = producto.precioProducto * cantidad,
                notas          = notas.trim()
            )

            val result = encargosRepo.crearEncargo(encargo)
            _hacerState.update {
                if (result.isSuccess) it.copy(isLoading = false, isSuccess = true)
                else it.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage ?: "Error al enviar encargo"
                )
            }
        }
    }

    fun resetHacerState() = _hacerState.update { HacerEncargoUiState() }

    private val _listaState = MutableStateFlow(MisEncargosUiState())
    val listaState: StateFlow<MisEncargosUiState> = _listaState.asStateFlow()

    fun cargarMisEncargos() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _listaState.update { it.copy(isLoading = true, error = null) }

            // Escucha notificaciones en paralelo
            launch {
                chatRepository.getEncargosConNotificacion(uid)
                    .catch { }
                    .collect { ids ->
                        _listaState.update { it.copy(encargosConMensaje = ids) }
                    }
            }

            // Obtiene el rol del usuario desde Firestore
            val rol = try {
                val doc  = firestore.collection("Usuarios").document(uid).get().await()
                RolUsuario.valueOf(doc.getString("rol") ?: RolUsuario.ALUMNO.name)
            } catch (e: Exception) { RolUsuario.ALUMNO }

            val result = if (rol == RolUsuario.VENDEDOR) {
                encargosRepo.getEncargosComoVendedor(uid)
            } else {
                encargosRepo.getEncargosComoAlumno(uid)
            }

            _listaState.update {
                it.copy(
                    isLoading        = false,
                    todosLosEncargos = result.getOrDefault(emptyList()),
                    encargos         = result.getOrDefault(emptyList()),
                    rolUsuario       = rol,
                    filtroActivo     = null, // resetea filtro al recargar
                    error            = if (result.isFailure)
                        result.exceptionOrNull()?.localizedMessage else null
                )
            }
        }
    }

    fun actualizarEstado(idEncargo: String, estado: EstadoEncargo) {
        viewModelScope.launch {
            encargosRepo.actualizarEstado(idEncargo, estado)


            if (estado == EstadoEncargo.CONFIRMADO || estado == EstadoEncargo.RECHAZADO) {
                val encargo = _listaState.value.todosLosEncargos
                    .find { it.idEncargo == idEncargo }
                if (encargo != null) {
                    try {
                        chatRepository.marcarChatLeido(idEncargo)
                        chatRepository.marcarChatLeidoPara(encargo.idAlumno, idEncargo)
                        chatRepository.marcarChatLeidoPara(encargo.idVendedor, idEncargo)
                    } catch (e: Exception) { }
                }
            }

            cargarMisEncargos()
        }
    }

    fun aplicarFiltro(estado: EstadoEncargo?) {
        _listaState.update { state ->
            state.copy(
                filtroActivo = estado,
                encargos     = if (estado == null) {
                    state.todosLosEncargos
                } else {
                    state.todosLosEncargos.filter { it.estado == estado }
                }
            )
        }
    }


}