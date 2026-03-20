package com.yareach.graphqlstudy.unit

import com.yareach.graphqlstudy.dto.AddWriterDto
import com.yareach.graphqlstudy.model.Writer
import com.yareach.graphqlstudy.repository.WriterRepository
import com.yareach.graphqlstudy.service.WriterWriteServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.assertIs

class WriterWriteServiceImplTest {

    val writerRepositoryMock = mockk<WriterRepository>()

    val writerWriteService = WriterWriteServiceImpl(writerRepositoryMock)

    @BeforeEach
    fun setUpRepository() {
        coEvery { writerRepositoryMock.save(any<Writer>()) } answers {
            (args.first() as Writer).let { Writer(id = 0, name = it.name, description = it.description) }
        }

        coEvery { writerRepositoryMock.deleteById(any<Long>()) } just runs
    }

    @Nested
    inner class AddWriterTest {

        fun isTheWriterBasedByDto(dto: AddWriterDto, writer: Writer) =
            writer.name == dto.name && writer.description == dto.description

        @Test
        fun shouldSaveMethodCalled() = runTest {

            val addWriterDto = AddWriterDto("Conan", "This is a test data")

            val result = writerWriteService.addWriter(addWriterDto)

            coVerify(exactly = 1) { writerRepositoryMock.save(match { isTheWriterBasedByDto(addWriterDto, it) }) }
        }

        @Test
        fun shouldReturnSavedWriter() = runTest {

            val addWriterDto = AddWriterDto("Conan", "This is a test data")

            val result = writerWriteService.addWriter(addWriterDto)

            assertTrue { isTheWriterBasedByDto(addWriterDto, result) }
        }
    }

    @Nested
    inner class DeleteWriterTest {

        val testId = 1L

        @Nested
        inner class WhenExistsByIdReturnTrue {

            @BeforeEach
            fun setUp() {
                coEvery { writerRepositoryMock.existsById(any<Long>()) } returns true
            }

            @Test
            fun shouldDeleteByIdCalled() = runTest {

                val id = 1L

                writerWriteService.deleteWriter(id)

                coVerify(exactly = 1) { writerRepositoryMock.deleteById(id) }
            }
        }

        @Nested
        inner class WhenExistsByIdReturnFalse {

            @BeforeEach
            fun setUp() {
                coEvery { writerRepositoryMock.existsById(any<Long>()) } returns false
            }

            @Test
            fun shouldDeleteByIdNotCalled() = runTest {

                val id = 1L

                assertFails { writerWriteService.deleteWriter(id) }

                coVerify(exactly = 0) { writerRepositoryMock.deleteById(id) }
            }

            @Test
            fun shouldThrowRuntimeException() = runTest {

                val id = 1L

                val exception = assertFails { writerWriteService.deleteWriter(id) }

                assertIs<RuntimeException>(exception)
            }
        }
    }
}