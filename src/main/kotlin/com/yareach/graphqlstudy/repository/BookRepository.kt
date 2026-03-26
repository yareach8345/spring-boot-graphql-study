package com.yareach.graphqlstudy.repository

import com.yareach.graphqlstudy.entity.BookR2dbcEntity
import com.yareach.graphqlstudy.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

interface BookRepository {

    suspend fun save(book: Book): Book

    suspend fun findById(id: Long): Book?

    suspend fun findAll(): Flow<Book>

    suspend fun findByWriterId(writerId: Long): Flow<Book>

    suspend fun findByPage(pageable: Pageable): Flow<Book>

    suspend fun searchByTitle(title: String): Flow<Book>

    suspend fun searchByTitle(title: String, pageable: Pageable): Flow<Book>

    suspend fun delete(id: Long)

    suspend fun existsById(id: Long): Boolean
}

@Repository
class BookRepositoryR2dbcImpl(
    private val bookR2dbcRepository: BookR2dbcRepository
): BookRepository {
    override suspend fun save(book: Book): Book {
        val entity = BookR2dbcEntity.fromModel(book)
        return bookR2dbcRepository.save(entity).toModel()
    }

    override suspend fun findById(id: Long): Book? {
        return bookR2dbcRepository.findById(id)?.toModel()
    }

    override suspend fun findAll(): Flow<Book> {
        return bookR2dbcRepository.findAll().map{ it.toModel() }
    }

    override suspend fun findByWriterId(writerId: Long): Flow<Book> {
        return bookR2dbcRepository.findByWriterId(writerId).map{ it.toModel() }
    }

    override suspend fun findByPage(pageable: Pageable): Flow<Book> {
        return bookR2dbcRepository.findAllBy(pageable).map{ it.toModel() }
    }

    override suspend fun searchByTitle(title: String): Flow<Book> {
        return bookR2dbcRepository.findByTitleContaining(title).map { it.toModel() }
    }

    override suspend fun searchByTitle(
        title: String,
        pageable: Pageable
    ): Flow<Book> {
        return bookR2dbcRepository.findByTitleContaining(title, pageable).map { it.toModel() }
    }

    override suspend fun delete(id: Long) {
        bookR2dbcRepository.deleteById(id)
    }

    override suspend fun existsById(id: Long): Boolean {
        return bookR2dbcRepository.existsById(id)
    }
}