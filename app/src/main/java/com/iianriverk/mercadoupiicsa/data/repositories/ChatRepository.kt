package com.iianriverk.mercadoupiicsa.data.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.iianriverk.mercadoupiicsa.models.Mensaje
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val auth     = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    // Mensaje en Tienda
    fun getMensajes(idEncargo: String): Flow<List<Mensaje>> = callbackFlow {
        val ref = database.getReference("chats/$idEncargo/messages")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mensajes = snapshot.children.mapNotNull { child ->
                    try {
                        Mensaje(
                            idMensaje = child.key ?: "",
                            senderId  = child.child("senderId").getValue(String::class.java) ?: "",
                            texto     = child.child("texto").getValue(String::class.java) ?: "",
                            timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                        )
                    } catch (e: Exception) { null }
                }.sortedBy { it.timestamp }
                trySend(mensajes)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun enviarMensaje(
        idEncargo:  String,
        texto:      String,
        idReceptor: String
    ) {
        val uid = auth.currentUser?.uid ?: return
        val mensaje = mapOf(
            "senderId"  to uid,
            "texto"     to texto,
            "timestamp" to System.currentTimeMillis()
        )
        database.getReference("chats/$idEncargo/messages")
            .push().setValue(mensaje).await()

        // Marca notificación para el receptor
        database.getReference("notifications/$idReceptor/$idEncargo")
            .setValue(true).await()
    }

    fun getBadgeCount(userId: String): Flow<Int> = callbackFlow {
        val ref = database.getReference("notifications/$userId")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.childrenCount.toInt())
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun marcarChatLeido(idEncargo: String) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference("notifications/$uid/$idEncargo")
            .removeValue().await()
    }

    suspend fun marcarChatLeidoPara(userId: String, idEncargo: String) {
        database.getReference("notifications/$userId/$idEncargo")
            .removeValue().await()
    }

    fun getEncargosConNotificacion(userId: String): Flow<Set<String>> = callbackFlow {
        val ref = database.getReference("notifications/$userId")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ids = snapshot.children.mapNotNull { it.key }.toSet()
                trySend(ids)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}