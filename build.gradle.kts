plugins {
    `java-library`
    `maven-publish`
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    // 首批契约（@Domain、TenantId<T>、package-info 配置注解、ServiceFactory 基类、
    // DefaultTenantIdProvider）无第三方依赖，work-breakdown #1 实施。
    // 唯一例外是 TenantIdExtractor：它引用 Spring Security 的 Authentication / UserDetails
    // （多租户 Controller 产物从认证主体解析当前租户，协议 §4.5）——compileOnly 不进发布 POM、
    // 不传递给使用方；仅多租户产物需要，由使用方自引 spring-security-core
    // （与 @ControllerSecurity 同一套 opt-in 约定，ADR 0008 非侵入 / ADR 0012 两代通用）
    compileOnly(libs.spring.security.core)
    // FlystemSpringCrudAutoConfiguration 用 @AutoConfiguration / @ConditionalOnMissingBean 注册
    // TenantIdExtractor 默认实现（使用方声明同类型 bean 即替换）；spring-context 经其传递可用
    compileOnly(libs.spring.boot.autoconfigure)
    // JsonImportParser / JsonExportWriter（导入导出文件格式的默认实现，协议 §4.5）引用 ObjectMapper；
    // 同为 compileOnly——Boot web 应用恒有 Jackson（控制器层本就要求 spring-web），故默认格式
    // **零额外依赖**，不为 CSV / Excel 预引 POI 之类重型库。要换格式：声明自己的
    // ImportParser / ExportWriter bean 即整体替换（@ConditionalOnMissingBean）。
    compileOnly(libs.jackson.databind)
    testImplementation(libs.junit.jupiter)
    // Gradle 9 不再自动注入 JUnit Platform launcher，需显式声明
    testRuntimeOnly(libs.junit.platform.launcher)
}

// 发布到 mavenLocal 供 Maven 集成模块与样例解析（协议 §6）
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
