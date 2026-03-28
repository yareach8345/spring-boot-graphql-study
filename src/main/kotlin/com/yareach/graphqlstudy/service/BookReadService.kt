package com.yareach.graphqlstudy.service

import com.yareach.graphqlstudy.model.Book
import com.yareach.graphqlstudy.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

interface BookReadService {
    suspend fun getBook(id: Long): Book

    suspend fun getAllBooks(pageRequest: Pageable? = null): Flow<Book>

    suspend fun getBookByWriterId(writerId: Long): Flow<Book>

    suspend fun searchBookByTitle(title: String, pageRequest: Pageable? = null): Flow<Book>

    suspend fun checkExistsBook(id: Long): Boolean
}

@Service
class BookReadServiceImpl(
    private val bookRepository: BookRepository,
) : BookReadService {
    override suspend fun getBook(id: Long): Book {
        return bookRepository.findById(id) ?: throw RuntimeException("Book with ID $id not found")
    }

    override suspend fun getAllBooks(pageRequest: Pageable?): Flow<Book> = when(pageRequest) {
        null -> bookRepository.findAll()
        else -> bookRepository.findByPage(pageRequest)
    }

    override suspend fun getBookByWriterId(writerId: Long): Flow<Book> {
        return bookRepository.findByWriterId(writerId)
    }

    override suspend fun searchBookByTitle(
        title: String,
        pageRequest: Pageable?
    ): Flow<Book> = when(pageRequest) {
        null -> bookRepository.searchByTitle(title)
        else -> bookRepository.searchByTitle(title, pageRequest)
    }

    override suspend fun checkExistsBook(id: Long): Boolean {
        return bookRepository.existsById(id)
    }
}