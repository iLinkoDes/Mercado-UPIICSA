package com.iianriverk.mercadoupiicsa.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iianriverk.mercadoupiicsa.data.repositories.AuthRepository
import com.iianriverk.mercadoupiicsa.models.RolUsuario
import com.iianriverk.mercadoupiicsa.models.TipoNegocio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

// Estado del formulario de registro (Registro y Pre)
data class RegisterFormState(
    val email: String = "",
    val password: String = "",
    val rol: RolUsuario = RolUsuario.ALUMNO
)

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _registerForm = MutableStateFlow(RegisterFormState())
    val registerForm: StateFlow<RegisterFormState> = _registerForm.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { AuthUiState(isLoading = true) }
            val result = repository.login(email, password)
            _uiState.update {
                if (result.isSuccess) AuthUiState(isSuccess = true)
                else AuthUiState(error = result.exceptionOrNull()?.localizedMessage ?: "Error al iniciar sesión")
            }
        }
    }

    // Pre-Registro
    fun saveStep1(email: String, password: String, rol: RolUsuario) {
        _registerForm.update { it.copy(email = email, password = password, rol = rol) }
    }

    // Registro
    fun register(
        nombreCompleto: String,
        boleta: String,
        telefono: String,
        nombreNegocio: String = "",
        descripcionNegocio: String = "",
        tipoNegocio: TipoNegocio = TipoNegocio.OTROS
    ) {
        val form = _registerForm.value
        viewModelScope.launch {
            _uiState.update { AuthUiState(isLoading = true) }

            val createResult = repository.createUser(form.email, form.password)
            if (createResult.isFailure) {
                _uiState.update {
                    AuthUiState(error = createResult.exceptionOrNull()?.localizedMessage ?: "Error al crear cuenta")
                }
                return@launch
            }

            val saveResult = repository.saveUser(
                nombreCompleto    = nombreCompleto,
                boleta            = boleta,
                telefono          = telefono,
                rol               = form.rol,
                nombreNegocio     = nombreNegocio,
                descripcionNegocio = descripcionNegocio,
                tipoNegocio       = tipoNegocio
            )

            _uiState.update {
                if (saveResult.isSuccess) AuthUiState(isSuccess = true)
                else AuthUiState(error = saveResult.exceptionOrNull()?.localizedMessage ?: "Error al guardar datos")
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun resetState() = _uiState.update { AuthUiState() }
    fun isLoggedIn() = repository.isLoggedIn()
}