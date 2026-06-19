package com.iianriverk.mercadoupiicsa.data.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.iianriverk.mercadoupiicsa.models.TipoNegocio
import kotlinx.coroutines.tasks.await

data class VendedorResumen(
    val idVendedor: String = "",
    val nombreNegocio: String = "",
    val descripcionNegocio: String = "",
    val fotoNegocioUrl: String = "",
    val tipoNegocio: TipoNegocio = TipoNegocio.OTROS
)

class FeedRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getVendedores(): Result<List<VendedorResumen>> {
        return try {
            val snapshot = firestore
                .collection("Usuarios")
                .whereEqualTo("rol", "VENDEDOR")
                .get()
                .await()

            val vendedores = snapshot.documents.mapNotNull { doc ->
                try {
                    VendedorResumen(
                        idVendedor        = doc.id,
                        nombreNegocio     = doc.getString("nombreNegocio").orEmpty(),
                        descripcionNegocio = doc.getString("descripcionNegocio").orEmpty(),
                        fotoNegocioUrl    = doc.getString("fotoNegocioUrl").orEmpty(),
                        tipoNegocio       = TipoNegocio.valueOf(
                            doc.getString("tipoNegocio") ?: TipoNegocio.OTROS.name
                        )
                    )
                } catch (e: Exception) {
                    Log.e("FEED_REPO", "Error mapeando vendedor ${doc.id}: ${e.localizedMessage}")
                    null // Si existe malformacion, sale sin crashear la app
                }
            }

            Result.success(vendedores)
        } catch (e: Exception) {
            Log.e("FEED_REPO", "getVendedores: ${e.localizedMessage}")
            Result.failure(e)
        }
    }
}