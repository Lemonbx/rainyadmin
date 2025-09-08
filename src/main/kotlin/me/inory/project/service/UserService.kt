package me.inory.project.service

import cn.hutool.crypto.digest.BCrypt
import me.inory.framework.ext.fetchPage
import me.inory.framework.ext.sqlClient
import me.inory.project.dto.UserCreateInput
import me.inory.project.dto.UserUpdateInput
import me.inory.project.dto.UserListView
import me.inory.project.dto.UserDetailView
import me.inory.project.dto.UserSpecification
import me.inory.project.model.*
import me.inory.util.PasswordUtil
import org.babyfish.jimmer.Page

import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.like
import org.babyfish.jimmer.sql.kt.ast.expression.isNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService {

    fun createUser(input: UserCreateInput): Boolean {
        // 检查用户名是否存在
        val existUser = sqlClient.createQuery(SysUser::class) {
            where(table.loginname eq input.loginname)
            select(table.id)
        }.fetchOneOrNull()

        if (existUser != null) {
            throw RuntimeException("用户名已存在")
        }

        // 为了加密密码，仍需要转换为实体
        val salt = UUID.randomUUID().toString()
        val hashedPassword = BCrypt.hashpw(input.password + salt, BCrypt.gensalt())

        val user = input.toEntity().copy {
            password = hashedPassword
            this.salt = salt
        }

        return sqlClient.save(user, SaveMode.NON_IDEMPOTENT_UPSERT, AssociatedSaveMode.REPLACE).totalAffectedRowCount > 0
    }

    fun updateUser(input: UserUpdateInput): Boolean {
        // 处理密码加密
        return if (!input.password.isNullOrBlank()) {
            // 如果有新密码，则更新密码
            val salt = UUID.randomUUID().toString()
            val hashedPassword = BCrypt.hashpw(input.password + salt, BCrypt.gensalt())

            val user = input.toEntity().copy {
                password = hashedPassword
                this.salt = salt
            }
            sqlClient.save(user, SaveMode.NON_IDEMPOTENT_UPSERT, AssociatedSaveMode.REPLACE).totalAffectedRowCount > 0
        } else {
            sqlClient.save(input, SaveMode.NON_IDEMPOTENT_UPSERT, AssociatedSaveMode.REPLACE).totalAffectedRowCount > 0
        }
    }

    fun deleteUser(id: Long): Boolean {
        return sqlClient.deleteById(SysUser::class, id).totalAffectedRowCount > 0
    }

    fun getUserById(id: Long): UserDetailView? {
        return sqlClient.createQuery(SysUser::class) {
            where(table.id eq id)
            select(table.fetch(UserDetailView::class))
        }.fetchOneOrNull()
    }

    fun getUserList(specification: UserSpecification): Page<UserListView> {
        return sqlClient.createQuery(SysUser::class) {
            where(specification)
            select(table.fetch(UserListView::class))
        }.fetchPage()
    }
}