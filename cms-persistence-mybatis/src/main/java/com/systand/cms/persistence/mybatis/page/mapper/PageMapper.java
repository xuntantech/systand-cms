package com.systand.cms.persistence.mybatis.page.mapper;

import com.systand.cms.persistence.mybatis.page.dataobject.PageDO;
import com.systand.cms.persistence.mybatis.CmsBaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PageMapper extends CmsBaseMapper<PageDO> {
}
