package com.internetfacilito.yardsale.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.Timestamp
import com.internetfacilito.yardsale.data.model.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FirebaseRepository {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    // Colecciones
    private val usersCollection = firestore.collection("users")
    private val yardSalesCollection = firestore.collection("yardSales")
    private val guestSessionsCollection = firestore.collection("guestSessions")
    private val ratingsCollection = firestore.collection("ratings")
    private val reportesCollection = firestore.collection("reportes")
    
    // ==================== USUARIOS ====================
    
    /**
     * Obtiene el usuario actual
     */
    suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return getUserById(firebaseUser.uid)
    }
    
    /**
     * Obtiene un usuario por ID
     */
    suspend fun getUserById(userId: String): User? {
        return try {
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) {
                document.toObject(User::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Crea o actualiza un usuario
     */
    suspend fun saveUser(user: User): Result<User> {
        return try {
            val userData = user.copy(
                fechaActualizacion = Timestamp.now(),
                fechaCreacion = user.fechaCreacion ?: Timestamp.now()
            )
            
            val documentRef = if (user.id.isNotEmpty()) {
                usersCollection.document(user.id)
            } else {
                usersCollection.document()
            }
            
            documentRef.set(userData).await()
            Result.success(userData.copy(id = documentRef.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Registra un nuevo usuario con email y password
     */
    suspend fun registerUser(email: String, password: String, userData: User): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Error al crear usuario")
            
            val newUser = userData.copy(
                id = firebaseUser.uid,
                email = email,
                fechaRegistro = Timestamp.now(),
                fechaCreacion = Timestamp.now(),
                fechaActualizacion = Timestamp.now()
            )
            
            saveUser(newUser)
        } catch (e: Exception) {
            when {
                e.message?.contains("email address is already in use") == true -> 
                    Result.failure(e)
                e.message?.contains("password is invalid") == true -> 
                    Result.failure(e)
                e.message?.contains("network") == true -> 
                    Result.failure(Exception("error_network"))
                e.message?.contains("timeout") == true -> 
                    Result.failure(Exception("error_timeout"))
                else -> Result.failure(e)
            }
        }
    }
    
    /**
     * Inicia sesión con email y password
     */
    suspend fun signInUser(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Error al iniciar sesión")
            
            val user = getUserById(firebaseUser.uid)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cierra sesión
     */
    fun signOut() {
        auth.signOut()
    }
    
    // ==================== SESIONES DE INVITADOS ====================
    
    /**
     * Obtiene una sesión de invitado existente sin incrementar el contador
     */
    suspend fun getExistingGuestSession(deviceId: String): GuestSession? {
        return try {
            val doc = guestSessionsCollection.document(deviceId).get().await()
            if (doc.exists()) {
                doc.toObject(GuestSession::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Obtiene o crea una sesión de invitado para un dispositivo
     * No incrementa el contador si ya se alcanzó el límite
     */
    suspend fun getOrCreateGuestSession(deviceId: String): Result<GuestSession> {
        return try {
            val docRef = guestSessionsCollection.document(deviceId)
            val doc = docRef.get().await()
            
            if (doc.exists()) {
                val session = doc.toObject(GuestSession::class.java)
                if (session != null) {
                    if (session.limiteAlcanzado) {
                        // Si ya se alcanzó el límite, devolver la sesión sin modificar
                        Result.success(session)
                    } else {
                        // Incrementar contador solo si no se ha alcanzado el límite
                        val updatedSession = session.copy(
                            contadorSesiones = session.contadorSesiones + 1,
                            ultimaSesion = Timestamp.now(),
                            limiteAlcanzado = session.contadorSesiones + 1 >= 10
                        )
                        
                        docRef.set(updatedSession).await()
                        Result.success(updatedSession)
                    }
                } else {
                    Result.failure(Exception("Error al parsear sesión existente"))
                }
            } else {
                // Crear nueva sesión
                val newSession = GuestSession(
                    deviceId = deviceId,
                    contadorSesiones = 1,
                    primeraSesion = Timestamp.now(),
                    ultimaSesion = Timestamp.now(),
                    limiteAlcanzado = false
                )
                
                docRef.set(newSession).await()
                Result.success(newSession)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Elimina la sesión de invitado de un dispositivo
     */
    suspend fun deleteGuestSession(deviceId: String): Result<Unit> {
        return try {
            guestSessionsCollection.document(deviceId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== YARD SALES ====================
    
    /**
     * Obtiene todas las yard sales activas
     */
    suspend fun getActiveYardSales(): Result<List<YardSale>> {
        return try {
            val snapshot = yardSalesCollection
                .whereEqualTo("estado", YardSaleStatus.ACTIVA)
                .get()
                .await()
            
            val yardSales = snapshot.documents.mapNotNull { doc ->
                doc.toObject(YardSale::class.java)?.copy(id = doc.id)
            }
            Result.success(yardSales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene yard sales por vendedor
     */
    suspend fun getYardSalesByVendor(vendorId: String): Result<List<YardSale>> {
        return try {
            val snapshot = yardSalesCollection
                .whereEqualTo("vendedorId", vendorId)
                .get()
                .await()
            
            val yardSales = snapshot.documents.mapNotNull { doc ->
                doc.toObject(YardSale::class.java)?.copy(id = doc.id)
            }
            Result.success(yardSales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Crea o actualiza una yard sale
     */
    suspend fun saveYardSale(yardSale: YardSale): Result<YardSale> {
        return try {
            val yardSaleData = yardSale.copy(
                fechaActualizacion = Timestamp.now(),
                fechaCreacion = yardSale.fechaCreacion ?: Timestamp.now()
            )
            
            val documentRef = if (yardSale.id.isNotEmpty()) {
                yardSalesCollection.document(yardSale.id)
            } else {
                yardSalesCollection.document()
            }
            
            documentRef.set(yardSaleData).await()
            Result.success(yardSaleData.copy(id = documentRef.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== RATINGS ====================
    
    /**
     * Guarda un rating
     */
    suspend fun saveRating(rating: Rating): Result<Rating> {
        return try {
            val ratingData = rating.copy(
                fecha = Timestamp.now()
            )
            
            val documentRef = if (rating.id.isNotEmpty()) {
                ratingsCollection.document(rating.id)
            } else {
                ratingsCollection.document()
            }
            
            documentRef.set(ratingData).await()
            Result.success(ratingData.copy(id = documentRef.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== REPORTES ====================
    
    /**
     * Guarda un reporte
     */
    suspend fun saveReporte(reporte: Reporte): Result<Reporte> {
        return try {
            val reporteData = reporte.copy(
                fecha = Timestamp.now()
            )
            
            val documentRef = if (reporte.id.isNotEmpty()) {
                reportesCollection.document(reporte.id)
            } else {
                reportesCollection.document()
            }
            
            documentRef.set(reporteData).await()
            Result.success(reporteData.copy(id = documentRef.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== CONFIGURACIÓN DE USUARIO ====================
    
    /**
     * Actualiza el radio de búsqueda y unidad de distancia del usuario
     */
    suspend fun updateUserSearchRadius(userId: String, radiusKm: Float, unit: DistanceUnit): Result<Unit> {
        return try {
            val userRef = usersCollection.document(userId)
            userRef.update(
                mapOf(
                    "radioBusquedaKm" to radiusKm,
                    "unidadDistancia" to unit.name,
                    "fechaActualizacion" to com.google.firebase.Timestamp.now()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    

} 