package me.inory.util

import cn.hutool.crypto.digest.MD5
import java.util.Objects

object PasswordUtil {
    fun encode(password: String?, salt: String?): String = MD5.create().digestHex(password + salt)
    fun check(input: String?, salt: String?, encodePassword: String?) =
        Objects.equals(encode(input, salt), encodePassword)

    fun isValid(password: String): Boolean {
        val regex = "^(?:(?=.*[a-z])(?=.*[A-Z])|(?=.*[a-z])(?=.*\\d)|(?=.*[a-z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])|(?=.*[A-Z])(?=.*\\d)|(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])|(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])).{8,}$".toRegex()
        return regex.matches(password)
    }
    fun validWithException(password:String?){
        if (password.isNullOrBlank()){
            throw Exception("密码不能为空")
        }
        if(!isValid(password)){
            throw Exception("密码长度需要大于8位，只能包含大小写英文，数字，特殊符号，并包含任意两种字符")
        }
    }
}