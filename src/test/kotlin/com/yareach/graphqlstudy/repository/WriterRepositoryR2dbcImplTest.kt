package com.yareach.graphqlstudy.repository

import com.yareach.graphqlstudy.model.Writer
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest
import org.springframework.data.domain.PageRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataR2dbcTest
class WriterRepositoryR2dbcImplTest {

    @Autowired
    private lateinit var writerR2dbcRepository: WriterR2dbcRepository

    private lateinit var writerRepository: WriterRepository

    @BeforeEach
    fun setUpRepository() {

        writerRepository = WriterRepositoryR2dbcImpl(writerR2dbcRepository)
    }

    @AfterEach
    suspend fun tearDownRepository() {

        writerR2dbcRepository.deleteAll()
    }

    @Nested
    inner class FindByIdTest {

        @Test
        fun shouldReturnWriter() = runTest {

            val newEntity = Writer.of("Osamu")

            val id = writerRepository.save(newEntity).id

            assertNotNull(id)

            val result = writerRepository.findById(id)

            assertNotNull(result)
            assertEquals(id, result.id)
            assertEquals(newEntity.name, result.name)
            assertEquals(newEntity.description, result.description)
        }

        @Test
        fun whenMatchingDataNotExistsShouldReturnNull() = runTest {
            val result = writerRepository.findById(9999)

            assertNull(result)
        }
    }

    @Nested
    inner class FindAllTest {

        @Test
        fun shouldReturnAllWriter() = runTest {

            val writerNames = listOf(
                "Shakespeare",
                "Rushdie",
                "Hangang"
            )

            writerNames
                .map { Writer.of(it) }
                .forEach { writerRepository.save(it) }

            val findResult = writerRepository.findAll().toList()

            assertEquals(writerNames.size, findResult.size)

            writerNames.zip(findResult)
                .forEach { assertEquals(it.first, it.second.name) }
        }

        @Test
        fun whenDataNotExistsShouldReturnEmptyFlow() = runTest {

            val findResult = writerRepository.findAll().toList()

            assertEquals(0, findResult.size)
        }
    }

    @Nested
    inner class FindByTest {

        @BeforeEach
        suspend fun setUp() {
            repeat(10) { index ->
                writerRepository.save(Writer.of("writer-$index"))
            }
        }

        @Test
        fun whenRequestWithPageableShouldReturnFourElements() = runTest {

            val result = writerRepository.findAllBy(PageRequest.of(1, 4)).toList()

            assertEquals(4, result.size)

            result
                .forEachIndexed { idx, writer -> assertEquals("writer-${idx + 4}", writer.name) }
        }

        @Test
        fun whenLastPageHasLessThanPageSizeShouldReturnRemainingElements() = runTest {

            val result = writerRepository.findAllBy(PageRequest.of(2, 4)).toList()

            assertEquals(2, result.size)

            result
                .forEachIndexed { idx, writer -> assertEquals("writer-${idx + 8}", writer.name) }
        }

        @Test
        fun whenPageExceedsTotalPagesShouldReturnEmpty() = runTest {

            val result = writerRepository.findAllBy(PageRequest.of(3, 4)).toList()

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class FindByNameContainingTest {

        @Test
        fun shouldReturnContainingName() = runTest {

            val namesContainingNi = listOf(
                "Friedrich Nietzsche",
                "Nisio Isin",
            )

            (listOf("Hannah Arendt", "J. K. Rowling") + namesContainingNi)
                .map { Writer.of(it) }
                .forEach { writerRepository.save(it) }

            val findResult = writerRepository.findByNameContaining("Ni").toList()

            assertEquals(2, findResult.size)

            findResult.map { it.name }
                .sorted()
                .zip(namesContainingNi)
                .forEach { assertEquals(it.first, it.second) }
        }
    }

    @Nested
    inner class FindByNameContainingWithPageableTest {

        @BeforeEach
        suspend fun setUp() {
            repeat(19) { index -> writerRepository.save(Writer.of("writer-${index + 1}")) }
        }

        @Test
        fun whenRequestWithPageable() = runTest {
            val result = writerRepository.findByNameContaining("1", PageRequest.of(0, 10)).toList()

            assertEquals(10, result.size)

            (listOf("writer-1") + List(9) { "writer-1$it" }).zip(result.map { it.name })
                .forEach { assertEquals(it.first, it.second) }
        }

        @Test
        fun whenRequestLastPage() = runTest {
            val result = writerRepository.findByNameContaining("1", PageRequest.of(1, 10)).toList()

            assertEquals(1, result.size)
            assertEquals("writer-19", result.first().name)
        }
    }

    @Nested
    inner class SaveTest {

        @Test
        fun shouldBeSaved() = runTest {

            val newEntity = Writer.of("Kafka")

            val saveResult = writerRepository.save(newEntity)

            assertNotNull(saveResult.id)

            val findResult = writerR2dbcRepository.findById(saveResult.id)

            assertNotNull(findResult)
            assertEquals(saveResult.id, findResult.id)
            assertEquals(saveResult.name, findResult.name)
            assertEquals(saveResult.description, findResult.description)
        }
    }

    @Nested
    inner class DeleteByIdTest {

        @Test
        fun shouldBeDeleted() = runTest {

            val id = writerRepository.save(Writer.of("Carl Sagan")).id!!

            writerRepository.deleteById(id)

            val findResultAfterDelete = writerRepository.findById(id)

            assertNull(findResultAfterDelete)
        }
    }

    @Nested
    inner class ExistsByIdTest {

        @Test
        fun whenMatchingDataExistsShouldReturnTrue() = runTest {

            val id = writerRepository.save(Writer.of("Douglas Adams")).id!!

            val result = writerRepository.existsById(id)

            assertTrue(result)
        }

        @Test
        fun whenMatchingDataDoNotExistsShouldReturnFalse() = runTest {

            val result = writerRepository.existsById(999999)

            assertFalse(result)
        }
    }

    @Nested
    inner class CountTest {

        @Test
        fun shouldReturnCount() = runTest {

            val numberOfData = 8

            repeat(numberOfData) { writerRepository.save(Writer.of("Writer ${it + 1}")) }

            val result = writerRepository.count()

            assertEquals(numberOfData.toLong(), result)
        }
    }
}