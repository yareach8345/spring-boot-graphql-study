package com.yareach.graphqlstudy.repository

import com.yareach.graphqlstudy.model.Book
import com.yareach.graphqlstudy.model.Writer
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest
import org.springframework.data.domain.PageRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataR2dbcTest
class BookRepositoryR2dbcImplTest {

    @Autowired
    lateinit var bookR2dbcRepository: BookR2dbcRepository

    lateinit var bookRepository: BookRepository

    @Autowired
    lateinit var writerR2dbcRepository: WriterR2dbcRepository

    lateinit var writerRepository: WriterRepository

    @BeforeEach
    fun setUpDatabase() {
        bookRepository = BookRepositoryR2dbcImpl(bookR2dbcRepository)
        writerRepository = WriterRepositoryR2dbcImpl(writerR2dbcRepository)
    }

    @AfterEach
    suspend fun tearDownDatabase() {
        bookR2dbcRepository.deleteAll()
        writerR2dbcRepository.deleteAll()
    }

    @Nested
    inner class SaveTest {

        @Test
        fun shouldSaveBook() = runTest {

            val writer = Writer(name = "Bjarne Stroustrup")
            val writerId = writerRepository.save(writer).id!!

            val newBook = Book(
                title = "The C++ Programming Language",
                writerId = writerId
            )

            val result = bookRepository.save(newBook)

            assertNotNull(result.id)
            assertEquals(newBook.title, result.title)
            assertEquals(writerId, result.writerId)
        }
    }

    @Nested
    inner class FindByIdTest {

        @Nested
        inner class WhenMatchingBookIsExists {

            private lateinit var testWriter: Writer

            private lateinit var testBook: Book

            @BeforeEach
            suspend fun insertTestData() {

                testWriter = Writer(name = "登尾徳誠")
                    .let { writerRepository.save(it) }

                testBook = Book(title = "ゼロからよくわかる！ Arduinoで電子工作入門ガイド", writerId = testWriter.id!!)
                    .let { bookRepository.save(it) }
            }

            @Test
            fun shouldReturnBook() = runTest {

                val result = bookRepository.findById(testBook.id!!)

                assertNotNull(result)
                assertEquals(testBook.title, result.title)
                assertEquals(testBook.writerId, result.writerId)
            }
        }

        @Nested
        inner class WhenMatchingBookNotExists {

            @Test
            fun shouldReturnZero() = runTest {

                val result = bookRepository.findById(99999)

                assertNull(result)
            }
        }
    }

    @Nested
    inner class FindAll {

        @Nested
        inner class WhenBookDataExists {

            val numberOfTestBooks = 10

            private lateinit var testWriter: Writer

            private lateinit var testBooks: List<Book>

            @BeforeEach
            suspend fun insertTestData() {

                testWriter = Writer(name = "test writer")
                    .let { writerRepository.save(it) }

                testBooks = List(numberOfTestBooks) { Book(title = "test book - $it", writerId = testWriter.id!!) }
                    .map { bookRepository.save(it) }
            }

            @Test
            fun shouldReturnListOfBooks() = runTest {

                val result = bookRepository.findAll().toList()

                assertEquals(numberOfTestBooks, result.size)

                result.sortedBy { it.id }.zip(testBooks.sortedBy { it.id }) { result, testBook ->
                    assertEquals(testBook.title, result.title)
                    assertEquals(testBook.writerId, result.writerId)
                }
            }
        }

        @Nested
        inner class WhenBookDataNotExists {

            @Test
            fun shouldReturnEmptyFlow() = runTest {

                val result = bookRepository.findAll()

                assertEquals(0, result.count())
            }
        }
    }

