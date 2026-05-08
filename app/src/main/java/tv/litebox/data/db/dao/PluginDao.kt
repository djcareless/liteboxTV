package tv.litebox.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import tv.litebox.data.db.entity.PluginEntity

@Dao
interface PluginDao {
    @Query("SELECT * FROM plugins ORDER BY installedAt DESC")
    fun observeAll(): Flow<List<PluginEntity>>

    @Query("SELECT * FROM plugins WHERE enabled = 1 ORDER BY installedAt DESC")
    fun observeEnabled(): Flow<List<PluginEntity>>

    @Query("SELECT * FROM plugins WHERE id = :id")
    suspend fun getById(id: String): PluginEntity?

    @Upsert
    suspend fun upsert(plugin: PluginEntity)

    @Query("UPDATE plugins SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)

    @Query("UPDATE plugins SET settings = :settings WHERE id = :id")
    suspend fun updateSettings(id: String, settings: Map<String, String>)

    @Query("DELETE FROM plugins WHERE id = :id")
    suspend fun deleteById(id: String)
}
