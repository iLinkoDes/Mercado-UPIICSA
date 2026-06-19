package com.iianriverk.mercadoupiicsa.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.iianriverk.mercadoupiicsa.data.repositories.ChatRepository
import com.iianriverk.mercadoupiicsa.models.Mensaje
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatUiState(
    val mensajes:  List<Mensaje> = emptyList(),
    val isLoading: Boolean       = false,
    val error:     String?       = null
)

class ChatViewModel : ViewModel() {

    private val repository    = ChatRepository()
    val currentUserId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun iniciar(idEncargo: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try { repository.marcarChatLeido(idEncargo) } catch (e: Exception) { }

            repository.getMensajes(idEncargo)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
                .collect { mensajes ->
                    _uiState.update { it.copy(isLoading = false, mensajes = mensajes) }
                }
        }
    }

    fun enviarMensaje(idEncargo: String, texto: String, idReceptor: String) {
        if (texto.isBlank()) return
        viewModelScope.launch {
            try {
                repository.enviarMensaje(idEncargo, texto.trim(), idReceptor)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al enviar: ${e.localizedMessage}") }
            }
        }
    }
}