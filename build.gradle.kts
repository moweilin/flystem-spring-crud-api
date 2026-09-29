import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.authentication.http.HttpHeaderAuthentication

plugins {
    `java-library`
    `maven-publish`
}

// 独立构建：Gitee Maven 包坐标 com.flystem:flystem-spring-crud-api
// monorepo 子模块：沿用父仓 allprojects 的 group / version
if (rootProject == project) {
    group = "com.flystem"
    version = System.getenv("VERSION")
        ?: findProperty("version")?.toString()?.takeIf { it != "unspecified" }
        ?: "0.1.0"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
    isFailOnError = false
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("flystem-spring-crud-api")
                description.set("flystem-spring-crud public API and runtime support types")
                url.set("https://gitee.com/moweilin/flystem-spring-crud-api")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    url.set("https://gitee.com/moweilin/flystem-spring-crud-api")
                    connection.set("scm:git:https://gitee.com/moweilin/flystem-spring-crud-api.git")
                }
            }
            if (rootProject == project) {
                artifactId = "flystem-spring-crud-api"
            }
        }
    }
    repositories {
        // 默认：写入本仓 repo/，push 后经 Gitee raw 作为 Maven 仓库消费
        maven {
            name = "GiteeMaven"
            url = uri(rootProject.layout.projectDirectory.dir("repo"))
        }
        // 可选：企业版 / 已开通 Packages 时，设置 GITEE_MAVEN_URL + GITEE_TOKEN 直推
        // 例：GITEE_MAVEN_URL=https://gitee.com/api/packages/moweilin/maven
        val giteeUrl = System.getenv("GITEE_MAVEN_URL")
            ?: findProperty("gitee.maven.url")?.toString()
        val giteeToken = System.getenv("GITEE_TOKEN")
            ?: findProperty("gitee.token")?.toString()
        if (!giteeUrl.isNullOrBlank() && !giteeToken.isNullOrBlank()) {
            maven {
                name = "GiteePackages"
                url = uri(giteeUrl)
                credentials(HttpHeaderCredentials::class) {
                    name = "Authorization"
                    value = "token $giteeToken"
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }
    }
}
