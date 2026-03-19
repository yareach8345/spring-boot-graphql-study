package com.yareach.graphqlstudy.repository

import com.yareach.graphqlstudy.entity.WriterR2dbcEntity
import com.yareach.graphqlstudy.model.Writer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Pageable

interface WriterRepository {
    suspend fun findAll(): Flow<Writer>

    suspend fun findById(id: Long): Writer?

    suspend fun findAllBy(pageable: Pageable): Flow<Writer>

    suspend fun findByNameContaining(name: String): Flow<Writer>

    suspend fun findByNameContaining(name: String, pageable: Pageable): Flow<Writer>

    suspend fun save(writer: Writer): Writer

    suspend fun deleteById(id: Long)

    suspend fun existsById(id: Long): Boolean

    suspend fun count(): Long
}

@Repository
class WriterRepositoryR2dbcImpl(
    private val writerR2dbRepository: WriterR2dbcRepository
) :  WriterRepository{
    override suspend fun findAll(): Flow<Writer> {
        return writerR2dbRepository.findAll().map { it.toModel() }
    }

    override suspend fun findById(id: Long): Writer? {
        return writerR2dbRepository.findById(id)?.toModel()
    }

    override suspend fun findAllBy(pageable: Pageable): Flow<Writer> {
        return writerR2dbRepository.findAllBy(pageable).map { it.toModel() }
    }

    override suspend fun findByNameContaining(name: String): Flow<Writer> {
        return writerR2dbRepository.findByNameContaining(name).map { it.toModel() }
    }

    override suspend fun findByNameContaining(name: String, pageable: Pageable): Flow<Writer> {
        return writerR2dbRepository.findByNameContaining(name, pageable).map { it.toModel() }
    }

    override suspend fun save(writer: Writer): Writer {
        val entity = WriterR2dbcEntity.fromModel(writer)

        return writerR2dbRepository.save(entity).toModel()
    }

    override suspend fun deleteById(id: Long) {
        writerR2dbRepository.deleteById(id)
    }

    override suspend fun existsById(id: Long): Boolean {
        return writerR2dbRepository.existsById(id)
    }

    override suspend fun count(): Long {
        return writerR2dbRepository.count()
    }
}