package com.systand.cms.persistence.mybatis.site.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.systand.cms.persistence.mybatis.site.dataobject.SiteDO;
import com.systand.cms.persistence.mybatis.CmsBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

@Mapper
public interface SiteMapper extends CmsBaseMapper<SiteDO> {
	@Select("select * from cms.site")
	@InterceptorIgnore(tenantLine = "true")
	List<SiteDO> getSites();

	@InterceptorIgnore(tenantLine = "true")
	SiteDO selectById(UUID siteId);
}
