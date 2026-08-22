package com.ben.khata.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.PrimaryKey
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "people", indices = [Index(value = ["name"], unique = true)])
data class Person(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val createdAt: Long = System.currentTimeMillis())

@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(entity = Person::class, parentColumns = ["id"], childColumns = ["personId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("personId")]
)
data class KhataTransaction(@PrimaryKey(autoGenerate = true) val id: Long = 0, val personId: Long, val amount: Long, val note: String = "", val imageUri: String? = null, val createdAt: Long = System.currentTimeMillis())

data class PersonSummary(val id: Long, val name: String, val total: Long, val entryCount: Long)

@Entity(tableName = "app_settings")
data class AppSettings(@PrimaryKey val id: Int = 1, val totalBudget: Long = 0)

data class TransactionWithPerson(
    val id: Long,
    val personId: Long,
    val amount: Long,
    val note: String,
    val imageUri: String?,
    val createdAt: Long,
    val personName: String
)

@Dao
interface KhataDao {
    @Query("SELECT p.id, p.name, COALESCE(SUM(t.amount), 0) AS total, COUNT(t.id) AS entryCount FROM people p LEFT JOIN transactions t ON p.id = t.personId GROUP BY p.id ORDER BY p.name COLLATE NOCASE")
    fun people(): Flow<List<PersonSummary>>

    @Query("SELECT p.id, p.name, COALESCE(SUM(t.amount), 0) AS total, COUNT(t.id) AS entryCount FROM people p LEFT JOIN transactions t ON p.id = t.personId WHERE p.id = :personId GROUP BY p.id")
    fun person(personId: Long): Flow<PersonSummary?>

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC LIMIT :limit")
    fun recent(limit: Int = 5): Flow<List<KhataTransaction>>

    @Query("SELECT * FROM transactions WHERE personId = :personId ORDER BY createdAt DESC")
    fun transactions(personId: Long): Flow<List<KhataTransaction>>

    @Query("SELECT t.id, t.personId, t.amount, t.note, t.createdAt, t.imageUri, p.name AS personName FROM transactions t INNER JOIN people p ON p.id = t.personId ORDER BY t.createdAt DESC")
    fun history(): Flow<List<TransactionWithPerson>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions") fun total(): Flow<Long>
    @Query("SELECT COUNT(*) FROM people") fun peopleCount(): Flow<Int>
    @Query("SELECT COALESCE((SELECT totalBudget FROM app_settings WHERE id = 1), 0) + COALESCE((SELECT SUM(amount) FROM transactions), 0)") fun budget(): Flow<Long?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveSettings(settings: AppSettings)
    @Query("UPDATE app_settings SET totalBudget = totalBudget + :amount WHERE id = 1") suspend fun increaseBudget(amount: Long): Int
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun addPerson(person: Person): Long
    @Insert suspend fun addTransaction(transaction: KhataTransaction): Long
    @Query("UPDATE transactions SET amount = :amount, note = :note WHERE id = :id") suspend fun updateTransaction(id: Long, amount: Long, note: String)
    @Query("DELETE FROM transactions WHERE id = :id") suspend fun deleteTransaction(id: Long)
    @Query("DELETE FROM people WHERE id = :id") suspend fun deletePerson(id: Long)

    @Transaction
    suspend fun addEntry(name: String, amount: Long, note: String, existingPersonId: Long? = null, imageUri: String? = null) {
        val personId = existingPersonId ?: run {
            val cleanName = name.trim()
            personIdByNormalizedName(cleanName) ?: addPerson(Person(name = cleanName)).let { insertedId ->
                if (insertedId == -1L) personIdByNormalizedName(cleanName) ?: error("Unable to create person") else insertedId
            }
        }
        addTransaction(KhataTransaction(personId = personId, amount = amount, note = note.trim(), imageUri = imageUri))
    }

    @Transaction
    suspend fun addToBudget(amount: Long) {
        if (increaseBudget(amount) == 0) saveSettings(AppSettings(totalBudget = amount))
    }
    @Query("SELECT id FROM people WHERE lower(trim(name)) = lower(trim(:name)) LIMIT 1") suspend fun personIdByNormalizedName(name: String): Long?

    @Query("SELECT r.*, p.name AS personName FROM reminders r INNER JOIN people p ON p.id = r.personId ORDER BY r.dueDate ASC")
    fun reminders(): Flow<List<ReminderWithPerson>>

    @Insert suspend fun addReminder(reminder: Reminder): Long

    @Query("UPDATE reminders SET status = :status WHERE id = :id") suspend fun updateReminderStatus(id: Long, status: Int)
    
    @Query("UPDATE reminders SET type = :type, amount = :amount, dueDate = :dueDate, reminderTiming = :timing, note = :note WHERE id = :id")
    suspend fun updateReminder(id: Long, type: Int, amount: Long, dueDate: Long, timing: Int, note: String)

    @Query("DELETE FROM reminders WHERE id = :id") suspend fun deleteReminder(id: Long)
    
    @Query("SELECT r.*, p.name AS personName FROM reminders r INNER JOIN people p ON p.id = r.personId WHERE r.id = :id LIMIT 1")
    suspend fun getReminderById(id: Long): ReminderWithPerson?
}

@Database(entities = [Person::class, KhataTransaction::class, AppSettings::class, Reminder::class], version = 4, exportSchema = false)
abstract class KhataDatabase : RoomDatabase() {
    abstract fun dao(): KhataDao
    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS app_settings (id INTEGER NOT NULL, totalBudget INTEGER NOT NULL, PRIMARY KEY(id))")
            }
        }
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `reminders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `personId` INTEGER NOT NULL, `type` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `dueDate` INTEGER NOT NULL, `reminderTiming` INTEGER NOT NULL, `note` TEXT NOT NULL, `status` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, FOREIGN KEY(`personId`) REFERENCES `people`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_personId` ON `reminders` (`personId`)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `transactions` ADD COLUMN `imageUri` TEXT DEFAULT NULL")
            }
        }

        @Volatile
        private var INSTANCE: KhataDatabase? = null

        fun create(context: android.content.Context): KhataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(context.applicationContext, KhataDatabase::class.java, "khata.db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
