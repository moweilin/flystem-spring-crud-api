package com.flystem.crud.api.id;

import java.util.UUID;

/**
 * 随机 UUID（v4）主键生成器。支持实体 id 类型 {@link UUID} 与 {@link String}。
 *
 * <p>由 {@code FlystemSpringCrudAutoConfiguration} 注册为具体类型 bean；亦可在
 * {@code @CrudGeneration#idGenerator} / {@code @Domain#idGenerator} 上显式配置本类。
 */
public final class UuidIdGenerator implements IdGenerator {

    @Override
    public <T> T nextId(Class<T> idType) {
        return UuidIds.coerce(UUID.randomUUID(), idType, getClass().getSimpleName());
    }
}
