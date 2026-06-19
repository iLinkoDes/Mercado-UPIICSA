package com.iianriverk.mercadoupiicsa.models

class Producto(
    val idProducto: String = "",
    val idVendedor: String = "",
    val nombreProducto: String = "",
    val precioProducto: Double = 0.0,
    val descripcionProducto: String = "",
    val fotoProductoUrl: String = "",
    val estadoProducto: Boolean = true
)