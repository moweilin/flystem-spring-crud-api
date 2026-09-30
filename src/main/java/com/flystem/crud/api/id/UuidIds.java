package com.flystem.crud.api.id;

import java.util.UUID;

/**
 * 把 {@link UUID} 收成实体主键类型：支持 {@link UUID} 与 {@link String}（{@code toString()}）。
 */
final class UuidIds {

    private UuidIds() {
    }

    static <T> T coerce(UUID uuid, Class<T> idType, String generatorName) {
        if (uuid == null) {
            throw new IllegalStateException(generatorName + " produced null UUID");
        }
        if (idType == UUID.class) {
            return idType.cast(uuid);
        }
        if (idType == String.class) {
            return idType.cast(uuid.toString());
        }
        throw new IllegalArgumentException(generatorName + " does not support id type "
                + idType.getName() + " (supported: java.util.UUID, java.lang.String)");
    }
}
