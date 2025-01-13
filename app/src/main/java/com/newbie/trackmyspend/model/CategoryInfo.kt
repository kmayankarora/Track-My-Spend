package com.newbie.trackmyspend.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "category"
)
data class CategoryInfo (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title:String,
    val monthlyLimit:Double,
    val hexColorCode:String = "aa7700"
)