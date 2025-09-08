package me.inory.project.controller

import me.inory.project.dto.RoleCreateInput
import me.inory.project.dto.RoleUpdateInput
import me.inory.project.dto.RoleSpecification
import me.inory.project.service.RoleService
import me.inory.util.BaseController
import me.inory.util.R
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/role")
class RoleController(val roleService: RoleService) : BaseController() {


    @PostMapping
    fun createRole(@RequestBody input: RoleCreateInput): R {
        val result = roleService.createRole(input)
        return save(result, "创建角色")
    }

    @PutMapping
    fun updateRole(@RequestBody input: RoleUpdateInput): R {
        val result = roleService.updateRole(input)
        return save(result, "更新角色")
    }

    @DeleteMapping("/{id}")
    fun deleteRole(@PathVariable id: Long): R {
        val result = roleService.deleteRole(id)
        return save(result, "删除角色")
    }

    @GetMapping("/{id}")
    fun getRoleById(@PathVariable id: Long): R {
        val role = roleService.getRoleById(id)
        return role.data("角色不存在")
    }

    @GetMapping
    fun getRoleList(specification: RoleSpecification): R {
        val page = roleService.getRoleList(specification)
        return page.data()
    }

    @GetMapping("/all")
    fun getAllRoles(): R {
        val roles = roleService.getAllRoles()
        return roles.data()
    }
}