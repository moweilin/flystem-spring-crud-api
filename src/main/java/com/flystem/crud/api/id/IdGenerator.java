package com.flystem.crud.api.id;

/**
 * 主键生成 SPI：在 create 路径上为尚无主键的实体赋值。由
 * {@code @CrudGeneration#idGenerator} / {@code @Domain#idGenerator} 配置实现类（默认
 * {@code Void.class} = <b>不注入</b>，主键仍由持久化框架或数据库自生成，如 MyBatis-Plus
 * 雪花 / {@code ASSIGN_UUID}）。
 *
 * <p>默认实现：{@link UuidIdGenerator}（随机 UUID）与 {@link UuidV7IdGenerator}（委派
 * {@code com.github.f4b6a3.uuid.UuidCreator}，<b>仅当该类在 classpath 上时</b>注册——不内嵌
 * UUID v7 算法）。{@code FlystemSpringCrudAutoConfiguration} 注册二者的具体类型 bean，并另注册
 * 一个 {@link IdGenerator} 接口 bean：有 {@link UuidV7IdGenerator} 时优先用之，否则
 * {@link UuidIdGenerator}。配置为接口类型即注入该优选 bean；配置为具体类即注入对应 bean。
 *
 * <p>与 {@code TenantIdExtractor} / {@code ResultWrapper} 同一替换机制：声明自己的
 * {@link IdGenerator}（或具体实现类）bean 即整体替换。
 *
 * @see UuidIdGenerator
 * @see UuidV7IdGenerator
 */
public interface IdGenerator {

    /**
     * 生成下一主键，类型须与实体 id 字段对齐。
     *
     * @param idType 实体主键类型（如 {@code UUID.class} / {@code String.class}）
     * @param <T>    主键类型
     * @return 非 null 的新主键
     * @throws IllegalArgumentException 本实现不支持该类型
     */
    <T> T nextId(Class<T> idType);
}
