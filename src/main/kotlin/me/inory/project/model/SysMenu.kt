package me.inory.project.model

import me.inory.project.model.base.SysMenuBase
import org.babyfish.jimmer.sql.*

@Entity
interface SysMenu : SysMenuBase {

    /**  */
    @IdView
    val parentId: Long?

    @ManyToOne
    val parent: SysMenu?

    @OneToMany(mappedBy = "parent")
    val children: List<SysMenu>

    @ManyToMany(mappedBy = "menus")
    val roles: List<SysRole>
}
