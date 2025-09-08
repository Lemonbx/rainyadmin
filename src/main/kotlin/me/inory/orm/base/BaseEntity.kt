package me.inory.orm.base

import org.babyfish.jimmer.sql.MappedSuperclass
import java.util.Date

@MappedSuperclass
interface BaseEntity {
    val createTime: Date
    val updateTime: Date
    val createBy: Long?
    val updateBy: Long?
}