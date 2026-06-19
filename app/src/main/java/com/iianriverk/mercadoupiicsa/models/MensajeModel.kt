package com.iianriverk.mercadoupiicsa.models

data class Mensaje(
    val idMensaje: String = "",
    val senderId:  String = "",
    val texto:     String = "",
    val timestamp: Long   = 0L
)