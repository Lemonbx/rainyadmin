package me.inory.project.model.base

import org.babyfish.jimmer.sql.*
import me.inory.orm.base.BaseEntity

@MappedSuperclass
interface SysRoleBase: BaseEntity {
	/**  */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long

	/**  */
	val name: String?

	/**  */
	@Column(name = "`key`")
	val key: String?

}

