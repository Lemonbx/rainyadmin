package me.inory.framework.config

import cn.dev33.satoken.interceptor.SaInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class MvcConfig : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        //registry.addInterceptor(SaInterceptor())
    }

    @Value("\${uploadPath}")
    lateinit var uploadPath: String
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/upload/**")
            .addResourceLocations("file:$uploadPath")
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")
    }
}