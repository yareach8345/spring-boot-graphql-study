package com.yareach.graphqlstudy.model

class Book(
    val id: Long? = null,

    val title: String,

    val description: String? = null,

    val writerId: Long
) {
    companion object {
        fun of(title: String, writerId: Long, description: String? = null): Book {
            return Book(title = title, description = description, writerId = writerId)
        }
    }
}