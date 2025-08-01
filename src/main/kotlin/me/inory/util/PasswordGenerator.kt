package me.inory.util

import cn.hutool.crypto.digest.BCrypt

fun main() {
    val password = "admin123"
    val salt = "abcdef"
    val passwordWithSalt = password + salt
    val hashedPassword = BCrypt.hashpw(passwordWithSalt, BCrypt.gensalt())
    
    println("原始密码: $password")
    println("盐值: $salt") 
    println("密码+盐值: $passwordWithSalt")
    println("BCrypt哈希值: $hashedPassword")
    
    // 验证
    val isValid = BCrypt.checkpw(passwordWithSalt, hashedPassword)
    println("验证结果: $isValid")
}