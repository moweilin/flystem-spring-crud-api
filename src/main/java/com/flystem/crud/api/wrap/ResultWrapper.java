package com.flystem.crud.api.wrap;

import java.util.function.Supplier;

/**
 * 结果包装 SPI（ADR 0020）：把「产出结果的计算」交进来，由实现决定怎么包。唯一启用包装的路径——
 * {@code @ControllerGeneration.resultWrapper} 配实现本接口的类（或 {@code Void.class} = 不包装）。
 *
 * <p>参数是 {@link Supplier} 而<b>不是已算好的值</b>——于是实现能在调用前后做事：try/catch 把异常
 * 翻成失败码、统计耗时、写审计日志、开事务……而生成产物一行都不用改。
 *
 * <p>默认实现 {@link DefaultResultWrapper} 由 {@code FlystemSpringCrudAutoConfiguration} 在使用方未声明
 * 同类型 bean 时注册；声明自己的 {@link ResultWrapper} bean 即<b>整体替换</b>包装逻辑（同
 * {@code TenantIdExtractor} 的替换机制，ADR 0017）。
 *
 * <p>返回类型固定为 {@link R}：SPI 无法既「开放信封类型」又保持类型安全（Java 没有高阶类型，
 * {@code <T> W<T> wrap(…)} 表达不出来），故信封锁定为本库的 {@code R}。要自己的信封形状，声明自己的
 * {@code ResultWrapper} bean，在 {@link #wrap} 里组装即可。
 */
public interface ResultWrapper {

    /**
     * 执行 {@code supplier} 并把结果包进 {@link R}。
     *
     * @param supplier 产出真正返回值的计算（生成的端点传 {@code () -> service.xxx(...)}）；
     *                 实现<b>应当</b>调用它一次——不调即业务不执行
     * @param <T>      数据类型
     * @return 包装后的结果
     */
    <T> R<T> wrap(Supplier<T> supplier);
}
