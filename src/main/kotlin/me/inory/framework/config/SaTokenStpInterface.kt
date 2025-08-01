package me.inory.framework.config

import cn.dev33.satoken.stp.StpInterface
import cn.dev33.satoken.stp.StpUtil
import me.inory.framework.ext.sqlClient
import me.inory.project.model.SysUser
import me.inory.project.model.fetchBy
import me.inory.project.model.id
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.stereotype.Component

@Component
class SaTokenStpInterface : StpInterface {

    /**
     * 返回一个账号所拥有的权限码集合 
     */
    override fun getPermissionList(loginId: Any?, loginType: String?): List<String> {
        val userId = loginId.toString().toLongOrNull() ?: return emptyList()
        
        return try {
            val user = sqlClient.createQuery(SysUser::class) {
                where(table.id eq userId)
                select(
                    table.fetchBy {
                        roles {
                            menus {
                                allScalarFields()
                                parentId()
                            }
                        }
                    }
                )
            }.fetchOneOrNull()
            
            user?.roles?.flatMap { it.menus }?.mapNotNull { it.perms }?.distinct() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    override fun getRoleList(loginId: Any?, loginType: String?): List<String> {
        val userId = loginId.toString().toLongOrNull() ?: return emptyList()
        
        return try {
            val user = sqlClient.createQuery(SysUser::class) {
                where(table.id eq userId)
                select(
                    table.fetchBy {
                        roles {
                            allScalarFields()
                        }
                    }
                )
            }.fetchOneOrNull()
            
            user?.roles?.mapNotNull { it.key }?.distinct() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}