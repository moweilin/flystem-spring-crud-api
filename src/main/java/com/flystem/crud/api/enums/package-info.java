/**
 * 契约里的<b>枚举</b>——与 {@code annotation} 子包分立：{@link com.flystem.crud.api.enums.CrudOperation}
 * （CRUD 操作词汇表）、{@link com.flystem.crud.api.enums.GenerationMode}（api / impl 生成范围）、
 * {@link com.flystem.crud.api.enums.PackageLayout}（产物包布局）、
 * {@link com.flystem.crud.api.enums.TenantIdType}（全局租户值类型）、
 * {@link com.flystem.crud.api.enums.TaskScope}（异步任务可见性粒度）。
 *
 * <p><b>为什么按「种类」分包</b>：这些枚举都是<b>配置注解属性的取值域</b>（分别是
 * {@code CrudOperation[]} / {@code mode} / {@code packageLayout} / {@code tenantIdType} /
 * {@code taskScope} 的类型），
 * 自身都不是注解。留在 {@code annotation} 下会让「这个包里都是注解」的直觉失效，也让按包名筛选
 * 注解的工具误收。本模块顶层本就混用两种组织方式（{@code annotation} / {@code autoconfigure} 按
 * 种类，{@code tenant} / {@code factory} / {@code page} 按领域），故 {@code enums} 与 {@code annotation}
 * 并列不引入新的不一致。
 *
 * <p><b>新增枚举的判据</b>：若它是某个配置注解属性的取值域，放这里；若它承载行为或运行期逻辑
 * （如 {@code page.PageResult} 是分页出参、{@code tenant.TenantIdExtractor} 是解析 SPI），仍按领域放
 * 对应子包——本包只收纯取值类型，不收「恰好也是枚举」的行为载体。
 *
 * <p>注意与 {@code -generator} 模块 {@code model} 包下的同名镜像枚举区分：那边是解析配置用的内部
 * 表示（由名字 {@code valueOf} 得到），本包是<b>对外契约</b>；两者刻意不共享类型，避免使用方的
 * 编译依赖牵出生成器内部（ADR 0008：使用方只依赖 {@code -api}）。
 */
package com.flystem.crud.api.enums;
