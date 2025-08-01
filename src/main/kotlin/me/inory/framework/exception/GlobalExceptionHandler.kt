package me.inory.framework.exception

import cn.dev33.satoken.exception.NotLoginException
import me.inory.util.R
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.stream.Collectors
@RestControllerAdvice
class GlobalExceptionHandler{
    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): R {
        val data = e.getFieldErrors().stream().map<String?> { obj: FieldError? -> obj!!.getDefaultMessage() }
            .collect(Collectors.joining(","))
        return R.error(data)
    }

//    @ExceptionHandler(value = [BusinessException::class])
//    fun handleBusinessException(e: BusinessException): R {
//        return R.error(e.getCode(), e.getMessage())
//    }

    @ExceptionHandler(value = [NotLoginException::class])
    fun notLoginException(e: NotLoginException?): R {
        return R.r(401, "登录失效")
    }

    @ExceptionHandler(value = [Exception::class])
    fun handleException(e: Exception): R {
        e.printStackTrace()
        return R.r(500, e.message)
    }
}