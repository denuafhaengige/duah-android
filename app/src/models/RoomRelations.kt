package dk.denuafhaengige.android.models

import androidx.room.*
import androidx.room.Entity

@Entity(primaryKeys = ["broadcast_id", "employee_id"])
data class BroadcastEmployeeCrossRef(
    @ColumnInfo(name = "broadcast_id", index = true)
    val broadcastReference: EntityReference<Broadcast>,
    @ColumnInfo(name = "employee_id", index = true)
    val employeeReference: EntityReference<Employee>,
)

data class BroadcastId(
    @ColumnInfo(name = "broadcast_id")
    val broadcastId: Int
)

@Dao
interface RelationsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBroadcastEmployeeCrossRefs(crossRefs: List<BroadcastEmployeeCrossRef>)
    @Delete(entity = BroadcastEmployeeCrossRef::class)
    suspend fun deleteBroadcastEmployeeCrossRefs(broadcastIds: List<BroadcastId>)
}
