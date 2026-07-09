package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuraSnapshotDao {
    @Query("SELECT * FROM aura_snapshots ORDER BY timestamp DESC")
    fun getAllSnapshots(): Flow<List<AuraSnapshot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: AuraSnapshot)

    @Query("DELETE FROM aura_snapshots WHERE id = :id")
    suspend fun deleteSnapshotById(id: Int)
}
