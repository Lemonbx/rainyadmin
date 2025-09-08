package me.inory.framework.config

import cn.dev33.satoken.`fun`.SaFunction
import cn.dev33.satoken.interceptor.SaInterceptor
import cn.dev33.satoken.router.SaRouter
import cn.dev33.satoken.stp.StpUtil
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class SaTokenConfig : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
        registry.addInterceptor(SaInterceptor {
            SaRouter.match("/**")    // 拦截的 path 列表，可以写多个 */
                .notMatch("/api/auth/**")        // 排除掉的 path 列表，可以写多个
                .notMatch("/", "/index.html")    // 排除首页
                .notMatch("/favicon.ico")        // 排除网站图标
                .notMatch("/static/**")          // 排除静态资源
                .check(StpUtil::checkLogin)  // 要执行的校验动作
        }).addPathPatterns("/**")
    }
}