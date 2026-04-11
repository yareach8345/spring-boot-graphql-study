package com.yareach.graphqlstudy.controller

import com.yareach.graphqlstudy.dto.AddBookDto
import com.yareach.graphqlstudy.dto.PageInfoDto
import com.yareach.graphqlstudy.dto.toPageable
import com.yareach.graphqlstudy.model.Book
import com.yareach.graphqlstudy.model.Writer
import com.yareach.graphqlstudy.service.BookReadService
import com.yareach.graphqlstudy.service.BookWriteService
import com.yareach.graphqlstudy.service.WriterReadService
import kotlinx.coroutines.flow.toList
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class BookController(
    private val bookReadService: BookReadService,
    private val bookWriteService: BookWriteService,
    private val writerReadService: WriterReadService,
) {

    @QueryMapping
    suspend fun getBook(@Argument id: Long): Book {
        return bookReadService.getBook(id)
    }

    @QueryMapping
    suspend fun getAllBooks(@Argument pageRequest: PageInfoDto?): List<Book> {
        val pageable = pageRequest?.toPageable()
        return bookReadService.getAllBooks(pageable).toList()
    }

    @QueryMapping
    suspend fun getBookByWriterId(@Argument writerId: Long): List<Book> {
        return bookReadService.getBookByWriterId(writerId).toList()
    }

    @QueryMapping
    suspend fun searchBookByTitle(@Argument title: String, @Argument pageRequest: PageInfoDto?): List<Book> {
        val pageable = pageRequest?.toPageable()
        return bookReadService.searchBookByTitle(title, pageable).toList()
    }

    @MutationMapping
    suspend fun addBook(@Argument input: AddBookDto): Book {
        return bookWriteService.addBook(input)
    }

    @MutationMapping
    suspend fun deleteBook(@Argument id: Long): Boolean {
        bookWriteService.deleteBook(id)
        return true
    }

    @SchemaMapping
    suspend fun writer(book: Book): Writer {
        val writerId = book.writerId
        return writerReadService.getById(writerId)
    }
}