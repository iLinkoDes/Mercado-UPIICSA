package com.iianriverk.mercadoupiicsa.views.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.iianriverk.mercadoupiicsa.models.Mensaje
import com.iianriverk.mercadoupiicsa.viewModels.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    idEncargo:      String,
    idReceptor:     String,
    nombreProducto: String,
    navController:  NavController,
    viewModel:      ChatViewModel = viewModel()
) {
    val uiState  by viewModel.uiState.collectAsState()
    var texto    by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(idEncargo) {
        viewModel.iniciar(idEncargo)
    }

    // Auto-scroll al último mensaje
    LaunchedEffect(uiState.mensajes.size) {
        if (uiState.mensajes.isNotEmpty()) {
            listState.animateScrollToItem(uiState.mensajes.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(nombreProducto, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Chat del encargo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value         = texto,
                        onValueChange = { texto = it },
                        placeholder   = { Text("Escribe un mensaje...") },
                        shape         = MaterialTheme.shapes.extraLarge,
                        maxLines      = 3,
                        modifier      = Modifier.weight(1f)
                    )
                    FilledIconButton(
                        onClick  = {
                            viewModel.enviarMensaje(idEncargo, texto, idReceptor)
                            texto = ""
                        },
                        enabled  = texto.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Enviar")
                    }
                }
            }
        }
    ) { innerPadding ->

        when {
            uiState.isLoading && uiState.mensajes.isEmpty() -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            else -> {
                LazyColumn(
                    state          = listState,
                    contentPadding = PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        top    = innerPadding.calculateTopPadding() + 8.dp,
                        bottom = innerPadding.calculateBottomPadding() + 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.mensajes.isEmpty()) {
                        item {
                            Box(
                                Modifier
                                    .fillParentMaxWidth()
                                    .padding(top = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Aún no hay mensajes.\n¡Empieza la conversación!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    items(uiState.mensajes, key = { it.idMensaje }) { mensaje ->
                        MensajeBurbuja(
                            mensaje = mensaje,
                            esMio   = mensaje.senderId == viewModel.currentUserId
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MensajeBurbuja(mensaje: Mensaje, esMio: Boolean) {
    val hora = remember(mensaje.timestamp) {
        SimpleDateFormat("HH:mm", Locale("es", "MX")).format(Date(mensaje.timestamp))
    }
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = if (esMio) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (esMio) Alignment.End else Alignment.Start) {
            Surface(
                shape = RoundedCornerShape(
                    topStart    = 16.dp,
                    topEnd      = 16.dp,
                    bottomStart = if (esMio) 16.dp else 4.dp,
                    bottomEnd   = if (esMio) 4.dp else 16.dp
                ),
                color    = if (esMio) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text     = mensaje.texto,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = if (esMio) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(hora, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}