    @Nested
    inner class FindByWriterId {

        @BeforeEach
        suspend fun insertTestData() {
            List(3) { Writer.of("some writer $it") }
                .map { writerRepository.save(it) }
                .forEach { writer -> repeat(10) { index -> bookRepository.save(Book(title = "book of ${writer.name} - $index", writerId = writer.id!!) ) }}
        }

        @Nested
        inner class WhenMatchingBookIsExists {

            private lateinit var testWriter: Writer

            private lateinit var testBooks: List<Book>

            private val numberOfBooksWrittenByTestWriter = 10

            @BeforeEach
            suspend fun insertTestBookDataWrittenByTestWriter() {

                testWriter = Writer(name = "tester")
                    .let { writerRepository.save(it) }

                testBooks = List(numberOfBooksWrittenByTestWriter) { index ->
                    Book(title = "book of tester - $index", writerId = testWriter.id!!)
                }.map { bookRepository.save(it) }
            }

            @Test
            fun shouldReturnBook() = runTest {

                val result = bookRepository.findByWriterId(testWriter.id!!).toList()

                assertEquals(numberOfBooksWrittenByTestWriter, result.size)
                result.sortedBy { it.id }.zip(testBooks.sortedBy { it.id }) { result, testBook ->
                    assertEquals(testBook.title, result.title)
                    assertEquals(testWriter.id, result.writerId)
                }
            }
        }

        @Nested
        inner class WhenMatchingBookNotExists {

            @Test
            fun shouldReturnEmptyFlow() = runTest {
                val result = bookRepository.findByWriterId(99999)

                assertEquals(0, result.count())
            }
        }
    }

    @Nested
    inner class FindByPage {

        val numberOfTestBooks = 10

        @BeforeEach
        suspend fun insertTestData() {
            val testWriterId = writerRepository.save(Writer.of("tester")).id!!

            List(numberOfTestBooks) { Book(title = "test book - $it", writerId = testWriterId) }
                .onEach { bookRepository.save(it) }
        }

        @Test
        fun whenRequestFirstPageShouldReturnBooks() = runTest {
            val pageable = PageRequest.of(0, 4)

            val result = bookRepository.findByPage(pageable).toList()

            assertEquals(pageable.pageSize, result.size)
        }

        @Test
        fun whenRequestLastPageShouldReturnBooksLessThanPageSize() = runTest {
            val pageable = PageRequest.of(2, 4)

            val result = bookRepository.findByPage(pageable).toList()

            assertTrue { result.size <= pageable.pageSize }
        }

        @Test
        fun whenPageIndexOutOfBoundsShouldReturnEmptyFlow() = runTest {

            val pageable = PageRequest.of(10, 4)

            val result = bookRepository.findByPage(pageable)

            assertEquals(0, result.count())
        }
    }

    @Nested
    inner class SearchByTitle {

        private lateinit var testBooks: List<Book>

        @BeforeEach
        suspend fun insertTestData() {
            val testWriterId = Writer(name = "test writer")
                .let { writerRepository.save(it) }
                .id!!

            testBooks = listOf(
                Book(title = "test book", writerId = testWriterId),
                Book(title = "book for test", writerId = testWriterId),
                Book(title = "book for tester", writerId = testWriterId),
                Book(title = "how write good test code", writerId = testWriterId),
                Book(title = "testing", writerId = testWriterId),
                Book(title = "the book", writerId = testWriterId),
                Book(title = "something", writerId = testWriterId),
                Book(title = "something of internet", writerId = testWriterId),
                Book(title = "the SOC(something of code)", writerId = testWriterId),
            ).map { bookRepository.save(it) }
        }

        @Nested
        inner class WithoutPagination {

            @Test
            fun whenRequestWithTitleShouldReturnBooksContainingKeywordInTitle() = runTest {

                val keyword = "test"

                val filteredTestBooks = testBooks.filter { it.title.contains(keyword) }
                assertNotEquals(0, filteredTestBooks.size, "The test premise is flawed. Books contains keyword $keyword in title must exist")

                val result = bookRepository.searchByTitle(keyword).toList()

                assertEquals(filteredTestBooks.size, result.size)

                result.sortedBy { it.id }
                    .zip(filteredTestBooks.sortedBy { it.id })
                    .forEach { (actual, expected) ->
                        assertEquals(expected.id, actual.id)
                        assertEquals(expected.title, actual.title)
                        assertEquals(expected.writerId, actual.writerId)
                    }
            }

            @Test
            fun whenBooksContainingKeywordInTitleIsNotExistsShouldReturnEmptyFlow() = runTest {

                val keyword = "javascript"
                val filteredTestBooks = testBooks.filter { it.title.contains(keyword) }
                assertEquals(0, filteredTestBooks.size, "The test premise is flawed. Books contains keyword $keyword in title must not exist")

                val result = bookRepository.searchByTitle(keyword)

                assertEquals(0, result.count())
            }
        }

        @Nested
        inner class WithPagination {

            @Test
            fun whenRequestFirstPageOfBooksContainingKeywordInItsTitleShouldReturnBooks() = runTest {

                val keyword = "test"

                val pageable = PageRequest.of(0, 4)

                val filteredTestBooks = testBooks.filter { it.title.contains(keyword) }.take(pageable.pageSize)
                assertNotEquals(0, filteredTestBooks.size, "The test premise is flawed. Books contains keyword $keyword in title must exist")

                val result = bookRepository.searchByTitle(keyword, pageable).toList()

                assertEquals(filteredTestBooks.size, result.size)

                result.sortedBy { it.id }
                    .zip(filteredTestBooks.sortedBy { it.id })
                    .forEach { (actual, expected) ->
                        assertEquals(expected.id, actual.id)
                        assertEquals(expected.title, actual.title)
                        assertEquals(expected.writerId, actual.writerId)
                    }
            }

            @Test
            fun whenPageIndexOutOfBoundsShouldReturnEmptyFlow() = runTest {

                val keyword = "test"

                val pageable = PageRequest.of(10, 4)

                val result = bookRepository.searchByTitle(keyword, pageable).toList()

                assertEquals(0, result.size)
            }
        }
    }

