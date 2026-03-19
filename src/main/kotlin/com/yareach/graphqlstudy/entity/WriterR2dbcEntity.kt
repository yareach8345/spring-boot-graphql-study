package com.yareach.graphqlstudy.entity

import com.yareach.graphqlstudy.model.Writer
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "writer")
class WriterR2dbcEntity(
    @Id @Column("id")
    val id: Long? = null,

    @Column("name")
    val name: String,

    @Column("description")
    val description: String?,
) {
    companion object {

        fun fromModel(model: Writer) = WriterR2dbcEntity(
            id = model.id,
            name = model.name,
            description = model.description,
        )
    }

    fun toModel() = Writer(
        id = id,
        name = name,
        description = description,
    )
}