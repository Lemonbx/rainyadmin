package me.inory.codegen

import com.zaxxer.hikari.HikariDataSource


import java.io.File
import java.sql.Connection
import java.util.*

/**
 * 一个纯字符串拼接的代码生成。仅适用于mysql
 */
class GenerateCode

val db = "matchmaker"
fun main() {
    val dataSource = HikariDataSource()
    dataSource.jdbcUrl =
        "jdbc:mysql://localhost/$db?useUnicode=true&characterEncoding=utf8&useSSL=true&autoReconnect=true&reWriteBatchedInserts=true"
    dataSource.username = "root"
    dataSource.password = "1234"
    val blackTable = listOf(
        "sys_role_menu",
        "sys_user_role",
        "sys_kf_user_tag",
        "sys_kf_user_group",
        "sys_kf_role_menu",
        "sys_kf_user_role"
    )
    //这些是BaseEntity里的字段，不生成在实体类中
    val publicCol = listOf("create_time", "update_time", "create_by", "update_by")
    //entity存放的包
    val packageName = "me.inory.project.model"
    //源码目录
    val baseDir = "C:\\code\\matchmaker-admin-kt\\src\\main\\kotlin"
    val entityPack = "$baseDir/${packageName.replace(".", "/")}"
    val basePack = "$entityPack/base"
    File(basePack).mkdirs()
    //获取所有的表
    val conn = dataSource.getConnection()
    val metaData = conn.metaData
    // 修改这一行，使用catalog参数指定数据库
    val tables = metaData.getTables(db, null, null, arrayOf("TABLE"))
    val tbs = mutableListOf<Clazz>()
    while (tables.next()) {
        val tableName = tables.getString("TABLE_NAME")
        if (blackTable.contains(tableName)) {
            continue
        }
        //tableName转换为kotlin类名驼峰
        val className = snakeToPascal(tableName)
        val columns = metaData.getColumns(db, null, tableName, null)
        val cols = mutableListOf<Column>()
        val entityCols = mutableListOf<Column>()
        val pks = getPks(tableName, conn)
        while (columns.next()) {
            var columnName = columns.getString("COLUMN_NAME")
            val columnType = columns.getString("TYPE_NAME")
            if (publicCol.contains(columnName)) {
                continue
            }
            //columnName转换为kotlin属性名驼峰，首字母小写
            var cColumnName = snakeToCamel(columnName)
            //把_id结尾的字段放在entity中，非base类中，但是仅第一次才生成
            if (columnName.endsWith("_id")) {
                val entityFile = File("$entityPack/${className}.kt")
                if (entityFile.exists())
                    continue
                entityCols.add(
                    Column(
                        cColumnName,
                        typeParse(columnType),
                        getColComment(columnName, tableName, conn),
                        columns.getBoolean("NULLABLE"),
                        pks.contains(columnName)
                    )
                )
                continue
            }
            cols.add(
                Column(
                    cColumnName,
                    typeParse(columnType),
                    getColComment(columnName, tableName, conn),
                    columns.getBoolean("NULLABLE"),
                    pks.contains(columnName)
                )
            )
        }
        tbs.add(Clazz(className, getTableComment(tableName, conn), cols, entityCols, getAllImport(cols)))
    }
    tbs.forEach {
        val res = buildString {
            appendLine("package $packageName.base")
            appendLine()
            it.allImport.forEach { imp ->
                appendLine(imp)
            }
            appendLine("import me.inory.orm.base.BaseEntity")
            appendLine()
            appendLine("@MappedSuperclass")
            appendLine("interface ${it.name}Base: BaseEntity {")
            it.columns.forEach { col ->
                appendLine("\t/** ${col.remark} */")
                if (col.ispk) {
                    appendLine("\t@Id")
                    appendLine("\t@GeneratedValue(strategy = GenerationType.IDENTITY)")
                }
                appendLine("\tval ${col.name}: ${col.type}${if (col.nullable) "?" else ""}")
                appendLine()
            }
            appendLine("}")
            appendLine()
        }
        //写出到文件
        //判断原始文件是否存在
        val file = File("$basePack/${it.name}Base.kt")
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        file.writeText(res)
        //判断实体类是否存在
        val entityFile = File("$entityPack/${it.name}.kt")
        if (entityFile.exists().not()) {
            //创建文件
            val content = buildString {
                appendLine("package $packageName")
                appendLine()
                appendLine("import $packageName.base.${it.name}Base")
                appendLine("import org.babyfish.jimmer.sql.*")
                appendLine()
                appendLine("@Entity")
                appendLine("interface ${it.name}: ${it.name}Base{")
                if (it.entityCols.isNotEmpty()) {
                    appendLine()
                    it.entityCols.forEach { col ->
                        appendLine("\t/** ${col.remark} */")
                        appendLine("\tval ${col.name}: ${col.type}${if (col.nullable) "?" else ""}")
                    }
                }
                appendLine()
                appendLine("}")
            }
            entityFile.createNewFile()
            entityFile.writeText(content)
        }
    }
    conn.close()
    dataSource.close()
}

