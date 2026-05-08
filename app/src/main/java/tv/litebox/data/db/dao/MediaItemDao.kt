package tv.litebox.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import tv.litebox.data.db.entity.MediaItemEntity

@Dao
interface MediaItemDao {
    @Query("SELECT * FROM media_items ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE type = :type ORDER BY addedAt DESC")
    fun observeByType(type: String): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE watched = 0 AND resumePosition > 0 ORDER BY addedAt DESC LIMIT 20")
    fun observeContinueWatching(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getById(id: String): MediaItemEntity?

    @Query("SELECT * FROM media_items WHERE title LIKE '%' || :query || '%' LIMIT 50")
    suspend fun search(query: String): List<MediaItemEntity>

    @Upsert
    suspend fun upsert(item: MediaItemEntity)

    @Upsert
    suspend fun upsertAll(items: List<MediaItemEntity>)

    @Query("UPDATE media_items SET resumePosition = :position WHERE id = :id")
    suspend fun updateResumePosition(id: String, position: Long)

    @Query("UPDATE media_items SET watched = 1, resumePosition = 0 WHERE id = :id")
    suspend fun markWatched(id: String)

    @Delete
    suspend fun delete(item: MediaItemEntity)

    @Query("DELETE FROM media_items WHERE sourcePlugin = :pluginId")
    suspend fun deleteByPlugin(pluginId: String)
}
