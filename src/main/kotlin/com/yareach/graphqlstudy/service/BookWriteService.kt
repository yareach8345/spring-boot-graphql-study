package com.yareach.graphqlstudy.service

import com.yareach.graphqlstudy.dto.AddBookDto
import com.yareach.graphqlstudy.model.Book
import com.yareach.graphqlstudy.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface BookWriteService {
    suspend fun addBook(addBookDto: AddBookDto): Book

    suspend fun deleteBook(id: Long): Boolean
}

@Service
class BookWriteServiceImpl(
    private val bookRepository: BookRepository,
    private val writerReadService: WriterReadService,
) : BookWriteService {

    override suspend fun addBook(addBookDto: AddBookDto): Book {
        val book = Book.of(addBookDto.title, addBookDto.writerId, addBookDto.description)

        if(!writerReadService.checkWritersExists(book.writerId)) {
            throw RuntimeException("Writer ${book.writerId} does not exist")
        }

        return bookRepository.save(book)
    }

    @Transactional
    override suspend fun deleteBook(id: Long): Boolean {
        if(!bookRepository.existsById(id)) {
            throw RuntimeException("Book does not exist")
        }

        bookRepository.delete(id)

        return true
    }
}