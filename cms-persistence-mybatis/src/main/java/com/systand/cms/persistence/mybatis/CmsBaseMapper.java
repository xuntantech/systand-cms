package com.systand.cms.persistence.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

/** Minimal CMS mapper convenience methods; no platform-common dependency. */
public interface CmsBaseMapper<T> extends BaseMapper<T> {
    default boolean existsByField(SFunction<T, ?> field, Object value) {
        return exists(new LambdaQueryWrapper<T>().eq(field, value));
    }
}
