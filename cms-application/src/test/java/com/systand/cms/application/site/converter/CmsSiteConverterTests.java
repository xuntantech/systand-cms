package com.systand.cms.application.site.converter;

import com.systand.cms.api.site.CmsSiteVO;
import com.systand.cms.application.site.model.CmsSiteState;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CmsSiteConverterTests {
    private final CmsSiteConverter converter = Mappers.getMapper(CmsSiteConverter.class);

    @Test
    void mapsSiteFieldsAndNull() {
        CmsSiteState source = new CmsSiteState();
        source.setId(UUID.randomUUID());
        source.setTenantId(UUID.randomUUID());
        source.setCode("home");
        source.setName("Home");
        source.setDefaultLocale("zh-CN");
        source.setEnabledLocales(List.of("zh-CN", "en-US"));
        source.setStatus("ACTIVE");
        source.setCreatedAt(OffsetDateTime.now());
        source.setUpdatedAt(OffsetDateTime.now());
        source.setCreatedBy(UUID.randomUUID());
        source.setUpdatedBy(UUID.randomUUID());

        CmsSiteVO actual = converter.toVO(source);

        assertEquals(source.getId(), actual.id());
        assertEquals(source.getTenantId(), actual.tenantId());
        assertEquals(source.getCode(), actual.code());
        assertEquals(source.getName(), actual.name());
        assertEquals(source.getDefaultLocale(), actual.defaultLocale());
        assertEquals(source.getEnabledLocales(), actual.enabledLocales());
        assertEquals(source.getStatus(), actual.status());
        assertEquals(source.getCreatedAt(), actual.createdAt());
        assertEquals(source.getUpdatedAt(), actual.updatedAt());
        assertEquals(source.getCreatedBy(), actual.createdBy());
        assertEquals(source.getUpdatedBy(), actual.updatedBy());
        assertNull(converter.toVO(null));
    }
}
