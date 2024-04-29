package com.pnfmaster.android

data class ParamsGroup(
    val id: Int,
    val title: String,
    val lowerLimit: Int,
    val upperLimit: Int,
    val motorPosition: Int,
    val trainingTime: Int
)