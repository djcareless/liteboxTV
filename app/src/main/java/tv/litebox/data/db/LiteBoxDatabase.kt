package tv.litebox.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tv.litebox.data.db.dao.MediaItemDao
import tv.litebox.data.db.dao.MediaSourceDao
import tv.litebox.data.db.dao.PluginDao
import tv.litebox.data.db.entity.MediaItemEntity
import tv.litebox.data.db.entity.MediaSourceEntity
import tv.litebox.data.db.entity.PluginEntity

@Database(
    entities = [
        MediaItemEntity::class,
        MediaSourceEntity::class,
        PluginEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class LiteBoxDatabase : RoomDatabase() {

    abstract fun mediaItemDao(): MediaItemDao
    abstract fun mediaSourceDao(): MediaSourceDao
    abstract fun pluginDao(): PluginDao

    companion object {
        @Volatile private var INSTANCE: LiteBoxDatabase? = null

        fun getInstance(context: Context): LiteBoxDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LiteBoxDatabase::class.java,
                    "litebox.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
