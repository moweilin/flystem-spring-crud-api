package com.flystem.crud.api.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class DefaultTenantIdProviderTest {

    @Test
    void uuidProviderReturnsZeroUuid() {
        DefaultUUIDTenantIdProvider provider = new DefaultUUIDTenantIdProviderImpl();
        assertEquals(new UUID(0L, 0L), provider.getDefaultTenantId());
    }

    @Test
    void stringProviderReturnsEmptyString() {
        DefaultStringTenantIdProvider provider = new DefaultStringTenantIdProviderImpl();
        assertEquals("", provider.getDefaultTenantId());
    }

    @Test
    void longProviderReturnsZero() {
        DefaultLongTenantIdProvider provider = new DefaultLongTenantIdProviderImpl();
        assertEquals(0L, provider.getDefaultTenantId());
    }
}
