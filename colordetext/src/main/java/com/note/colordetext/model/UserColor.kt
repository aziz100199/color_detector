package com.note.colordetext.model

import androidx.room.Entity
import androidx.room.PrimaryKey


class UserColor(
    var hex: String = "#000000",
    var r: String = "",
    var g: String = "",
    var b: String = "",
    var h: String = "",
    var s: String = "",
    var l: String = ""
)