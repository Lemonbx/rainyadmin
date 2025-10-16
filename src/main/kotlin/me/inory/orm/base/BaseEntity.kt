package me.inory.orm.base

import org.babyfish.jimmer.sql.MappedSuperclass
import java.time.LocalDateTime

@MappedSuperclass
interface BaseEntity {
    val createTime: LocalDateTime
    val updateTime: LocalDateTime
    val createBy: Long?
    val updateBy: Long?
}
