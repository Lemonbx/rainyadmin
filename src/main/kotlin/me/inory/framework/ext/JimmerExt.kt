package me.inory.framework.ext

import me.inory.util.ServletUtils
import org.babyfish.jimmer.spring.model.SortUtils
import org.babyfish.jimmer.spring.repository.fetchSpringPage
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.query.KConfigurableRootQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest

val sqlClient: KSqlClient by inject()

fun buildPage(): PageRequest {
    ServletUtils.request.let {
        val page = it.getParameter("pageNum")?.toIntOrNull() ?: 1
        val size = it.getParameter("pageSize")?.toIntOrNull() ?: 10
        return PageRequest.of(page - 1, size, SortUtils.toSort("id desc"))
    }
}

fun <T> Page<T>.toPage(): org.babyfish.jimmer.Page<T> {
    return org.babyfish.jimmer.Page(this.content, this.totalElements, this.totalPages.toLong())
}

fun <E> KConfigurableRootQuery<*, E>.fetchPage(): org.babyfish.jimmer.Page<E> =
    this.fetchSpringPage(buildPage()).toPage()