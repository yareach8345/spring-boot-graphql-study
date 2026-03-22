package com.yareach.graphqlstudy.dto

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

data class PageInfoDto(
    val page: Int,
    val size: Int?,
)

fun PageInfoDto.toPageable(): Pageable = PageRequest.of(page, size ?: 10)