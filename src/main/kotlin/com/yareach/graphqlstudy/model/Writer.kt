package com.yareach.graphqlstudy.model

class Writer(
    val id: Long? = null,

    val name: String,

    val description: String?,
) {
    companion object {
        fun of(name: String, description: String? = null): Writer = Writer(
            name = name,
            description = description,
        )
    }
}