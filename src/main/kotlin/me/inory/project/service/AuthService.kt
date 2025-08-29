package me.inory.project.service

import cn.dev33.satoken.stp.StpUtil
import cn.hutool.crypto.digest.BCrypt
import me.inory.framework.ext.sqlClient
import me.inory.project.dto.LoginInput
import me.inory.project.dto.ChangePasswordInput
import me.inory.project.dto.MenuTreeNode
import me.inory.project.dto.UserInfo
import me.inory.project.model.*
import me.inory.util.PasswordUtil
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthService {

    fun login(input: LoginInput): String? {
        val user = sqlClient.createQuery(SysUser::class) {
            where(table.loginname eq input.loginname)
            select(
                table.fetchBy {
                    allScalarFields()
                    roles {
                        allScalarFields()
                        menus {
                            allScalarFields()
                            parentId()
                        }
                    }
                }
            )
        }.fetchOneOrNull()

        if (user == null) {
            throw RuntimeException("用户不存在")
        }

        // 验证密码
        if (!BCrypt.checkpw(input.password + user.salt, user.password)) {
            throw RuntimeException("密码错误")
        }

        // 登录成功，生成token
        StpUtil.login(user.id)
        return StpUtil.getTokenValue()
    }

    fun getUserInfo(userId: Long): UserInfo {
        val user = sqlClient.createQuery(SysUser::class) {
            where(table.id eq userId)
            select(
                table.fetchBy {
                    allScalarFields()
                    roles {
                        allScalarFields()
                        menus {
                            allScalarFields()
                            parentId()
                        }
                    }
                }
            )
        }.fetchOneOrNull() ?: throw RuntimeException("用户不存在")

        // 收集所有角色和权限
        val roles = user.roles.map { it.key ?: "" }
        val permissions = user.roles.flatMap { it.menus }.map { it.perms ?: "" }.distinct()

        // 构建菜单树
        val allMenus = user.roles.flatMap { it.menus }.distinctBy { it.id }
        val menus = buildMenuTree(allMenus)

        return UserInfo(
            id = user.id,
            nickname = user.nickname,
            loginname = user.loginname,
            roles = roles,
            permissions = permissions,
            menus = menus
        )
    }

    private fun buildMenuTree(menus: List<SysMenu>): List<MenuTreeNode> {
        val menuMap = menus.associateBy { it.id }
        val rootMenus = mutableListOf<MenuTreeNode>()

        fun buildNode(menu: SysMenu): MenuTreeNode {
            val children = menus.filter { it.parentId == menu.id }
                .map { buildNode(it) }
            return MenuTreeNode(
                id = menu.id,
                name = menu.name,
                path = menu.path,
                component = menu.component,
                logo = menu.logo,
                type = menu.type,
                children = if (children.isEmpty()) null else children
            )
        }

        menus.filter { it.parentId == null }
            .forEach { rootMenus.add(buildNode(it)) }

        return rootMenus
    }

    fun logout() {
        StpUtil.logout()
    }

    fun changePassword(userId: Long, input: ChangePasswordInput): Boolean {
        // 获取当前用户信息
        val user = sqlClient.createQuery(SysUser::class) {
            where(table.id eq userId)
            select(
                table.fetchBy {
                    allScalarFields()
                }
            )
        }.fetchOneOrNull() ?: throw RuntimeException("用户不存在")

        // 验证旧密码
        if (!BCrypt.checkpw(input.oldPassword + user.salt, user.password)) {
            throw RuntimeException("当前密码错误")
        }

        // 生成新的盐值和加密新密码
        val newSalt = UUID.randomUUID().toString()
        val hashedNewPassword = BCrypt.hashpw(input.newPassword + newSalt, BCrypt.gensalt())

        // 更新密码
        val updatedUser = user.copy {
            password = hashedNewPassword
            salt = newSalt
        }

        val result = sqlClient.save(updatedUser)
        return result.totalAffectedRowCount > 0
    }
}