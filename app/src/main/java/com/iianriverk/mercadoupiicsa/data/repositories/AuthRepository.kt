package com.iianriverk.mercadoupiicsa.data.repositories

import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.iianriverk.mercadoupiicsa.models.Alumno
import com.iianriverk.mercadoupiicsa.models.RolUsuario
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthRepository{
    private val auth : FirebaseAuth = Firebase.auth
    private val firestore : FirebaseFirestore = Firebase.firestore

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("ERROR EN LOGIN", "${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun createUser(email: String, password: String, username: String): Result<Unit>{
        return  try {
            auth.createUserWithEmailAndPassword(email,password).await()
            Result.success(Unit)
        } catch (e: Exception){
            Log.d("ERROR EN REGISTRO", "${e.localizedMessage}")
            Result.failure(e)
        }
    }

    fun saveUser(username: String, nombre: String, telefono: String, fotoPerfilUrl: String?, rol: RolUsuario): Result<Unit>{
        val id = auth.currentUser?.uid.orEmpty()
        val nombreCompleto = nombre
        val email = auth.currentUser?.email.orEmpty()
        val telefono = telefono
        val fotoPerfilUrl = fotoPerfilUrl.orEmpty()
        val rol = rol

        val user = Alumno(
            idAlumno = id,
            nombreCompleto = nombreCompleto,
            correo = email,
            telefono = telefono,
            fotoPerfilUrl = fotoPerfilUrl,
            rol = rol

        )

        return try {
            FirebaseFirestore.getInstance().collection("Usuarios")
                .add(user)
            Result.success(Unit)
        } catch (e: Exception){
            Result.failure(e)
        }




    }
}