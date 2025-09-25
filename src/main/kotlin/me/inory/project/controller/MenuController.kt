package me.inory.project.controller

import me.inory.project.dto.MenuCreateInput
import me.inory.project.dto.MenuUpdateInput
import me.inory.project.dto.MenuSpecification
import me.inory.project.service.MenuService
import me.inory.util.BaseController
import me.inory.util.R
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/menu")
class MenuController(val menuService: MenuService) : BaseController() {


    @PostMapping
    fun createMenu(@RequestBody input: MenuCreateInput): R {
        val result = menuService.createMenu(input)
        return save(result, "创建菜单")
    }

    @PutMapping
    fun updateMenu(@RequestBody input: MenuUpdateInput): R {
        val result = menuService.updateMenu(input)
        return save(result, "更新菜单")
    }

    @DeleteMapping("/{id}")
    fun deleteMenu(@PathVariable id: Long): R {
        val result = menuService.deleteMenu(id)
        return save(result, "删除菜单")
    }

    @GetMapping("/{id}")
    fun getMenuById(@PathVariable id: Long): R {
        val menu = menuService.getMenuById(id)
        return menu.data("菜单不存在")
    }

    @GetMapping
    fun getMenuList(specification: MenuSpecification): R {
        val menus = menuService.getMenuList(specification)
        return menus.data()
    }

    @GetMapping("/tree")
    fun getMenuTree(): R {
        val tree = menuService.getMenuTree()
        return tree.data()
    }

    @GetMapping("/all")
    fun getAllMenus(): R {
        val menus = menuService.getAllMenus()
        return menus.data()
    }
}
