package me.inory.project.dto

data class UserInfo(
    val id: Long,
    val nickname: String?,
    val loginname: String?,
    val roles: List<String>,
    val permissions: List<String>,
    val menus: List<MenuTreeNode>
)