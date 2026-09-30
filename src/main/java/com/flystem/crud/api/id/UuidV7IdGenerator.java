package com.flystem.crud.api.id;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * UUID v7（时间有序）主键生成器：<b>仅</b>委派 classpath 上的
 * {@code com.github.f4b6a3.uuid.UuidCreator#getTimeOrderedEpoch()}——不内嵌 RFC 9562
 * 实现。无该类时本 bean 不应被注册（见 {@code FlystemSpringCrudAutoConfiguration} 的
 * {@code @ConditionalOnClass}）；误用则 {@link #nextId} 抛错。
 *
 * <p>支持实体 id 类型 {@link UUID} 与 {@link String}。
 */
public final class UuidV7IdGenerator implements IdGenerator {

    /** uuid-creator 入口类；存在时才注册本 bean。 */
    public static final String UUID_CREATOR_CLASS = "com.github.f4b6a3.uuid.UuidCreator";

    private static final Method UUID_CREATOR_METHOD = resolveUuidCreator();

    @Override
    public <T> T nextId(Class<T> idType) {
        return UuidIds.coerce(nextUuid(), idType, getClass().getSimpleName());
    }

    private static UUID nextUuid() {
        if (UUID_CREATOR_METHOD == null) {
            throw new IllegalStateException(UUID_CREATOR_CLASS
                    + " is not on the classpath — add com.github.f4b6a3:uuid-creator "
                    + "(or configure UuidIdGenerator / a custom IdGenerator)");
        }
        try {
            return (UUID) UUID_CREATOR_METHOD.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("UuidCreator.getTimeOrderedEpoch() failed", e);
        }
    }

    private static Method resolveUuidCreator() {
        try {
            Class<?> type = Class.forName(UUID_CREATOR_CLASS);
            Method method = type.getMethod("getTimeOrderedEpoch");
            if (UUID.class.isAssignableFrom(method.getReturnType())) {
                return method;
            }
        } catch (ReflectiveOperationException ignored) {
            // 无 uuid-creator：本实现不可用
        }
        return null;
    }
}