    @Nested
    inner class DeleteTest {

        @Nested
        inner class WhenMatchingBookDataIsExists {

            private lateinit var testBook: Book

            @BeforeEach
            suspend fun insertTestData() {
                val testWriterId = Writer(name = "test writer")
                    .let { writerRepository.save(it) }
                    .id!!

                testBook = Book(title = "test book", writerId = testWriterId)
                    .let { bookRepository.save(it) }
            }

            @Test
            fun shouldBookIsDelete() = runTest {
                val numberOfBooksBeforeDelete = bookR2dbcRepository.count()

                bookRepository.delete(testBook.id!!)

                val numberOfBooksAfterDelete = bookR2dbcRepository.count()

                val findResultAfterDelete = bookRepository.findById(testBook.id!!)

                assertEquals(1, numberOfBooksBeforeDelete - numberOfBooksAfterDelete)
                assertNull(findResultAfterDelete)
            }
        }

        @Nested
        inner class WhenMatchingBookDataIsNotExists {

            @Test
            fun shouldHasNoEffect() = runTest {
                val numberOfBooksBeforeDelete = bookRepository.findAll().toList()

                bookRepository.delete(999999)

                val numberOfBooksAfterDelete = bookRepository.findAll().toList()

                assertEquals(numberOfBooksBeforeDelete.size, numberOfBooksAfterDelete.size)

                numberOfBooksBeforeDelete.zip(numberOfBooksAfterDelete).forEach { (before, after) ->
                    assertEquals(before.id, after.id)
                    assertEquals(before.title, after.title)
                    assertEquals(before.writerId, after.writerId)
                }
            }
        }
    }

    @Nested
    inner class ExistsByIdTest {

        @Nested
        inner class WhenMatchingDataExists {

            private lateinit var testBook: Book

            @BeforeEach
            suspend fun insertTestData() {
                val testWriterId = Writer(name = "test writer")
                    .let { writerRepository.save(it) }
                    .id!!

                testBook = Book(title = "test book", writerId = testWriterId)
                    .let { bookRepository.save(it) }

            }

            @Test
            fun shouldReturnTrue() = runTest {
                val result = bookRepository.existsById(testBook.id!!)
                assertTrue(result)
            }
        }

        @Nested
        inner class WhenMatchingDataIsNotExists {

            @Test
            fun shouldReturnFalse() = runTest {
                val result = bookRepository.existsById(999999)
                assertFalse(result)
            }
        }
    }
}