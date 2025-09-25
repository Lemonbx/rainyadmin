package me.inory.project.service

import me.inory.framework.ext.sqlClient
import me.inory.project.dto.MenuCreateInput
import me.inory.project.dto.MenuTreeNode
import me.inory.project.dto.MenuUpdateInput
import me.inory.project.dto.MenuListView
import me.inory.project.dto.MenuDetailView
import me.inory.project.dto.MenuSimpleView
import me.inory.project.dto.MenuSpecification
import me.inory.project.model.*
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.like
import org.babyfish.jimmer.sql.kt.ast.expression.count
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.springframework.stereotype.Service

@Service
class MenuService {

    fun createMenu(input: MenuCreateInput): Boolean {
        return sqlClient.save(input, SaveMode.NON_IDEMPOTENT_UPSERT, AssociatedSaveMode.REPLACE).totalAffectedRowCount > 0
    }

    fun updateMenu(input: MenuUpdateInput): Boolean {
        return sqlClient.save(input, SaveMode.NON_IDEMPOTENT_UPSERT, AssociatedSaveMode.REPLACE).totalAffectedRowCount > 0
    }

    fun deleteMenu(id: Long): Boolean {
        // 检查是否有子菜单
        val childCount = sqlClient.createQuery(SysMenu::class) {
            where(table.parentId eq id)
            select(count(table))
        }.fetchOne()

        if (childCount > 0) {
            throw RuntimeException("存在子菜单，无法删除")
        }

        return sqlClient.deleteById(SysMenu::class, id).totalAffectedRowCount > 0
    }

    fun getMenuById(id: Long): MenuDetailView? {
        return sqlClient.createQuery(SysMenu::class) {
            where(table.id eq id)
            select(table.fetch(MenuDetailView::class))
        }.fetchOneOrNull()
    }

    fun getMenuList(specification: MenuSpecification): List<MenuTreeNode> {
        // 1. 先获取所有菜单数据
        val allMenus = sqlClient.createQuery(SysMenu::class) {
            select(
                table.fetchBy {
                    allScalarFields()
                    parentId()
                }
            )
        }.execute()

        // 2. 获取符合搜索条件的菜单
        val matchedMenus = sqlClient.createQuery(SysMenu::class) {
            where(specification)
            select(
                table.fetchBy {
                    allScalarFields()
                    parentId()
                }
            )
        }.execute()

        // 3. 如果匹配的菜单数量等于所有菜单数量，说明是空搜索条件，直接返回完整树状结构
        if (matchedMenus.size == allMenus.size) {
            return buildMenuTree(allMenus)
        }

        // 4. 如果有具体搜索条件但无匹配结果，返回空
        if (matchedMenus.isEmpty()) {
            return emptyList()
        }

        // 5. 有搜索条件且有匹配结果，需要补充父级菜单保持树状结构
        val allMenuIds = mutableSetOf<Long>()
        matchedMenus.forEach { menu ->
            allMenuIds.add(menu.id)
            addParentMenuIds(allMenuIds, menu.parentId, allMenus)
        }

        // 6. 从所有菜单中筛选需要的菜单并构建树
        val completeMenus = allMenus.filter { it.id in allMenuIds }
        return buildMenuTree(completeMenus)
    }
    private fun addParentMenuIds(menuIds: MutableSet<Long>, parentId: Long?, allMenus: List<SysMenu>) {
        if (parentId == null) return

        menuIds.add(parentId)

        // 从已有菜单数据中查找父级菜单
        val parent = allMenus.find { it.id == parentId }
        if (parent != null) {
            addParentMenuIds(menuIds, parent.parentId, allMenus)
        }
    }
    fun getMenuTree(): List<MenuTreeNode> {
        // 对于树形结构，需要获取包含parentId的数据
        val allMenus = sqlClient.createQuery(SysMenu::class) {
            select(
                table.fetchBy {
                    allScalarFields()
                    parentId()
                }
            )
        }.execute()

        return buildMenuTree(allMenus)
    }

    private fun buildMenuTree(menus: List<SysMenu>): List<MenuTreeNode> {
        val rootMenus = mutableListOf<MenuTreeNode>()

        fun buildNode(menu: SysMenu): MenuTreeNode {
            val children = menus
                .filter { it.parentId == menu.id }
                .sortedBy { it.sort ?: 0 }
                .map { buildNode(it) }
            return MenuTreeNode(
                id = menu.id,
                name = menu.name,
                path = menu.path,
                component = menu.component,
                logo = menu.logo,
                type = menu.type,
                sort = menu.sort,
                children = if (children.isEmpty()) null else children
            )
        }

        menus
            .filter { it.parentId == null }
            .sortedBy { it.sort ?: 0 }
            .forEach { rootMenus.add(buildNode(it)) }

        return rootMenus
    }

    fun getAllMenus(): List<MenuSimpleView> {
        return sqlClient.createQuery(SysMenu::class) {
            select(table.fetch(MenuSimpleView::class))
        }.execute()
    }
}
