package com.newbie.trackmyspend.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "club_info"
)
data class ClubInfo (
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title:String,
    val subtitle:String ?= null,
    val hexColorCode:String = "aa7700"
)