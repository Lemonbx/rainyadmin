package me.inory.framework.config

import cn.hutool.core.lang.Snowflake
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SnowflakeConfig {
    @Bean
    fun snow() = Snowflake()
}
