package me.inory.project.controller

import me.inory.project.dto.UserCreateInput
import me.inory.project.dto.UserSpecification
import me.inory.project.dto.UserUpdateInput
import me.inory.project.service.UserService
import me.inory.util.BaseController
import me.inory.util.R
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController(val userService: UserService) : BaseController() {

    @PostMapping
    fun createUser(@RequestBody input: UserCreateInput): R {
        val result = userService.createUser(input)
        return save(result, "创建用户")
    }

    @PutMapping
    fun updateUser(@RequestBody input: UserUpdateInput): R {
        val result = userService.updateUser(input)
        return save(result, "更新用户")
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): R {
        val result = userService.deleteUser(id)
        return save(result, "删除用户")
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): R {
        val user = userService.getUserById(id)
        return user.data("用户不存在")
    }

    @GetMapping
    fun getUserList(request: UserSpecification): R {
        val page = userService.getUserList(request)
        return page.data()
    }
}