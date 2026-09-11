package com.avih6.vehiclecheck.data

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    val isOffRoad: Boolean = false,
    val offRoadDate: String? = null,
    val isEngineeringEquipment: Boolean = false,
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

    @Query("DELETE FROM vehicle_history WHERE licensePlate = :plate AND make = :make")
    suspend fun deleteByPlateAndMake(plate: String, make: String)

    @Query("DELETE FROM vehicle_history WHERE licensePlate = :plate AND isEngineeringEquipment = :isEngineering")
    suspend fun deleteByPlateAndType(plate: String, isEngineering: Boolean)

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

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE vehicle_history ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE vehicle_history ADD COLUMN isOffRoad INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE vehicle_history ADD COLUMN offRoadDate TEXT")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE vehicle_history ADD COLUMN isEngineeringEquipment INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(entities = [VehicleHistoryEntity::class], version = 4, exportSchema = false)
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
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration(true)
                .build()
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
        testStatus: TestStatus,
        isEngineeringEquipment: Boolean = false
    ) {
        val cleanPlate = plate.filter { it.isDigit() }
        val isOffRoad = testStatus is TestStatus.OffRoad || !record?.cancellationDate.isNullOrBlank()
        val offRoadDate = (testStatus as? TestStatus.OffRoad)?.offRoadDate ?: record?.cancellationDate
        val isTestValid = (testStatus is TestStatus.Valid || testStatus is TestStatus.ExpiringSoon) && !isOffRoad
        val daysUntilTest = when (testStatus) {
            is TestStatus.Valid -> testStatus.daysLeft
            is TestStatus.ExpiringSoon -> testStatus.daysLeft
            is TestStatus.Expired -> -testStatus.daysPassed
            else -> 0L
        }

        val isReallyValid = isTestValid || (record != null && record.testExpiryDate == null && testStatus !is TestStatus.Expired && !isOffRoad)
        val isTaxi = record?.vehicleCategory?.contains("מונית") == true || record?.modelType?.contains("מונית") == true
        val savedCategory = when {
            isTaxi -> "מונית"
            !record?.effectiveVehicleCategory.isNullOrBlank() -> record?.effectiveVehicleCategory
            !record?.effectiveStandardType.isNullOrBlank() -> record?.effectiveStandardType
            else -> record?.modelType
        }
        val savedOwnership = when {
            isTaxi -> if (!record?.ownership.isNullOrBlank() && record?.ownership != "פרטי") "מונית (${record?.ownership})" else "מונית (פרטי)"
            else -> record?.ownership
        }

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
            modelType = savedCategory,
            ownership = savedOwnership,
            trimLevel = record?.trimLevel,
            isOffRoad = isOffRoad,
            offRoadDate = offRoadDate,
            isEngineeringEquipment = isEngineeringEquipment,
            timestamp = System.currentTimeMillis()
        )
        dao.deleteByPlateAndType(cleanPlate, isEngineeringEquipment)
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