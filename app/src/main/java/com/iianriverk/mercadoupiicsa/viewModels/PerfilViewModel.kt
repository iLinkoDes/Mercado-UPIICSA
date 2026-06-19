package com.iianriverk.mercadoupiicsa.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iianriverk.mercadoupiicsa.data.repositories.PerfilData
import com.iianriverk.mercadoupiicsa.data.repositories.PerfilRepository
import com.iianriverk.mercadoupiicsa.models.TipoNegocio
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PerfilUiState(
    val isLoading:  Boolean    = false,
    val isSaving:   Boolean    = false,
    val isSuccess:  Boolean    = false,
    val error:      String?    = null,
    val perfil:     PerfilData? = null
)

class PerfilViewModel : ViewModel() {

    private val repository = PerfilRepository()

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

    fun guardarCambios(
        nombreCompleto:     String,
        telefono:           String,
        nombreNegocio:      String = "",
        descripcionNegocio: String = "",
        tipoNegocio:        TipoNegocio = TipoNegocio.OTROS
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = repository.actualizarPerfil(
                nombreCompleto     = nombreCompleto.trim(),
                telefono           = telefono.trim(),
                nombreNegocio      = nombreNegocio.trim(),
                descripcionNegocio = descripcionNegocio.trim(),
                tipoNegocio        = tipoNegocio
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