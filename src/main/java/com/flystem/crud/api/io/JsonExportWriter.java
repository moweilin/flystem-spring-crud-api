package com.flystem.crud.api.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;

/**
 * {@link ExportWriter} 的默认实现：写出 <b>DTO 的 JSON 数组</b>，文件名 {@code <baseName>.json}、
 * Content-Type {@code application/json}。
 *
 * <p>选 JSON 的理由同 {@link JsonImportParser}：控制器层已要求 spring-web，Boot web 应用恒有 Jackson，
 * 默认格式因此零额外依赖。要 CSV / Excel，声明自己的 {@link ExportWriter} bean 即整体替换。
 *
 * <p>复用注入的 {@link ObjectMapper}（若有），故应用自己的 Jackson 配置对导出同样生效——导出内容与
 * 端点直接返回同一批 DTO 时的 JSON 形状一致，不会出现「屏幕上是一个样、下载下来是另一个样」。
 */
public class JsonExportWriter implements ExportWriter {

    /** 导出文件的 Content-Type。 */
    public static final String CONTENT_TYPE = "application/json";

    /** 导出文件扩展名（含点）。 */
    public static final String EXTENSION = ".json";

    private final ObjectMapper objectMapper;

    /** 使用内部新建的 {@link ObjectMapper}（无 Spring 上下文时的退路）。 */
    public JsonExportWriter() {
        this(new ObjectMapper());
    }

    public JsonExportWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> ExportFile write(List<T> items, Class<T> itemType, String baseName) throws IOException {
        byte[] content = objectMapper.writeValueAsBytes(items == null ? List.of() : items);
        return ExportFile.of(baseName + EXTENSION, CONTENT_TYPE, content);
    }
}
