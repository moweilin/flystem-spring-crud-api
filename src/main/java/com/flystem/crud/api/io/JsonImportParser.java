package com.flystem.crud.api.io;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

/**
 * {@link ImportParser} 的默认实现：文件内容须是 <b>DTO 的 JSON 数组</b>（如
 * {@code [{"title":"B1","author":"A"},{"title":"B2"}]})。
 *
 * <p>选 JSON 作默认是因为它<b>零额外依赖</b>：控制器层本就要求 spring-web，而 Boot web 应用恒有
 * Jackson，故不需要为默认格式再引 POI / commons-csv。要 CSV / Excel，声明自己的 {@link ImportParser}
 * bean 即整体替换（{@code @ConditionalOnMissingBean}）。
 *
 * <p><b>刻意校验扩展名</b>：文件名非 {@code .json}（且非空）时直接抛 {@link IOException} 并说明出路，
 * 而不是把 Excel 字节喂给 Jackson 让它吐出「Unrecognized token 'PK'」这类无从下手的错误——错误应当
 * 指出「默认只支持 JSON，换格式请替换 bean」，而不是让人去猜。
 *
 * <p>复用注入的 {@link ObjectMapper}（若有）：这样应用自己的 Jackson 配置（日期格式、命名策略、
 * 模块注册）对导入同样生效，与 {@code @RequestBody} 的反序列化行为一致。
 */
public class JsonImportParser implements ImportParser {

    private final ObjectMapper objectMapper;

    /** 使用内部新建的 {@link ObjectMapper}（无 Spring 上下文时的退路）。 */
    public JsonImportParser() {
        this(new ObjectMapper());
    }

    public JsonImportParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> List<T> parse(InputStream content, String filename, Class<T> itemType) throws IOException {
        if (filename != null && !filename.isBlank()
                && !filename.toLowerCase(Locale.ROOT).endsWith(".json")) {
            throw new IOException("default ImportParser only supports JSON arrays, but got '" + filename
                    + "'; register your own ImportParser bean for other formats (CSV / Excel / ...)");
        }
        JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, itemType);
        return objectMapper.readValue(content, listType);
    }
}
