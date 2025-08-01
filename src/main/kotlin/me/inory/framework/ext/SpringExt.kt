package me.inory.framework.ext


import cn.hutool.extra.spring.SpringUtil.getBean
//import com.mybatisflex.core.service.IService
import kotlin.reflect.KClass


inline fun <reified T1 : Any, R> withBean(
    beanType: KClass<T1>,
    crossinline block: T1.() -> R
) = getBean(beanType.java).block()


inline fun <reified T> inject() = lazy {
    getBean(T::class.java)
}