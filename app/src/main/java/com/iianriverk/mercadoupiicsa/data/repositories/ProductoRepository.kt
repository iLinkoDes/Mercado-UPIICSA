package com.iianriverk.mercadoupiicsa.data.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.iianriverk.mercadoupiicsa.models.Producto
import kotlinx.coroutines.tasks.await

class ProductoRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("Productos")

    // Vista pública, solo productos disponibles
    suspend fun getProductosDeVendedor(idVendedor: String): Result<List<Producto>> {
        return try {
            val snapshot = collection
                .whereEqualTo("idVendedor", idVendedor)
                .whereEqualTo("estadoProducto", true)
                .get().await()
            Result.success(mapearProductos(snapshot.documents))
        } catch (e: Exception) {
            Log.e("PRODUCTO_REPO", "getProductos: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    // Vista del vendedor, incluye productos no disponibles
    suspend fun getMisProductos(idVendedor: String): Result<List<Producto>> {
        return try {
            val snapshot = collection
                .whereEqualTo("idVendedor", idVendedor)
                .get().await()
            Result.success(mapearProductos(snapshot.documents))
        } catch (e: Exception) {
            Log.e("PRODUCTO_REPO", "getMisProductos: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun getProductoPorId(idProducto: String): Result<Producto> {
        return try {
            val doc = collection.document(idProducto).get().await()
            val producto = mapearProducto(doc.id, doc.data ?: emptyMap())
                ?: return Result.failure(Exception("Producto no encontrado"))
            Result.success(producto)
        } catch (e: Exception) {
            Log.e("PRODUCTO_REPO", "getProductoPorId: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun agregarProducto(producto: Producto): Result<Unit> {
        return try {
            collection.add(producto.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PRODUCTO_REPO", "agregar: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun editarProducto(producto: Producto): Result<Unit> {
        return try {
            collection.document(producto.idProducto).update(producto.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PRODUCTO_REPO", "editar: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun eliminarProducto(idProducto: String): Result<Unit> {
        return try {
            collection.document(idProducto).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PRODUCTO_REPO", "eliminar: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    private fun mapearProductos(
        docs: List<com.google.firebase.firestore.DocumentSnapshot>
    ): List<Producto> = docs.mapNotNull { doc ->
        try { mapearProducto(doc.id, doc.data ?: emptyMap()) }
        catch (e: Exception) { null }
    }

    private fun mapearProducto(id: String, data: Map<String, Any?>): Producto? {
        return Producto(
            idProducto          = id,
            idVendedor          = data["idVendedor"] as? String ?: "",
            nombreProducto      = data["nombreProducto"] as? String ?: "",
            precioProducto      = (data["precioProducto"] as? Number)?.toDouble() ?: 0.0,
            descripcionProducto = data["descripcionProducto"] as? String ?: "",
            fotoProductoUrl     = data["fotoProductoUrl"] as? String ?: "",
            estadoProducto      = data["estadoProducto"] as? Boolean ?: true
        )
    }

    private fun Producto.toMap() = mapOf(
        "idVendedor"          to idVendedor,
        "nombreProducto"      to nombreProducto,
        "precioProducto"      to precioProducto,
        "descripcionProducto" to descripcionProducto,
        "fotoProductoUrl"     to fotoProductoUrl,
        "estadoProducto"      to estadoProducto
    )
}