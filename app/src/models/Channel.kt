package dk.denuafhaengige.android.models

import android.net.Uri
import com.squareup.moshi.Json
import androidx.room.*
import dk.denuafhaengige.android.content.ContentStore
import dk.denuafhaengige.android.content.EntityFlow

@androidx.room.Entity
data class Channel(
    @PrimaryKey
    override val id: Int,
    val identifier: String,
    override val title: String,
    val hidden: Boolean,
    @ColumnInfo(name = "is_broadcasting")
    val isBroadcasting: Boolean,
    @Embedded(prefix = "square_image_file_")
    val squareImageFile: File?,
    @Embedded(prefix = "wide_image_file_")
    val wideImageFile: File?,
    @ColumnInfo(name = "current_broadcast_id")
    @Json(name = "currentBroadcast")
    val currentBroadcastReference: EntityReference<Broadcast>?,
): Entity, Titled, Imaged {

    override val wideImageUri: Uri?
        get() {
            val string = wideImageFile?.url ?: return null
            return Uri.parse(string)
        }

    override val squareImageUri: Uri?
        get() {
            val string = squareImageFile?.url ?: return null
            return Uri.parse(string)
        }

}

@Dao
interface ChannelDao {

    @Transaction
    @Query("SELECT * FROM channel")
    suspend fun getAll(): List<ChannelWithCurrentBroadcast>

    @Transaction
    @Query("SELECT * FROM channel WHERE id IN (:ids)")
    suspend fun loadAllByIds(ids: IntArray): List<ChannelWithCurrentBroadcast>

    @Transaction
    @Query("SELECT * FROM channel WHERE title LIKE :title")
    suspend fun findByTitle(title: String): List<ChannelWithCurrentBroadcast>

    @Transaction
    @Query("SELECT * FROM channel WHERE identifier = :identifier")
    suspend fun findByIdentifier(identifier: String): ChannelWithCurrentBroadcast?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<Channel>)

    @Delete
    suspend fun delete(entity: Channel)

    @Delete
    suspend fun deleteAll(entities: List<Channel>)
}

data class ChannelWithCurrentBroadcast(
    @Embedded val channel: Channel,
    @Relation(
        parentColumn = "current_broadcast_id",
        entityColumn = "id"
    )
    val currentBroadcast: Broadcast?,
): Titled, MetaTitled, Imaged, Entity {

    override val id
        get() = channel.id

    override val wideImageUri: Uri?
        get() {
            val currentBroadcastImageUri = currentBroadcast?.wideImageUri
            if (currentBroadcastImageUri != null) {
                return currentBroadcastImageUri
            }
            return this.channel.wideImageUri
        }

    override val squareImageUri: Uri?
        get() {
            val currentBroadcastImageUri = currentBroadcast?.squareImageUri
            if (currentBroadcastImageUri != null) {
                return currentBroadcastImageUri
            }
            return channel.squareImageUri
        }

    override val title: String
        get() = channel.title

    override val metaTitle: String?
        get() = currentBroadcast?.title

    override val metaTitleSupplement: String?
        get() = null

}

class ChannelFetcher(
    private val store: ContentStore
): EntityFlow.Fetcher<ChannelWithCurrentBroadcast> {
    override suspend fun get(id: Int): ChannelWithCurrentBroadcast? =
        store.database.channelDao()
            .loadAllByIds(ids = intArrayOf(id))
            .firstOrNull()
}
