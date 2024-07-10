package com.yagizcandinc.yemekkitabi.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.yagizcandinc.yemekkitabi.model.Tarif

@Dao
interface TarifDAO {

    @Query("SELECT * FROM Tarif")
    fun getAll() : List<Tarif>

    @Query("SELECT * FROM Tarif WHERE id = :id")
    fun findById(id: Int) : Tarif

    @Insert()
    fun insert(tarif: Tarif)

    @Delete()
    fun delete(tarif: Tarif)

}