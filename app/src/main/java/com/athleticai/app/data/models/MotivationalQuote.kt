package com.athleticai.app.data.models

import com.google.gson.annotations.SerializedName

data class MotivationalQuote(
    val id: Int,
    val text: String,
    val context: String,
    @SerializedName("timeOfDay")
    val timeOfDay: String
)

data class QuotesResponse(
    val quotes: List<MotivationalQuote>
)