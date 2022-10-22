package com.halo_sf.android.halo_notes.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NoteDao {
    @Insert
    fun insertItem(note: NoteEntity): Long

    @Query("UPDATE notes SET title = :title, note = :note, tagColor = :tagColor, edited = :edited WHERE id = :id")
    fun updateItem(id: Int, title: String, note: String, tagColor: String?, edited: String)

    @Query("UPDATE notes SET nextId = :nextId WHERE id = :id")
    fun updateNextId(id: Int, nextId: Int?)

    @Query("UPDATE notes SET firstRecord = :firstRecord WHERE id = :id")
    fun updateFirstRecord(id: Int, firstRecord: Boolean)

    @Query("UPDATE notes SET `delete` = :deletingTime WHERE id = :id")
    fun updateDelete(id: Int, deletingTime: Long?)

    @Query("DELETE FROM notes WHERE id = :id")
    fun deleteItem(id: Int)

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getItem(id: Int): NoteEntity

    @Query("SELECT id FROM notes WHERE nextId = :nextId")
    fun getPreviousId(nextId: Int): Int?

    @Query("SELECT nextId FROM notes WHERE id = :id")
    fun getNextId(id: Int): Int?

    @Query("SELECT firstRecord FROM notes WHERE id = :id")
    fun getFirstRecord(id: Int): Boolean

    @Query("SELECT `delete` FROM notes WHERE id = :id")
    fun getDelete(id: Int): Long?

    @Query("SELECT id FROM notes WHERE `delete` IS NOT NULL")
    fun getDeleteIds(): List<Int>

    @Query("SELECT id FROM notes WHERE firstRecord = 1")
    fun getFirstRecordId(): Int?

    @Query("SELECT id FROM notes WHERE nextId is null")
    fun getLastRecordId(): Int?
}