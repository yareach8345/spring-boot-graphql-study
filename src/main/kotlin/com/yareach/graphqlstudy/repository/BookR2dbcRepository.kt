package com.yareach.graphqlstudy.repository

import com.yareach.graphqlstudy.entity.BookR2dbcEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BookR2dbcRepository: CoroutineCrudRepository<BookR2dbcEntity, Long> {

    fun findAllBy(pageable: Pageable): Flow<BookR2dbcEntity>

    fun findByWriterId(writerId: Long): Flow<BookR2dbcEntity>

    fun findByTitleContaining(title: String): Flow<BookR2dbcEntity>

    fun findByTitleContaining(title: String, pageable: Pageable): Flow<BookR2dbcEntity>
}