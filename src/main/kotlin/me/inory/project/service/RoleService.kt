package me.inory.project.service

import me.inory.framework.ext.fetchPage
import me.inory.framework.ext.sqlClient
import me.inory.project.dto.RoleCreateInput
import me.inory.project.dto.RoleUpdateInput
import me.inory.project.dto.RoleListView
import me.inory.project.dto.RoleDetailView
import me.inory.project.dto.RoleSimpleView
import me.inory.project.dto.RoleSpecification
import me.inory.project.model.*
import org.babyfish.jimmer.Page
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.like
import org.springframework.stereotype.Service

@Service
class RoleService {

    fun createRole(input: RoleCreateInput): Boolean {
        // 检查角色key是否存在
        val existRole = sqlClient.createQuery(SysRole::class) {
            where(table.key eq input.key)
            select(table.id)
        }.fetchOneOrNull()
        
        if (existRole != null) {
            throw RuntimeException("角色key已存在")
        }

        return sqlClient.save(input, SaveMode.NON_IDEMPOTENT_UPSERT, AssociatedSaveMode.REPLACE).totalAffectedRowCount > 0
    }

    fun updateRole(input: RoleUpdateInput): Boolean {
        return sqlClient.save(input, SaveMode.NON_IDEMPOTENT_UPSERT, AssociatedSaveMode.REPLACE).totalAffectedRowCount > 0
    }

    fun deleteRole(id: Long): Boolean {
        return sqlClient.deleteById(SysRole::class, id).totalAffectedRowCount > 0
    }

    fun getRoleById(id: Long): RoleDetailView? {
        return sqlClient.createQuery(SysRole::class) {
            where(table.id eq id)
            select(table.fetch(RoleDetailView::class))
        }.fetchOneOrNull()
    }

    fun getRoleList(specification: RoleSpecification): Page<RoleListView> {
        return sqlClient.createQuery(SysRole::class) {
            where(specification)
            select(table.fetch(RoleListView::class))
        }.fetchPage()
    }

    fun getAllRoles(): List<RoleSimpleView> {
        return sqlClient.createQuery(SysRole::class) {
            select(table.fetch(RoleSimpleView::class))
        }.execute()
    }
}