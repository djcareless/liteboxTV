package tv.litebox.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import tv.litebox.data.db.entity.MediaSourceEntity

@Dao
interface MediaSourceDao {
    @Query("SELECT * FROM media_sources ORDER BY name ASC")
    fun observeAll(): Flow<List<MediaSourceEntity>>

    @Query("SELECT * FROM media_sources WHERE id = :id")
    suspend fun getById(id: String): MediaSourceEntity?

    @Upsert
    suspend fun upsert(source: MediaSourceEntity)

    @Delete
    suspend fun delete(source: MediaSourceEntity)
}
