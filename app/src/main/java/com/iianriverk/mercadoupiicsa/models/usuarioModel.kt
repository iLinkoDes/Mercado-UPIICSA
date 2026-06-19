package com.iianriverk.mercadoupiicsa.models

open class Alumno(
    open val idAlumno: String = "",
    open val nombreCompleto: String = "",
    open val boleta: String = "",
    open val correo: String = "",
    open val telefono: String = "",
    open val fotoPerfilUrl: String = "",
    open val rol: RolUsuario = RolUsuario.ALUMNO
)

class Vendedor(
    idAlumno: String = "",
    nombreCompleto: String = "",
    boleta: String = "",
    correo: String = "",
    telefono: String = "",
    fotoPerfilUrl: String = "",
    rol: RolUsuario = RolUsuario.VENDEDOR,
    val nombreNegocio: String = "",
    val fotoNegocioUrl: String = "",
    val descripcionNegocio: String = "",
    val tipoNegocio: TipoNegocio = TipoNegocio.OTROS
) : Alumno(idAlumno, nombreCompleto, boleta, correo, telefono, fotoPerfilUrl, rol)

enum class RolUsuario { ALUMNO, VENDEDOR }
enum class TipoNegocio { ACCESORIOS, BEBIDAS, BOTANAS, DULCES, ROPA, SERVICIOS, OTROS }