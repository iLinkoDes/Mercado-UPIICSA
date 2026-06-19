package com.iianriverk.mercadoupiicsa.data.repositories

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.iianriverk.mercadoupiicsa.models.RolUsuario
import com.iianriverk.mercadoupiicsa.models.TipoNegocio
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AUTH_REPO", "login: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun createUser(email: String, password: String): Result<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AUTH_REPO", "createUser: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun saveUser(
        nombreCompleto: String,
        boleta: String,
        telefono: String,
        fotoPerfilUrl: String = "",
        rol: RolUsuario,
        nombreNegocio: String = "",
        descripcionNegocio: String = "",
        tipoNegocio: TipoNegocio = TipoNegocio.OTROS
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(Exception("Usuario no autenticado"))
        val email = auth.currentUser?.email.orEmpty()

        val data = mutableMapOf<String, Any>(
            "idAlumno"       to uid,
            "nombreCompleto" to nombreCompleto,
            "boleta"         to boleta,
            "correo"         to email,
            "telefono"       to telefono,
            "fotoPerfilUrl"  to fotoPerfilUrl,
            "rol"            to rol.name
        )

        if (rol == RolUsuario.VENDEDOR) {
            data["nombreNegocio"]     = nombreNegocio
            data["descripcionNegocio"] = descripcionNegocio
            data["tipoNegocio"]       = tipoNegocio.name
            data["fotoNegocioUrl"]    = ""
        }

        return try {
            firestore.collection("Usuarios").document(uid).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AUTH_REPO", "saveUser: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<Boolean> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result     = auth.signInWithCredential(credential).await()
            val esNuevo    = result.additionalUserInfo?.isNewUser ?: false
            if (!esNuevo) {
                // Usuario existente: verifica que tenga perfil en Firestore
                val doc = firestore.collection("Usuarios")
                    .document(auth.currentUser!!.uid).get().await()
                Result.success(!doc.exists())
            } else {
                Result.success(true) // nuevo, necesita completar perfil
            }
        } catch (e: Exception) {
            Log.e("AUTH_REPO", "signInWithGoogle: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun signInWithFacebook(token: com.facebook.AccessToken): Result<Boolean> {
        return try {
            val credential = FacebookAuthProvider.getCredential(token.token)
            val result     = auth.signInWithCredential(credential).await()
            val esNuevo    = result.additionalUserInfo?.isNewUser ?: false
            if (!esNuevo) {
                val doc = firestore.collection("Usuarios")
                    .document(auth.currentUser!!.uid).get().await()
                Result.success(!doc.exists())
            } else {
                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e("AUTH_REPO", "signInWithFacebook: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    fun getCurrentUser() = auth.currentUser

    fun getCurrentUserId(): String? = auth.currentUser?.uid
    fun isLoggedIn(): Boolean = auth.currentUser != null
    fun logout() = auth.signOut()
}