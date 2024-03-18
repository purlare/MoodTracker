package com.example.moodtracker

data class UserEntry(
    val mood: String = "",
    val date: String = "",
    val notes: String = "",
    val imageResource: String = ""
) {
    constructor() : this("", "", "", "")
}
