package com.yareach.graphqlstudy.unit

import com.yareach.graphqlstudy.model.Writer
import com.yareach.graphqlstudy.repository.WriterRepository
import com.yareach.graphqlstudy.service.WriterReadServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@Suppress("UNUSED")
class WriterReadServiceImplTest {

    val writerRepositoryMock = mockk<WriterRepository>()

    val writerReadService = WriterReadServiceImpl(writerRepositoryMock)

    fun generateTestWriter(id: Long) = Writer(id, "writer-$id", "test data")

    fun assertWriterEqual(expected: Writer, actual: Writer) {
        assertEquals(expected.id, actual.id)
        assertEquals(expected.name, actual.name)
        assertEquals(expected.description, actual.description)
    }

    fun isPageableEqual(a: Pageable, b: Pageable): Boolean {
        return a.pageSize == b.pageSize && a.pageNumber == b.pageNumber
    }

    @Nested
    inner class GetById {

        @Nested
        inner class WhenWriterIsExists {

            @BeforeEach
            fun setMock() {

                coEvery {
                    writerRepositoryMock.findById(any<Long>())
                } answers {
                    val id = args[0] as Long

                    generateTestWriter(id)
                }
            }

            @Test
            fun findByIdCalled() = runTest {

                val testId = 1L

                writerReadService.getById(testId)

                coVerify(exactly = 1) { writerRepositoryMock.findById(testId) }
            }

            @Test
            fun getByIdReturnWriterInfo() = runTest {

                val testId = 1L

                val result = writerReadService.getById(testId)

                assertWriterEqual(generateTestWriter(testId), result)
            }
        }

        @Nested
        inner class WhenWriterIsNotExists {

            @BeforeEach
            fun setMock() {
                coEvery { writerRepositoryMock.findById(any<Long>()) } returns null
            }

            @Test
            fun throwException() = runTest {

                val testId = 1L

                val exception = assertFails { writerReadService.getById(testId) }

                assertIs<RuntimeException>(exception)
            }
        }
    }

    @Nested
    inner class GetWriters {

        @Nested
        inner class CallWithoutPagingArgument {

            val testWriters = List(5) { Writer(id = it.toLong(), name = "writer-$it") }

            @BeforeEach
            fun setMock() {
                coEvery { writerRepositoryMock.findAll() } returns testWriters.asFlow()
            }

            @Test
            fun findAllCalled() = runTest {

                writerReadService.getWriters().collect()

                coVerify(exactly = 1) { val unused = writerRepositoryMock.findAll() }
            }

            @Test
            fun returnWriters() = runTest {

                val result = writerReadService.getWriters().toList()

                assertEquals(testWriters.size, result.size)

                result.zip(testWriters)
                    .forEach { assertWriterEqual(it.second, it.first) }
            }
        }

        @Nested
        inner class CallWithPagingArgument {

            val lastWriterId = 12

            @BeforeEach
            fun setMock() {
                coEvery {
                    writerRepositoryMock.findAllBy(any<Pageable>())
                } answers {
                    val pageable = arg<Pageable>(0)
                    val pageNumber = pageable.pageNumber
                    val pageSize = pageable.pageSize

                    flow {
                        repeat(pageSize) { index ->
                            val id = pageNumber * pageSize + index

                            if(id > lastWriterId) { return@repeat }

                            emit(generateTestWriter(id.toLong()))
                        }
                    }
                }
            }

            @Test
            fun findAllByCalled() = runTest {
                val pageRequest = PageRequest.of(0, 10)

                writerReadService.getWriters(pageRequest).collect()

                coVerify(exactly = 1) { val unused = writerRepositoryMock.findAllBy(match { isPageableEqual(it, pageRequest) }) }
            }

            @Test
            fun returnWritersOnPage() = runTest {

                val pageRequest = PageRequest.of(1, 5)

                val writers = writerReadService.getWriters(pageRequest).toList()

                (5 until 10).map { generateTestWriter(it.toLong()) }
                    .zip(writers)
                    .forEach { assertWriterEqual(it.second, it.first) }
            }
        }
    }

    @Nested
    inner class SearchWriterByNameTest {

        @Nested
        inner class CallWithoutPagingArgument {

            val sampleWriter = List(5) { Writer(id = it.toLong(), name = "writer-$it") }

            @BeforeEach
            fun setMock() {
                coEvery {
                    writerRepositoryMock.findByNameContaining(any<String>())
                } returns sampleWriter.asFlow()
            }

            @Test
            fun callFindByNameContaining() = runTest {

                writerReadService.searchWriterByName("name").collect()

                coVerify(exactly = 1) { val unused = writerRepositoryMock.findByNameContaining(any<String>()) }
            }

            @Test
            fun returnWriters() = runTest {

                val result = writerReadService.searchWriterByName("name").toList()

                sampleWriter.zip(result)
                    .forEach { assertWriterEqual(it.first, it.second) }
            }
        }

        @Nested
        inner class CallWithPagingArgument {

            @BeforeEach
            fun setMock() {
                coEvery {
                    writerRepositoryMock.findByNameContaining(any<String>(), any<Pageable>())
                } answers {
                    val pageable = arg<Pageable>(1)
                    val pageNumber = pageable.pageNumber
                    val pageSize = pageable.pageSize

                    flow {
                        repeat(pageSize) { index ->
                            val id = pageNumber * pageSize + index
                            emit(generateTestWriter(id.toLong()))
                        }
                    }
                }
            }

            @Test
            fun callFindByNameContaining() = runTest {

                val text = "name"

                val pageRequest = PageRequest.of(1, 5)

                writerReadService.searchWriterByName(text, pageRequest).collect()

                coVerify(exactly = 1) { val unused = writerRepositoryMock.findByNameContaining(text, match { isPageableEqual(it, pageRequest) }) }
            }

            @Test
            fun returnWriters() = runTest {

                val pageRequest = PageRequest.of(1, 5)

                val result = writerReadService.searchWriterByName("name", pageRequest).toList()

                result.forEachIndexed { index, writer ->
                    assertWriterEqual(generateTestWriter(5.toLong() + index), writer)
                }
            }
        }
    }

    @Nested
    inner class checkWritersExists {

        @Nested
        inner class WhenWriterIsExists {

            @BeforeEach
            fun setMock() {
                coEvery {
                    writerRepositoryMock.existsById(any<Long>())
                } returns true
            }

            @Test
            fun existsByIdIsCalled() = runTest {

                writerReadService.checkWritersExists(0L)

                coVerify { writerRepositoryMock.existsById(0L) }
            }

            @Test
            fun returnTrue() = runTest {

                val result = writerReadService.checkWritersExists(0L)

                assertTrue(result)
            }
        }

        @Nested
        inner class WhenWriterIsNotExists {

            @BeforeEach
            fun setMock() {
                coEvery {
                    writerRepositoryMock.existsById(any<Long>())
                } returns false
            }

            @Test
            fun existsByIdIsCalled() = runTest {

                writerReadService.checkWritersExists(0L)

                coVerify { writerRepositoryMock.existsById(0L) }
            }

            @Test
            fun returnTrue() = runTest {

                val result = writerReadService.checkWritersExists(0L)

                assertFalse(result)
            }
        }
    }
}