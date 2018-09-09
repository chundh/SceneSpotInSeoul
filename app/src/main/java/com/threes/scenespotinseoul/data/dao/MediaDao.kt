package com.threes.scenespotinseoul.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.threes.scenespotinseoul.data.model.Media

@Dao
interface MediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(media: Media): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(media: List<Media>): List<Long>

    @Query("SELECT * FROM media WHERE ROWID = :rowId")
    fun loadByRowId(rowId: Long): Media

    @Query("SELECT * FROM media WHERE id = :mediaId")
    fun loadById(mediaId: Int): Media

    @Query("SELECT * FROM media WHERE name = :name")
    fun loadByName(name: String): Media

    @Query("SELECT * FROM media")
    fun loadAll(): List<Media>

    @Query("SELECT COUNT(*) FROM media")
    fun getNumberOfRows(): LiveData<Int>
}