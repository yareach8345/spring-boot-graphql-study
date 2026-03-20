package com.yareach.graphqlstudy.service

import com.yareach.graphqlstudy.dto.AddWriterDto
import com.yareach.graphqlstudy.model.Writer
import com.yareach.graphqlstudy.repository.WriterRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface WriterWriteService {
    suspend fun addWriter(addWriterDto: AddWriterDto): Writer

    suspend fun deleteWriter(id: Long)
}

@Service
@Transactional
class WriterWriteServiceImpl(
    private val writerRepository: WriterRepository
) : WriterWriteService {
    override suspend fun addWriter(addWriterDto: AddWriterDto): Writer {
        val newWriter = Writer.of(addWriterDto.name, addWriterDto.description)
        return writerRepository.save(newWriter)
    }

    override suspend fun deleteWriter(id: Long) {
        if(!writerRepository.existsById(id)) {
            throw RuntimeException("Writer with ID $id not found")
        }
        writerRepository.deleteById(id)
    }
}