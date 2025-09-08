package me.inory.orm.listener

import cn.dev33.satoken.stp.StpUtil
import me.inory.orm.base.BaseEntity
import me.inory.orm.base.BaseEntityDraft
import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.sql.DraftInterceptor
import org.springframework.stereotype.Component
import java.util.*

@Component
class EntityListener : DraftInterceptor<BaseEntity, BaseEntityDraft> {
    override fun beforeSave(draft: BaseEntityDraft, original: BaseEntity?) {
        super.beforeSave(draft, original)
        draft.updateTime = Date()
        if (StpUtil.isLogin()){
            draft.updateBy = StpUtil.getLoginIdAsLong()
        }
//        if (!isLoaded(draft, BaseEntity::requestId)) {
//            draft.requestId = UrlInterceptor.operLogInfoThreadLocal.get()?.requestId
//        }
        if (original == null) {
            if (!isLoaded(draft, BaseEntity::createTime)) {
                draft.createTime = Date()
            }
            if (!isLoaded(draft, BaseEntity::createBy)) {
                if (StpUtil.isLogin()){
                    draft.createBy = StpUtil.getLoginIdAsLong()
                }
            }
        }
    }
}