package me.inory.project.model

import me.inory.project.model.base.SysRoleBase
import org.babyfish.jimmer.sql.*

@Entity
interface SysRole: SysRoleBase{
    @ManyToMany(mappedBy = "roles")
    val users: List<SysUser>

    @ManyToMany
    @JoinTable(
        name = "sys_role_menu",
        joinColumnName = "role_id",
        inverseJoinColumnName = "menu_id",
        cascadeDeletedBySource = true,
        cascadeDeletedByTarget = true
    )
    val menus: List<SysMenu>
    
    @IdView("users")
    val userIds: List<Long>
    
    @IdView("menus")
    val menuIds: List<Long>
}
