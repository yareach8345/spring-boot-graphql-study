package com.yareach.graphqlstudy.repository

import com.yareach.graphqlstudy.entity.WriterR2dbcEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Pageable


@Repository
interface WriterR2dbcRepository: CoroutineCrudRepository<WriterR2dbcEntity, Long> {
    fun findAllBy(pageable: Pageable): Flow<WriterR2dbcEntity>

    fun findByNameContaining(name: String): Flow<WriterR2dbcEntity>

    fun findByNameContaining(name: String, pageable: Pageable): Flow<WriterR2dbcEntity>
}