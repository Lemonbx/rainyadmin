package me.inory.orm.listener

import cn.dev33.satoken.stp.StpUtil
import me.inory.orm.base.BaseEntity
import me.inory.orm.base.BaseEntityDraft
import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.sql.DraftInterceptor
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class EntityListener : DraftInterceptor<BaseEntity, BaseEntityDraft> {
    override fun beforeSave(draft: BaseEntityDraft, original: BaseEntity?) {
        super.beforeSave(draft, original)
        draft.updateTime = LocalDateTime.now()
        try{
            if (StpUtil.isLogin()){
                draft.updateBy = StpUtil.getLoginIdAsLong()
            }
        }catch (_: Exception){}

        if (original == null) {
            if (!isLoaded(draft, BaseEntity::createTime)) {
                draft.createTime = LocalDateTime.now()
            }
            if (!isLoaded(draft, BaseEntity::createBy)) {
                try{
                    if (StpUtil.isLogin()){
                        draft.createBy = StpUtil.getLoginIdAsLong()
                    }
                }catch (_: Exception){}
            }
        }
    }
}
