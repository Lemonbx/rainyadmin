package me.inory.project.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.text.substringAfterLast
import kotlin.to

@RestController
@RequestMapping("/api/upload")
class UploadController {

    @Value("\${uploadPath}")
    private lateinit var uploadPath: String

    @Value("\${domain}")
    private lateinit var domain: String

    @PostMapping("/file")
    fun uploadFile(@RequestParam("file") file: MultipartFile): Map<String, Any> {
        return uploadFile(file, "files")
    }

    private fun uploadFile(file: MultipartFile, subDir: String): Map<String, Any> {
        return try {
            // 检查文件是否为空
            if (file.isEmpty) {
                return mapOf(
                    "code" to 0,
                    "msg" to "文件不能为空"
                )
            }

            // 创建上传目录
            val uploadDir = Paths.get(uploadPath, subDir)
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir)
            }

            // 生成唯一文件名
            val originalFilename = file.originalFilename ?: "unknown"
            val extension = originalFilename.substringAfterLast('.', "")
            val filename = "${UUID.randomUUID()}.$extension"
            
            // 保存文件
            val filePath = uploadDir.resolve(filename)
            file.transferTo(filePath.toFile())

            // 返回文件URL
            val fileUrl = "$domain${subDir}/$filename"

            mapOf(
                "code" to 1,
                "msg" to "上传成功",
                "data" to fileUrl
            )
        } catch (e: IOException) {
            mapOf(
                "code" to 0,
                "msg" to "文件上传失败: ${e.message}"
            )
        } catch (e: Exception) {
            mapOf(
                "code" to 0,
                "msg" to "上传失败: ${e.message}"
            )
        }
    }
} 