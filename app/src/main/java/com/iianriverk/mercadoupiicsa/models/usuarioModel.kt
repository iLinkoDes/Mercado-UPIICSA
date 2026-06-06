package com.iianriverk.mercadoupiicsa.models

open class AlumnoModel(
    val idAlumno : String,
    val nombreCompleto : String,
    val correo : String,
    val telefono : String,
    val fotoPerfilUrl : String,
    val rol : RolUsuario
) {

}

class vendedorModel (
    idAlumno: String,
    nombreCompleto: String,
    correo: String,
    telefono: String,
    fotoPerfilUrl: String,
    rol: RolUsuario,

    val idNegocio : String,
    val nombreNegocio : String,
    val fotoNegocioUrl : String,
    val descripcionNegocio : String,
    val tipoNegocio : TipoNegocio,
) : AlumnoModel(idAlumno, nombreCompleto, correo, telefono, fotoPerfilUrl, rol){

}

enum class RolUsuario { ALUMNO, VENDEDOR }
enum class TipoNegocio { ACCESORIOS, BEBIDAS, BOTANAS, ROPA, SERVICIOS, OTROS }