package com.iianriverk.mercadoupiicsa.viewModels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.iianriverk.mercadoupiicsa.data.repositories.PerfilData
import com.iianriverk.mercadoupiicsa.data.repositories.PerfilRepository
import com.iianriverk.mercadoupiicsa.data.repositories.StorageRepository
import com.iianriverk.mercadoupiicsa.models.TipoNegocio
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PerfilUiState(
    val isLoading:       Boolean    = false,
    val isSaving:        Boolean    = false,
    val isSuccess:       Boolean    = false,
    val error:           String?    = null,
    val perfil:          PerfilData? = null,
    val fotoPerfilUri:   Uri?       = null,  // imagen de perfil seleccionada aún no subida
    val fotoNegocioUri:  Uri?       = null   // imagen de negocio seleccionada aún no subida
)

class PerfilViewModel : ViewModel() {

    private val repository        = PerfilRepository()
    private val storageRepository = StorageRepository()
    private val auth              = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init { cargarPerfil() }

    fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getPerfil()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    perfil    = result.getOrNull(),
                    error     = if (result.isFailure)
                        result.exceptionOrNull()?.localizedMessage else null
                )
            }
        }
    }

    fun setFotoPerfilUri(uri: Uri?) {
        _uiState.update { it.copy(fotoPerfilUri = uri) }
    }

    fun setFotoNegocioUri(uri: Uri?) {
        _uiState.update { it.copy(fotoNegocioUri = uri) }
    }

    fun guardarCambios(
        nombreCompleto:     String,
        telefono:           String,
        nombreNegocio:      String = "",
        descripcionNegocio: String = "",
        tipoNegocio:        TipoNegocio = TipoNegocio.OTROS
    ) {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.update { it.copy(error = "Sesión no válida") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val fotoPerfilUri  = _uiState.value.fotoPerfilUri
            val fotoNegocioUri = _uiState.value.fotoNegocioUri

            var fotoPerfilUrl: String? = null
            if (fotoPerfilUri != null) {
                val result = storageRepository.uploadImage(
                    uri  = fotoPerfilUri,
                    path = "usuarios/$uid/foto_perfil.jpg"
                )
                if (result.isFailure) {
                    _uiState.update {
                        it.copy(isSaving = false, error = "Error al subir la foto de perfil")
                    }
                    return@launch
                }
                fotoPerfilUrl = result.getOrThrow()
            }

            var fotoNegocioUrl: String? = null
            if (fotoNegocioUri != null) {
                val result = storageRepository.uploadImage(
                    uri  = fotoNegocioUri,
                    path = "usuarios/$uid/foto_negocio.jpg"
                )
                if (result.isFailure) {
                    _uiState.update {
                        it.copy(isSaving = false, error = "Error al subir la foto del negocio")
                    }
                    return@launch
                }
                fotoNegocioUrl = result.getOrThrow()
            }

            val result = repository.actualizarPerfil(
                nombreCompleto     = nombreCompleto.trim(),
                telefono           = telefono.trim(),
                fotoPerfilUrl      = fotoPerfilUrl,
                nombreNegocio      = nombreNegocio.trim(),
                descripcionNegocio = descripcionNegocio.trim(),
                tipoNegocio        = tipoNegocio,
                fotoNegocioUrl     = fotoNegocioUrl
            )
            _uiState.update {
                if (result.isSuccess) it.copy(isSaving = false, isSuccess = true)
                else it.copy(
                    isSaving = false,
                    error    = result.exceptionOrNull()?.localizedMessage ?: "Error al guardar"
                )
            }
        }
    }

    fun logout() = repository.logout()

    fun resetSuccess() = _uiState.update { it.copy(isSuccess = false) }
}
