package com.phoneshim.android.domain.model

data class DailyReport(
    val date: String,
    val timetable: List<TimetableEntry>,
    val aiSuggestion: String,
    val summary: String,
)
