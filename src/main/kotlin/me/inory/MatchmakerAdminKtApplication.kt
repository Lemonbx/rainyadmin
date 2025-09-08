package me.inory

import cn.hutool.extra.spring.SpringUtil
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(SpringUtil::class)
class MatchmakerAdminKtApplication

fun main(args: Array<String>) {
    runApplication<MatchmakerAdminKtApplication>(*args)
}
