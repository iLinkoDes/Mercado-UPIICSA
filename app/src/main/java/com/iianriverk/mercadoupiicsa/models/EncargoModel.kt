package com.iianriverk.mercadoupiicsa.models

data class Encargo(
    val idEncargo:       String        = "",
    val idVendedor:      String        = "",
    val idAlumno:        String        = "",
    // Snapshot del producto al momento del encargo
    // (por si el vendedor lo edita después)
    val nombreProducto:  String        = "",
    val precioUnitario:  Double        = 0.0,
    val cantidad:        Int           = 1,
    val total:           Double        = 0.0,
    val notas:           String        = "",
    val estado:          EstadoEncargo = EstadoEncargo.PENDIENTE,
    val fechaCreacion:   Long          = 0L
)

enum class EstadoEncargo { PENDIENTE, CONFIRMADO, RECHAZADO }