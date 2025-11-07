package com.eam.tarea_retrofitenandroid.model

import com.google.gson.annotations.SerializedName

data class Pokemon(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("height") val height: Int,
    @SerializedName("weight") val weight: Int,
    @SerializedName("sprites") val sprites: Sprites,
    @SerializedName("types") val types: List<TypeSlot>
)

data class Sprites(
    @SerializedName("front_default") val front_default: String?
)

data class TypeSlot(
    @SerializedName("type") val type: Type
)

data class Type(
    @SerializedName("name") val name: String
)
