package com.halo_sf.android.halo_notes.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val title: String,
    val note: String,
    val tagColor: String?,
    val created: String,
    val edited: String?,
    val nextId: Int?,
    val firstRecord: Boolean,
    val delete: Long?
)