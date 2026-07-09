package com.example.data

import kotlinx.coroutines.flow.Flow

class AuraRepository(private val dao: AuraSnapshotDao) {
    val allSnapshots: Flow<List<AuraSnapshot>> = dao.getAllSnapshots()

    suspend fun insert(snapshot: AuraSnapshot) {
        dao.insertSnapshot(snapshot)
    }

    suspend fun deleteById(id: Int) {
        dao.deleteSnapshotById(id)
    }
}
