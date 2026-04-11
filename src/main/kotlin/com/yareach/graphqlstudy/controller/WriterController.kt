package com.yareach.graphqlstudy.controller

import com.yareach.graphqlstudy.dto.AddWriterDto
import com.yareach.graphqlstudy.dto.PageInfoDto
import com.yareach.graphqlstudy.dto.toPageable
import com.yareach.graphqlstudy.model.Book
import com.yareach.graphqlstudy.model.Writer
import com.yareach.graphqlstudy.service.BookReadService
import com.yareach.graphqlstudy.service.WriterReadService
import com.yareach.graphqlstudy.service.WriterWriteService
import kotlinx.coroutines.flow.toList
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WriterController(
    private val writerReadService: WriterReadService,
    private val writerWriteService: WriterWriteService,
    private val bookReadService: BookReadService,
) {

    @QueryMapping
    suspend fun getWriter(@Argument id: Long): Writer {
        return writerReadService.getById(id)
    }

    @QueryMapping
    suspend fun getWriters(@Argument pageRequest: PageInfoDto?): List<Writer> {
        val pageable = pageRequest?.toPageable()
        return writerReadService.getWriters(pageable).toList()
    }

    @QueryMapping
    suspend fun searchWriterByName(@Argument name: String, @Argument pageRequest: PageInfoDto?): List<Writer> {
        val pageable = pageRequest?.toPageable()
        return writerReadService.searchWriterByName(name, pageable).toList()
    }

    @MutationMapping
    suspend fun addWriter(@Argument input: AddWriterDto): Writer {
        return writerWriteService.addWriter(input)
    }

    @MutationMapping
    suspend fun deleteWriter(@Argument id: Long): Boolean {
        writerWriteService.deleteWriter(id)
        return true
    }

    @SchemaMapping
    suspend fun books(writer: Writer): List<Book> {
        val writerId = writer.id
        if(writerId == null) {
            throw RuntimeException("unknown writer id: $writerId")
        }
        return bookReadService.getBookByWriterId(writerId).toList()
    }
}