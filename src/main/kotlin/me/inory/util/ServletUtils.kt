package me.inory.util

import cn.hutool.core.convert.Convert
import cn.hutool.json.JSONObject
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.io.IOException

object ServletUtils {
    /**
     * 获取String参数
     */
    fun getParameter(name: String?): String {
        return request.getParameter(name)
    }

    /**
     * 获取String参数
     */
    fun getParameter(name: String?, defaultValue: String?): String {
        return Convert.toStr(request.getParameter(name), defaultValue)
    }

    /**
     * 获取Integer参数
     */
    fun getParameterToInt(name: String?): Int {
        return Convert.toInt(request.getParameter(name))
    }

    /**
     * 获取Integer参数
     */
    fun getParameterToInt(name: String?, defaultValue: Int?): Int {
        return Convert.toInt(request.getParameter(name), defaultValue)
    }

    val request: HttpServletRequest
        /**
         * 获取request
         */
        get() = requestAttributes!!.request

    val response: HttpServletResponse?
        /**
         * 获取response
         */
        get() = requestAttributes!!.response

    val session: HttpSession
        /**
         * 获取session
         */
        get() = request.session

    val requestAttributes: ServletRequestAttributes?
        get() {
            val attributes = RequestContextHolder.getRequestAttributes()
            return attributes as ServletRequestAttributes?
        }

    /**
     * 将字符串渲染到客户端
     *
     * @param response 渲染对象
     * @param string   待渲染的字符串
     * @return null
     */
    fun renderString(response: HttpServletResponse, string: String?): String? {
        try {
            response.status = 200
            response.contentType = "application/json"
            response.characterEncoding = "utf-8"
            response.writer.print(string)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

//    fun requestParm(request: HttpServletRequest): JSONObject {
//        var parms = ""
//        if (request is RequestWrapper) {
//            parms = request.body
//        }
//        // body参数为空，获取Parameter的数据
//        if (StrUtil.isBlank(parms)) {
//            parms = JSONUtil.toJsonStr(request.parameterMap)
//        }
//        return JSONUtil.parseObj(parms)
//    }

    /**
     * 获取请求参数
     *
     * @param request
     * @return
     */
//    fun requestParmStr(request: HttpServletRequest): String {
//        return requestParm(request).toString()
//    }

    fun getHeaders(request: HttpServletRequest): JSONObject {
        val jsonObject = JSONObject()
        for (headerName in request.headerNames) {
            jsonObject.set(headerName, request.getHeader(headerName))
        }
        return jsonObject
    }

    fun getHeaderStr(request: HttpServletRequest): String {
        return getHeaders(request).toString()
    }
}