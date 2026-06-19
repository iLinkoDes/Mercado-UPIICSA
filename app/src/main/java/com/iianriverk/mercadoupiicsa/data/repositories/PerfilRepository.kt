package com.iianriverk.mercadoupiicsa.data.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iianriverk.mercadoupiicsa.models.RolUsuario
import com.iianriverk.mercadoupiicsa.models.TipoNegocio
import kotlinx.coroutines.tasks.await

data class PerfilData(
    val idUsuario:         String     = "",
    val nombreCompleto:    String     = "",
    val boleta:            String     = "",
    val correo:            String     = "",
    val telefono:          String     = "",
    val fotoPerfilUrl:     String     = "",
    val rol:               RolUsuario = RolUsuario.ALUMNO,
    // Solo Vendedor
    val nombreNegocio:     String     = "",
    val descripcionNegocio:String     = "",
    val tipoNegocio:       TipoNegocio = TipoNegocio.OTROS,
    val fotoNegocioUrl:    String     = ""
)

class PerfilRepository {
    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getPerfil(): Result<PerfilData> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(Exception("No hay sesión activa"))
        return try {
            val doc  = firestore.collection("Usuarios").document(uid).get().await()
            val data = doc.data ?: return Result.failure(Exception("Perfil no encontrado"))
            val rol  = RolUsuario.valueOf(
                data["rol"] as? String ?: RolUsuario.ALUMNO.name
            )
            Result.success(
                PerfilData(
                    idUsuario          = doc.id,
                    nombreCompleto     = data["nombreCompleto"]     as? String ?: "",
                    boleta             = data["boleta"]             as? String ?: "",
                    correo             = data["correo"]             as? String ?: "",
                    telefono           = data["telefono"]           as? String ?: "",
                    fotoPerfilUrl      = data["fotoPerfilUrl"]      as? String ?: "",
                    rol                = rol,
                    nombreNegocio      = data["nombreNegocio"]      as? String ?: "",
                    descripcionNegocio = data["descripcionNegocio"] as? String ?: "",
                    tipoNegocio        = TipoNegocio.valueOf(
                        data["tipoNegocio"] as? String ?: TipoNegocio.OTROS.name
                    ),
                    fotoNegocioUrl     = data["fotoNegocioUrl"]     as? String ?: ""
                )
            )
        } catch (e: Exception) {
            Log.e("PERFIL_REPO", "getPerfil: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun actualizarPerfil(
        nombreCompleto:     String,
        telefono:           String,
        nombreNegocio:      String = "",
        descripcionNegocio: String = "",
        tipoNegocio:        TipoNegocio = TipoNegocio.OTROS
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(Exception("No hay sesión activa"))
        return try {
            val campos = mutableMapOf<String, Any>(
                "nombreCompleto" to nombreCompleto,
                "telefono"       to telefono
            )
            // Solo actualiza campos de negocio si es vendedor
            if (nombreNegocio.isNotBlank()) {
                campos["nombreNegocio"]      = nombreNegocio
                campos["descripcionNegocio"] = descripcionNegocio
                campos["tipoNegocio"]        = tipoNegocio.name
            }
            firestore.collection("Usuarios").document(uid).update(campos).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PERFIL_REPO", "actualizarPerfil: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    fun logout() = auth.signOut()
}