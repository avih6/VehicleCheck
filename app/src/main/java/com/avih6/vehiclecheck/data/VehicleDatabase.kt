package com.avih6.vehiclecheck.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import android.content.Context

@Entity(tableName = "vehicle_history")
data class VehicleHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val licensePlate: String,
    val make: String?,
    val model: String?,
    val year: Int?,
    val color: String?,
    val fuelType: String?,
    val testExpiryDate: String?,
    val isTestValid: Boolean,
    val daysUntilTest: Long,
    val modelType: String? = null,
    val ownership: String? = null,
    val trimLevel: String? = null,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicle_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<VehicleHistoryEntity>>

    @Query("SELECT * FROM vehicle_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<VehicleHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: VehicleHistoryEntity): Long

    @Query("DELETE FROM vehicle_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM vehicle_history WHERE licensePlate = :plate")
    suspend fun deleteByPlate(plate: String)

    @Query("DELETE FROM vehicle_history WHERE isFavorite = 0")
    suspend fun clearNonFavorites()

    @Query("DELETE FROM vehicle_history")
    suspend fun clearAll()

    @Query("UPDATE vehicle_history SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE vehicle_history SET isFavorite = :isFavorite WHERE licensePlate = :plate")
    suspend fun setFavoriteByPlate(plate: String, isFavorite: Boolean)
}

@Database(entities = [VehicleHistoryEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vehicle_check_db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class HistoryRepository(private val dao: VehicleDao) {
    val allHistory: Flow<List<VehicleHistoryEntity>> = dao.getAllHistory()
    val favorites: Flow<List<VehicleHistoryEntity>> = dao.getFavorites()

    suspend fun saveSearch(
        plate: String,
        record: VehicleRecord?,
        testStatus: TestStatus
    ) {
        val cleanPlate = plate.filter { it.isDigit() }
        val isTestValid = testStatus is TestStatus.Valid || testStatus is TestStatus.ExpiringSoon
        val daysUntilTest = when (testStatus) {
            is TestStatus.Valid -> testStatus.daysLeft
            is TestStatus.ExpiringSoon -> testStatus.daysLeft
            is TestStatus.Expired -> -testStatus.daysPassed
            else -> 0L
        }

        val isReallyValid = isTestValid || (record != null && record.testExpiryDate == null && testStatus !is TestStatus.Expired && testStatus !is TestStatus.OffRoad)
        val entry = VehicleHistoryEntity(
            licensePlate = cleanPlate,
            make = record?.make,
            model = record?.model,
            year = record?.year,
            color = record?.color,
            fuelType = record?.fuelType,
            testExpiryDate = record?.testExpiryDate,
            isTestValid = isReallyValid,
            daysUntilTest = daysUntilTest,
            modelType = record?.effectiveVehicleCategory ?: record?.modelType,
            ownership = record?.ownership,
            trimLevel = record?.trimLevel,
            timestamp = System.currentTimeMillis()
        )
        dao.deleteByPlate(cleanPlate)
        dao.insert(entry)
    }

    suspend fun saveNotFoundSearch(plate: String) {
        val cleanPlate = plate.filter { it.isDigit() }
        if (cleanPlate.length !in 5..8) return
        val entry = VehicleHistoryEntity(
            licensePlate = cleanPlate,
            make = "לא אותר במאגר",
            model = "לחץ לבדיקה חוזרת",
            year = null,
            color = null,
            fuelType = null,
            testExpiryDate = null,
            isTestValid = false,
            daysUntilTest = 0L,
            timestamp = System.currentTimeMillis()
        )
        dao.deleteByPlate(cleanPlate)
        dao.insert(entry)
    }

    suspend fun toggleFavorite(id: Long, currentStatus: Boolean) {
        dao.setFavorite(id, !currentStatus)
    }

    suspend fun toggleFavoriteByPlate(plate: String, isFavorite: Boolean) {
        dao.setFavoriteByPlate(plate, isFavorite)
    }

    suspend fun delete(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}