data class Column(val name: String, val type: String, val remark: String, val nullable: Boolean, val ispk: Boolean)
data class Clazz(
    val name: String,
    val remark: String,
    val columns: List<Column>,
    val entityCols: List<Column>,
    val allImport: Set<String>
)

fun typeParse(columnType: String): String {
    return when (columnType.lowercase()) {
        "int"->"Int"
        "int2" -> "Int"
        "int4" -> "Int"
        "int8" -> "Long"
        "bigint" -> "Long"
        "varchar" -> "String"
        "timestamp" -> "Date"
        "text" -> "String"
        "numeric" -> "BigDecimal"
        "bpchar" -> "String"
        else -> {
            println("未知类型：$columnType")
            "Any"
        }
    }
}

fun getAllImport(columns: List<Column>): Set<String> {
    val set = mutableSetOf<String>()
    set.add("import org.babyfish.jimmer.sql.*")
    columns.forEach {
        when (it.type) {
            "Date" -> set.add("import java.util.Date")
            "BigDecimal" -> set.add("import java.math.BigDecimal")
        }
    }
    return set
}

fun snakeToCamel(snake: String): String {
    return snake.split("_")
        .joinToString("") { it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
        .replaceFirstChar { it.lowercase(Locale.getDefault()) } // 将首字母小写
}

fun snakeToPascal(snake: String): String {
    return snake.split("_")
        .joinToString("") { it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } } // 每个单词首字母大写
}

fun getPks(tableName: String, conn: Connection): List<String> {
    // 指定catalog参数
    val pks = conn.metaData.getPrimaryKeys(db, null, tableName)
    val pkList = mutableListOf<String>()
    while (pks.next()) {
        pkList.add(pks.getString("COLUMN_NAME"))
    }
    return pkList
}

fun getTableComment(tableName: String, conn: Connection): String {
    val pstm = conn.prepareStatement(
        "SELECT table_comment FROM information_schema.tables WHERE table_schema = ? AND table_name = ?"
    )
    pstm.setString(1, db)
    pstm.setString(2, tableName)
    val rs = pstm.executeQuery()
    try {
        return if (rs.next()) {
            rs.getString("table_comment") ?: ""
        } else {
            ""
        }
    } finally {
        rs.close()
        pstm.close()
    }
}

fun getColComment(col: String, tableName: String, conn: Connection): String {
    val pstm = conn.prepareStatement(
        "SELECT column_comment FROM information_schema.columns WHERE table_schema = ? AND table_name = ? AND column_name = ?"
    )
    pstm.setString(1, db)
    pstm.setString(2, tableName)
    pstm.setString(3, col)
    val rs = pstm.executeQuery()
    try {
        return if (rs.next()) {
            rs.getString("column_comment") ?: ""
        } else {
            ""
        }
    } finally {
        rs.close()
        pstm.close()
    }
}