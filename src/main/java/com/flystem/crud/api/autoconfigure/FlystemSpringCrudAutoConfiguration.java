package com.flystem.crud.api.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flystem.crud.api.id.IdGenerator;
import com.flystem.crud.api.id.UuidIdGenerator;
import com.flystem.crud.api.id.UuidV7IdGenerator;
import com.flystem.crud.api.io.ExportWriter;
import com.flystem.crud.api.io.ImportParser;
import com.flystem.crud.api.io.JsonExportWriter;
import com.flystem.crud.api.io.JsonImportParser;
import com.flystem.crud.api.task.CrudTaskManager;
import com.flystem.crud.api.task.InMemoryCrudTaskManager;
import com.flystem.crud.api.tenant.ReflectiveTenantIdExtractor;
import com.flystem.crud.api.tenant.TenantIdExtractor;
import com.flystem.crud.api.link.DefaultRelationshipLoader;
import com.flystem.crud.api.link.RelationshipLoader;
import com.flystem.crud.api.link.TypedLinkableLoader;
import com.flystem.crud.api.wrap.DefaultResultWrapper;
import com.flystem.crud.api.wrap.ResultWrapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 本库<b>自身</b>的 Spring Boot 自动配置：为运行期支撑类型注册默认 bean，使用方声明同类型 bean
 * 即整体替换（{@code @ConditionalOnMissingBean}）。
 *
 * <p>与「Spring 自动配置层」<b>不是一回事</b>：后者是每实体一份的生成产物
 * {@code F<Entity>AutoConfiguration}（CONTEXT.md「Spring 自动配置层」，wb#8 未实现）；本类是库级、
 * 手写、随 {@code -api} 构件分发的一份。
 *
 * <p>注册经 {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * （Boot 3 / Boot 4 同机制，ADR 0012）。非 Boot 的纯 Spring 使用方不会被自动装配，需自行声明 bean。
 *
 * <p>本类对第三方依赖全部 {@code compileOnly}（不进发布 POM、不传递，ADR 0008 非侵入），故<b>凡引用
 * 第三方类型的</b>装配块都以 {@code @ConditionalOnClass} 守卫，并放在<b>嵌套</b>配置类里：条件由 Boot
 * 在类加载前以 ASM 求值，缺依赖时嵌套类根本不会被加载，不会触发 {@code NoClassDefFoundError}。
 * 只用 JDK 类型的装配（如 {@link ResultWrapper}）无需守卫，直接挂在本类上。
 */
@AutoConfiguration
public class FlystemSpringCrudAutoConfiguration {

    /**
     * 关联批量加载（按目标类型一次查出多 id）：聚合所有 {@link TypedLinkableLoader} 生成 bean。
     * 使用方声明自己的 {@link RelationshipLoader} 即整体替换。
     */
    @Bean
    @ConditionalOnMissingBean(RelationshipLoader.class)
    RelationshipLoader flystemRelationshipLoader(ObjectProvider<TypedLinkableLoader> loaders) {
        return new DefaultRelationshipLoader(loaders.stream().toList());
    }

    /**
     * 主键生成（ADR 0026）：始终注册 {@link UuidIdGenerator}；{@link UuidV7IdGenerator} 仅在
     * classpath 有 {@code UuidCreator} 时注册（嵌套配置 + {@code @ConditionalOnClass}，不内嵌
     * v7 算法）。{@link IdGenerator} 接口 bean：有 v7 则优先，否则回落 v4。实体
     * {@code idGenerator=Void}（默认）时这些 bean 闲置；配了实现类 / 接口时产物才注入。
     */
    @Bean
    @ConditionalOnMissingBean(UuidIdGenerator.class)
    UuidIdGenerator flystemUuidIdGenerator() {
        return new UuidIdGenerator();
    }

    @Bean
    @ConditionalOnMissingBean(IdGenerator.class)
    IdGenerator flystemIdGenerator(ObjectProvider<UuidV7IdGenerator> uuidV7,
            UuidIdGenerator uuidIdGenerator) {
        UuidV7IdGenerator v7 = uuidV7.getIfAvailable();
        return v7 != null ? v7 : uuidIdGenerator;
    }

    /**
     * UUID v7：依赖 uuid-creator 的 {@code UuidCreator}，缺类则本嵌套配置整块不加载。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = UuidV7IdGenerator.UUID_CREATOR_CLASS)
    static class UuidV7IdGeneratorConfiguration {

        @Bean
        @ConditionalOnMissingBean(UuidV7IdGenerator.class)
        UuidV7IdGenerator flystemUuidV7IdGenerator() {
            return new UuidV7IdGenerator();
        }
    }

    /**
     * 结果包装（ADR 0020）：使用方未声明 {@link ResultWrapper} bean 时注册默认的
     * {@link DefaultResultWrapper}——执行 supplier 后经 {@link RelationshipLoader} 批量填充
     * {@code R.relationships}，再装入 {@code R.ok(data, relationships)}；<b>不吞异常</b>
     * （异常仍走使用方的 {@code @ControllerAdvice}；要「异常 → 失败码」就换掉本 bean）。
     * 只有当某实体的 {@code @ControllerGeneration.resultWrapper} 配成实现该 SPI 的类时，生成的
     * Controller 才会注入它；未配则这个 bean 只是闲置，无副作用。仅用 JDK 类型，故无需
     * {@code @ConditionalOnClass} 守卫。
     */
    @Bean
    @ConditionalOnMissingBean(ResultWrapper.class)
    ResultWrapper flystemResultWrapper(ObjectProvider<RelationshipLoader> relationshipLoader) {
        return new DefaultResultWrapper(relationshipLoader.getIfAvailable());
    }

    /**
     * 异步导入 / 导出任务的管理器（ADR 0022）：使用方未声明 {@link CrudTaskManager} bean 时注册默认的
     * {@link InMemoryCrudTaskManager}（进程内守护线程池 + 内存保存结果）。
     *
     * <p><b>默认实现只适合单实例与非生产场景</b>：不持久（重启即丢）、不跨实例（多副本部署时查询 /
     * 下载可能落到另一个实例上而 404）、结果文件全量在堆里（大表导出吃内存）。多副本或大文件场景请
     * 声明自己的 {@link CrudTaskManager} bean（数据库 / Redis / 对象存储 + 持久队列），即整体替换。
     *
     * <p>仅用 JDK 类型，故无需 {@code @ConditionalOnClass} 守卫。
     */
    @Bean
    @ConditionalOnMissingBean(CrudTaskManager.class)
    CrudTaskManager flystemCrudTaskManager() {
        return new InMemoryCrudTaskManager();
    }

    /**
     * 导入 / 导出的文件编解码（协议 §4.5）：默认 <b>JSON</b>。守卫 {@code ObjectMapper}——只有引了
     * Jackson 的项目才装配（Boot web 应用恒有），未引入时这两个 bean 不注册，生成的 import / export
     * 端点会在启动时报缺 bean，比默默生成用不了的端点更好排查。
     *
     * <p>优先复用<b>应用自己的</b> {@code ObjectMapper}（{@link ObjectProvider} 取，缺则退到实现内部
     * 新建的实例）：这样应用配置的日期格式、命名策略、注册模块对导入导出同样生效，与
     * {@code @RequestBody} / {@code @ResponseBody} 的 JSON 形状保持一致。
     *
     * <p>要换 CSV / Excel：声明自己的 {@link ImportParser} / {@link ExportWriter} bean 即整体替换。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ObjectMapper.class)
    static class FileCodecConfiguration {

        @Bean
        @ConditionalOnMissingBean(ImportParser.class)
        ImportParser flystemImportParser(ObjectProvider<ObjectMapper> objectMapper) {
            ObjectMapper mapper = objectMapper.getIfAvailable();
            return mapper == null ? new JsonImportParser() : new JsonImportParser(mapper);
        }

        @Bean
        @ConditionalOnMissingBean(ExportWriter.class)
        ExportWriter flystemExportWriter(ObjectProvider<ObjectMapper> objectMapper) {
            ObjectMapper mapper = objectMapper.getIfAvailable();
            return mapper == null ? new JsonExportWriter() : new JsonExportWriter(mapper);
        }
    }

    /**
     * 当前租户解析（协议 §4.5）：使用方未声明 {@link TenantIdExtractor} bean 时注册默认实现
     * {@link ReflectiveTenantIdExtractor}。守卫 {@code UserDetails}：仅多租户 Controller 产物需要
     * spring-security-core，未引入它的项目（含全部单租户项目）不受影响。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(UserDetails.class)
    static class TenantIdExtractorConfiguration {

        @Bean
        @ConditionalOnMissingBean(TenantIdExtractor.class)
        TenantIdExtractor flystemTenantIdExtractor() {
            return new ReflectiveTenantIdExtractor();
        }
    }
}