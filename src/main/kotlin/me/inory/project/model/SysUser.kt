package me.inory.project.model

import me.inory.project.model.base.SysUserBase
import org.babyfish.jimmer.sql.*

@Entity
interface SysUser : SysUserBase {
    @ManyToMany
    @JoinTable(
        name = "sys_user_role",
        joinColumnName = "user_id",
        inverseJoinColumnName = "role_id",
        cascadeDeletedBySource = true,
        cascadeDeletedByTarget = true
    )
    val roles: List<SysRole>
    
    @IdView("roles")
    val roleIds: List<Long>
}
