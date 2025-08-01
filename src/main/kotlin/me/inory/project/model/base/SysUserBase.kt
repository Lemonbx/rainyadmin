package me.inory.project.model.base

import org.babyfish.jimmer.sql.*
import me.inory.orm.base.BaseEntity

@MappedSuperclass
interface SysUserBase: BaseEntity {
	/**  */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long

	/**  */
	val nickname: String?

	/**  */
	val loginname: String?

	/**  */
	val password: String?

	/**  */
	val salt: String?

}

