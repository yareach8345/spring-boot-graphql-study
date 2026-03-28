package com.yareach.graphqlstudy.unit

import com.yareach.graphqlstudy.model.Book
import com.yareach.graphqlstudy.repository.BookRepository
import com.yareach.graphqlstudy.service.BookReadService
import com.yareach.graphqlstudy.service.BookReadServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("UNUSED_VARIABLE")
class BookReadServiceImplTest {

    val repositoryMock: BookRepository = mockk()

    val bookReadService: BookReadService = BookReadServiceImpl(repositoryMock)

    @Nested
    inner class GetBookTest {

        @Nested
        inner class WhenReturnBook {

            val testBookId = 42L
            val testBook = Book(
                id = testBookId,
                title = "The Hitchhiker's Guide to the Galaxy",
                writerId = 10
            )

            @BeforeTest
            fun setUpMocks() {
                coEvery { repositoryMock.findById(testBookId) } returns testBook
            }

            @Test
            fun shouldFindByIdCalled() = runTest {

                bookReadService.getBook(testBookId)

                coVerify(exactly = 1) { repositoryMock.findById(testBookId) }
            }

            @Test
            fun shouldReturnBookFromRepository() = runTest {

                val result = bookReadService.getBook(testBookId)

                assertEquals(testBook.id, result.id)
                assertEquals(testBook.title, result.title)
                assertEquals(testBook.writerId, result.writerId)
            }
        }

        @Nested
        inner class WhenRepositoryReturnNull {

            @BeforeTest
            fun setUpMocks() {
                coEvery { repositoryMock.findById(any()) } returns null
            }

            @Test
            fun shouldFails() = runTest {

                assertFails { bookReadService.getBook(9999) }
            }

            @Test
            fun shouldFindByIdCalled() = runTest {

                assertFails { bookReadService.getBook(9999) }

                coVerify(exactly = 1) { repositoryMock.findById(any()) }
            }
        }
    }

    @Nested
    inner class GetAllBooksTest {

        @Nested
        inner class WithoutPageable {

            @Test
            fun whenBooksExistInRepositoryShouldReturnFlowIncludeBooks() = runTest {

                val testBooks = List(10) { id -> Book(id = id.toLong(), title = "book - $id", writerId = 10)}

                coEvery { repositoryMock.findAll() } returns testBooks.asFlow()

                val result = bookReadService.getAllBooks().toList()

                assertEquals(testBooks.size, result.size)

                testBooks.zip(result).forEach { (expected, actual) ->
                    assertEquals(expected.id, actual.id)
                    assertEquals(expected.title, actual.title)
                    assertEquals(expected.writerId, actual.writerId)
                }
            }

            @Test
            fun whenBookNotExistInRepositoryShouldReturnEmptyFlow() = runTest {

                coEvery { repositoryMock.findAll() } returns emptyFlow()

                val result = bookReadService.getAllBooks().toList()

                assertEquals(0, result.size)
            }
        }

        @Nested
        inner class WithPageable {

            @Nested
            inner class BookIsNotExistsInRepository {

                @BeforeTest
                fun setUpMocks() { coEvery { repositoryMock.findByPage(any<Pageable>()) } returns emptyFlow() }

                @Test
                fun shouldReturnEmptyResult() = runTest {

                    val result = bookReadService.getAllBooks(PageRequest.of(0, 10)).toList()

                    assertEquals(0, result.size)
                }
            }

            @Nested
            inner class BookIsExists {

                val testBooks = List(10) { id -> Book(id = id.toLong(), title = "book - $id", writerId = 10) }

                @BeforeTest
                fun setUpMocks() {
                    coEvery {
                        repositoryMock.findByPage(any<Pageable>())
                    } answers {
                        val pageRequest = firstArg<PageRequest>()
                        val page = pageRequest.pageNumber
                        val size = pageRequest.pageSize

                        testBooks.asFlow().drop(page * size).take(size)
                    }
                }

                @Test
                fun whenEnoughBookExistsReturnBooks() = runTest {

                    val size = 4

                    val result = bookReadService.getAllBooks(PageRequest.of(0, size)).toList()

                    assertEquals(size, result.size)

                    testBooks.slice(0 ..< size).zip(result).forEach { (expected, actual) ->
                        assertEquals(expected.id, actual.id)
                        assertEquals(expected.title, actual.title)
                        assertEquals(expected.writerId, actual.writerId)
                    }
                }

                @Test
                fun whenRequestLastPageShouldReturnLessThanPageSize() = runTest {

                    val result = bookReadService.getAllBooks(PageRequest.of(2, 4)).toList()

                    assertEquals(2, result.size)

                    testBooks.drop(8).zip(result).forEach { (expected, actual) ->
                        assertEquals(expected.id, actual.id)
                        assertEquals(expected.title, actual.title)
                        assertEquals(expected.writerId, actual.writerId)
                    }
                }
            }
        }
    }

