package com.yareach.graphqlstudy.service

import com.yareach.graphqlstudy.model.Writer
import com.yareach.graphqlstudy.repository.WriterRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

interface WriterReadService {
    suspend fun getById(id: Long): Writer

    suspend fun getWriters(pageRequest: PageRequest? = null): Flow<Writer>

    suspend fun searchWriterByName(name: String, pageRequest: PageRequest? = null): Flow<Writer>

    suspend fun checkWritersExists(id: Long): Boolean
}

@Service
class WriterReadServiceImpl(
    private val writerRepository: WriterRepository
) : WriterReadService {
    override suspend fun getById(id: Long): Writer {
        return writerRepository.findById(id) ?: throw RuntimeException("Writer with ID $id not found")
    }

    override suspend fun getWriters(pageRequest: PageRequest?): Flow<Writer> {
        return when(pageRequest) {
            null -> writerRepository.findAll()
            else -> writerRepository.findAllBy(pageRequest)
        }
    }

    override suspend fun searchWriterByName(
        name: String,
        pageRequest: PageRequest?
    ): Flow<Writer> {
        return when(pageRequest) {
            null -> writerRepository.findByNameContaining(name)
            else -> writerRepository.findByNameContaining(name, pageRequest)
        }
    }

    override suspend fun checkWritersExists(id: Long): Boolean {
        return writerRepository.existsById(id)
    }
}