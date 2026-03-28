package com.yareach.graphqlstudy.dto

data class AddBookDto(
    val title: String,
    val description: String,
    val writerId: Long
)