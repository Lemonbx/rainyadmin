package me.inory.project.controller

import cn.dev33.satoken.stp.StpUtil
import jakarta.servlet.http.HttpServletResponse
import me.inory.project.dto.LoginInput
import me.inory.project.dto.ChangePasswordInput
import me.inory.project.service.AuthService
import me.inory.util.BaseController
import me.inory.util.R
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(val authService: AuthService) : BaseController() {

    @PostMapping("/login")
    fun login(@RequestBody input: LoginInput): R {
        val token = authService.login(input)
        return R.ok("登录成功").data(mapOf("token" to token))
    }

    @GetMapping("/userinfo")
    fun getUserInfo(): R {
        val userId = StpUtil.getLoginIdAsLong()
        val userInfo = authService.getUserInfo(userId)
        return userInfo.data()
    }

    @PostMapping("/logout")
    fun logout(): R {
        StpUtil.logout()
        return R.ok("退出成功")
    }

    @PostMapping("/changePassword")
    fun changePassword(@RequestBody input: ChangePasswordInput): R {
        val userId = StpUtil.getLoginIdAsLong()
        val result = authService.changePassword(userId, input)
        return save(result, "修改密码")
    }
}