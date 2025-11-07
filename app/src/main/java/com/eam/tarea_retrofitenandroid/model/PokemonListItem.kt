package com.eam.tarea_retrofitenandroid.model

import com.google.gson.annotations.SerializedName

data class PokemonListItem(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
) {
    fun extractId(): Int {
        return url.trimEnd('/').split("/").last().toInt()
    }
}