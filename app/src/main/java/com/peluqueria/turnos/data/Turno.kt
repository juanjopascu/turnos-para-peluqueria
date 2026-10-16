package com.peluqueria.turnos.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "turnos")
data class Turno(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clienteNombre: String,
    val clienteEmail: String,
    val clienteTelefono: String,
    val servicio: String,
    val fechaHoraMillis: Long,
    val duracionMinutos: Int = 30,
    val notas: String = "",
    val estado: String = "CONFIRMADO", // CONFIRMADO, PENDIENTE, CANCELADO
    val calendarEventId: Long? = null
)

@Dao
interface TurnoDao {
    @Query("SELECT * FROM turnos ORDER BY fechaHoraMillis ASC")
    fun obtenerTodos(): Flow<List<Turno>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(turno: Turno): Long

    @Update
    suspend fun actualizar(turno: Turno)

    @Delete
    suspend fun eliminar(turno: Turno)
}

@Database(entities = [Turno::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun turnoDao(): TurnoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "turnos_peluqueria_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
