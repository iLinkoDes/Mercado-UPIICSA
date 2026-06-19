package com.iianriverk.mercadoupiicsa.data.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.iianriverk.mercadoupiicsa.models.Encargo
import com.iianriverk.mercadoupiicsa.models.EstadoEncargo
import kotlinx.coroutines.tasks.await

class EncargosRepository {
    private val firestore  = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("Encargos")

    suspend fun crearEncargo(encargo: Encargo): Result<Unit> {
        return try {
            collection.add(encargo.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ENCARGO_REPO", "crearEncargo: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    // Encargos que el alumno envió
    suspend fun getEncargosComoAlumno(idAlumno: String): Result<List<Encargo>> {
        return try {
            val snapshot = collection
                .whereEqualTo("idAlumno", idAlumno)
                // ← sin orderBy
                .get().await()
            Result.success(
                mapearEncargos(snapshot.documents)
                    .sortedByDescending { it.fechaCreacion } // orden local
            )
        } catch (e: Exception) {
            Log.e("ENCARGO_REPO", "getAlumno: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    // Encargos que el vendedor recibió
    suspend fun getEncargosComoVendedor(idVendedor: String): Result<List<Encargo>> {
        return try {
            val snapshot = collection
                .whereEqualTo("idVendedor", idVendedor)
                // ← sin orderBy
                .get().await()
            Result.success(
                mapearEncargos(snapshot.documents)
                    .sortedByDescending { it.fechaCreacion } // orden local
            )
        } catch (e: Exception) {
            Log.e("ENCARGO_REPO", "getVendedor: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun actualizarEstado(idEncargo: String, estado: EstadoEncargo): Result<Unit> {
        return try {
            collection.document(idEncargo)
                .update("estado", estado.name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ENCARGO_REPO", "actualizarEstado: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    // ── Helpers ──────────────────────────────────────────

    private fun mapearEncargos(
        docs: List<com.google.firebase.firestore.DocumentSnapshot>
    ): List<Encargo> = docs.mapNotNull { doc ->
        try {
            val data = doc.data ?: return@mapNotNull null
            Encargo(
                idEncargo      = doc.id,
                idVendedor     = data["idVendedor"]     as? String ?: "",
                idAlumno       = data["idAlumno"]       as? String ?: "",
                nombreProducto = data["nombreProducto"] as? String ?: "",
                precioUnitario = (data["precioUnitario"] as? Number)?.toDouble() ?: 0.0,
                cantidad       = (data["cantidad"]       as? Number)?.toInt()    ?: 1,
                total          = (data["total"]          as? Number)?.toDouble() ?: 0.0,
                notas          = data["notas"]           as? String ?: "",
                estado         = EstadoEncargo.valueOf(
                    data["estado"] as? String ?: EstadoEncargo.PENDIENTE.name
                ),
                fechaCreacion  = (data["fechaCreacion"]  as? Number)?.toLong()   ?: 0L
            )
        } catch (e: Exception) {
            Log.e("ENCARGO_REPO", "mapeo: ${e.localizedMessage}")
            null
        }
    }

    private fun Encargo.toMap() = mapOf(
        "idVendedor"     to idVendedor,
        "idAlumno"       to idAlumno,
        "nombreProducto" to nombreProducto,
        "precioUnitario" to precioUnitario,
        "cantidad"       to cantidad,
        "total"          to total,
        "notas"          to notas,
        "estado"         to estado.name,
        "fechaCreacion"  to System.currentTimeMillis()
    )
}