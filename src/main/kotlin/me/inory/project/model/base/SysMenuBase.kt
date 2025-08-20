package me.inory.project.model.base

import org.babyfish.jimmer.sql.*
import me.inory.orm.base.BaseEntity

@MappedSuperclass
interface SysMenuBase: BaseEntity {
	/**  */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long

	/**  */
	val name: String?

	/**  */
	val logo: String?

	/**  */
	val path: String?

	/**  */
	val perms: String?

	/**  */
	val type: Int?

	/**  */
	val component: String?

    /**  */
    val sort: Int?
}

