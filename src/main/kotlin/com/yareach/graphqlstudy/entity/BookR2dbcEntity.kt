package com.yareach.graphqlstudy.entity

import com.yareach.graphqlstudy.model.Book
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "book")
class BookR2dbcEntity(
    @Id @Column("id")
    val id: Long? = null,

    @Column("title")
    val title: String,

    @Column("description")
    val description: String? = null,

    @Column("writer_id")
    val writerId: Long
) {

    companion object {
        fun fromModel(model: Book) = BookR2dbcEntity(
            id = model.id,
            title = model.title,
            description = model.description,
            writerId = model.writerId
        )
    }

    fun toModel() = Book(
        id = this.id,
        title = this.title,
        description = this.description,
        writerId = this.writerId
    )
}