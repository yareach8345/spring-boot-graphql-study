package com.yareach.graphqlstudy.unit

import com.yareach.graphqlstudy.dto.AddBookDto
import com.yareach.graphqlstudy.model.Book
import com.yareach.graphqlstudy.repository.BookRepository
import com.yareach.graphqlstudy.service.BookWriteService
import com.yareach.graphqlstudy.service.BookWriteServiceImpl
import com.yareach.graphqlstudy.service.WriterReadService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertNotNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class BookWriteServiceImplTest {

    val repositoryMock = mockk<BookRepository>()

    val writerReadServiceMock = mockk<WriterReadService>()

    val bookWriteService: BookWriteService = BookWriteServiceImpl(repositoryMock, writerReadServiceMock)

    @Nested
    inner class AddBookTest {

        @BeforeEach
        fun setUpMock() {
            coEvery {
                repositoryMock.save(any<Book>())
            } answers {
                val input = arg<Book>(0)

                Book(
                    id = 0,
                    title = input.title,
                    description = input.description,
                    writerId = input.writerId
                )
            }
        }

        @Nested
        inner class WhenMatchingWriterExists {

            @BeforeEach
            fun setUpMock() {
                coEvery { writerReadServiceMock.checkWritersExists(any()) } returns true
            }

            @Test
            fun shouldSuccessSave() = runTest {

                val addBookDto = AddBookDto(
                    title = "The Rust Programming Language, 2nd Edition",
                    description = "The official, comprehensive guide to the Rust",
                    writerId = 1,
                )

                val result = bookWriteService.addBook(addBookDto)

                coVerify { writerReadServiceMock.checkWritersExists(addBookDto.writerId) }

                coVerify(exactly = 1) {
                    repositoryMock.save(
                        match<Book> { addBookDto.title == it.title && addBookDto.description == it.description && addBookDto.writerId == it.writerId }
                    )
                }

                assertNotNull(result.id)
                assertEquals(addBookDto.title, result.title)
                assertEquals(addBookDto.description, result.description)
                assertEquals(addBookDto.writerId, result.writerId)
            }
        }

        @Nested
        inner class WhenMatchingWriterNotExists {

            @BeforeEach
            fun setUpMock() {
                coEvery { writerReadServiceMock.checkWritersExists(any()) } returns false
            }

            @Test
            fun shouldThrowException() = runTest {

                val addBookDto = AddBookDto( title = "Voynich manuscript", description = "A mystery book", writerId = 999 )

                assertFails { bookWriteService.addBook(addBookDto) }

                coVerify { writerReadServiceMock.checkWritersExists(addBookDto.writerId) }

                coVerify(exactly = 0) { repositoryMock.save(any<Book>()) }
            }
        }
    }

    @Nested
    inner class DeleteBookTest {

        @BeforeEach
        fun setUpMock() {
            coEvery { repositoryMock.delete(any<Long>()) } just Runs
        }

        @Nested
        inner class WhenMatchingWriterExists {

            @BeforeEach
            fun setUpMock() {
                coEvery { repositoryMock.existsById(any()) } returns true
            }

            @Test
            fun shouldCallDeleteMethodInRepository() = runTest {

                val testBookId = 0L

                bookWriteService.deleteBook(testBookId)

                coVerify { repositoryMock.existsById(testBookId) }

                coVerify(exactly = 1) { repositoryMock.delete(testBookId) }
            }

            @Test
            fun shouldReturnTrue() = runTest {

                val testBookId = 0L

                val result = bookWriteService.deleteBook(testBookId)

                assertTrue(result)
            }
        }

        @Nested
        inner class WhenMatchingWriterNotExists {

            @BeforeEach
            fun setUpMock() {
                coEvery { repositoryMock.existsById(any()) } returns false
            }

            @Test
            fun shouldThrowException() = runTest {

                assertFails { bookWriteService.deleteBook(999) }

                coVerify { repositoryMock.existsById(999) }

                coVerify(exactly = 0) { repositoryMock.save(any<Book>()) }
            }
        }
    }
}