    @Nested
    inner class GetBookByWriterIdTest {

        @Nested
        inner class WhenBooksWrittenByWriterIdIsExists {

            @BeforeTest
            fun setUpMocks() {
                coEvery {
                    repositoryMock.findByWriterId(any<Long>())
                } answers {
                    val writerId = arg<Long>(0)

                    List(10) { Book(id = writerId * 10 + it, title = "$writerId's book $it", writerId = writerId ) }.asFlow()
                }
            }

            @Test
            fun shouldReturnResult() = runTest {

                val result = bookReadService.getBookByWriterId(10).toList()

                assertEquals(10, result.size)
            }

            @Test
            fun shouldCallFindByWriterId() = runTest {
                bookReadService.getBookByWriterId(10).collect()

                coVerify(exactly = 1) { val unused = repositoryMock.findByWriterId(10) }
            }
        }

        @Nested
        inner class WhenBooksWrittenByWriterIdIsNotExists {
            @BeforeTest
            fun setUpMocks() {
                coEvery { repositoryMock.findByWriterId(any<Long>()) } returns emptyFlow()
            }

            @Test
            fun shouldReturnResult() = runTest {

                val result = bookReadService.getBookByWriterId(10).toList()

                assertEquals(0, result.size)
            }

            @Test
            fun shouldCallFindByWriterId() = runTest {
                bookReadService.getBookByWriterId(10).collect()

                coVerify(exactly = 1) { val unused = repositoryMock.findByWriterId(10) }
            }
        }
    }

    @Nested
    inner class SearchBookByTitleTest {

        @Nested
        inner class WithPageable {

            @BeforeTest
            fun setUpMocks() {
                coEvery {
                    repositoryMock.searchByTitle(any<String>(), any<Pageable>())
                } answers {
                    val keyword = arg<String>(0)

                    val pageable = arg<Pageable>(1)
                    val pageNumber = pageable.pageNumber
                    val pageSize = pageable.pageSize

                    flow {
                        repeat(pageSize) { index ->
                            val book = Book(
                                id = (pageNumber * pageSize + index).toLong(),
                                title = "book name containing $keyword - $index",
                                writerId = Random.nextLong().absoluteValue,
                            )

                            emit(book)
                        }
                    }
                }
            }

            @Test
            fun shouldReturnResult() = runTest {
                val pageNumber = 1
                val pageSize = 5

                val result = bookReadService.searchBookByTitle("something words", PageRequest.of(pageNumber, pageSize)).toList()

                assertEquals(pageSize, result.size)

                result.forEachIndexed { index, book -> assertEquals((pageNumber * pageSize + index).toLong(), book.id) }
            }

            @Test
            fun shouldCallSearchByTitle() = runTest {
                bookReadService.searchBookByTitle("something words", PageRequest.of(1, 10)).collect()

                coVerify(exactly = 1) { val unused = repositoryMock.searchByTitle(any<String>(), any<Pageable>()) }
            }
        }

        @Nested
        inner class WithoutPageable {

            val numberOfBooks = 10

            @BeforeTest
            fun setUpMocks() {
                coEvery { repositoryMock.searchByTitle(any<String>()) } answers {
                    flow {
                        val keyword = arg<String>(0)

                        repeat(numberOfBooks) { index ->
                            val book = Book(id = index.toLong(), title = "book title containing $keyword - $index", writerId = Random.nextLong().absoluteValue)
                            emit(book)
                        }
                    }
                }
            }

            @Test
            fun shouldReturnResult() = runTest {

                val result = bookReadService.searchBookByTitle("something words").toList()

                assertEquals(numberOfBooks, result.size)
            }

            @Test
            fun shouldCallSearchByTitle() = runTest {

                bookReadService.searchBookByTitle("something words").collect()

                coVerify { val unused = repositoryMock.searchByTitle(any<String>()) }
            }
        }
    }

    @Nested
    inner class CheckExistBook {

        @Nested
        inner class WhenBookExists {

            @BeforeTest
            fun setUpMocks() {
                coEvery {
                    repositoryMock.existsById(any<Long>())
                } returns true
            }

            @Test
            fun shouldCallExistsById() = runTest {

                bookReadService.checkExistsBook(0)
                coVerify(exactly = 1) { repositoryMock.existsById(any<Long>()) }
            }

            @Test
            fun shouldReturnTrue() = runTest {

                val result = bookReadService.checkExistsBook(0)
                assertTrue(result)
            }
        }

        @Nested
        inner class WhenBookNotExists {

            @BeforeTest
            fun setUpMocks() {
                coEvery {
                    repositoryMock.existsById(any<Long>())
                } returns false
            }

            @Test
            fun shouldCallExistsById() = runTest {

                bookReadService.checkExistsBook(0)
                coVerify(exactly = 1) { repositoryMock.existsById(any<Long>()) }
            }

            @Test
            fun shouldReturnTrue() = runTest {

                val result = bookReadService.checkExistsBook(0)
                assertFalse(result)
            }
        }
    }
}