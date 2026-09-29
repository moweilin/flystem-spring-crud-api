package com.flystem.crud.api.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 导入解析 SPI（协议 §4.5）：把上传的文件解析成 DTO 列表。生成的 IMPORT 端点收
 * {@code MultipartFile}，经本接口取得 {@code List<DTO>} 后交 Service 的 {@code importBatch}——
 * 故<b>文件格式只影响这一层</b>，Service 契约与租户填充逻辑完全不变。
 *
 * <p>默认实现 {@link JsonImportParser}（JSON 数组）由 {@code FlystemSpringCrudAutoConfiguration} 在
 * 使用方未声明同类型 bean 时注册；声明自己的 bean 即<b>整体替换</b>（换 CSV / Excel / 固定宽度文本皆可，
 * 机制同 {@code TenantIdExtractor}、{@code ResultWrapper}）。
 *
 * <p>{@code filename} 一并传入，便于实现按扩展名分派（如 {@code .csv} 走 CSV 解析、{@code .xlsx} 走
 * POI）；本接口的默认实现会校验扩展名并在不匹配时给出明确错误，而不是让底层解析器抛出难懂的异常。
 */
public interface ImportParser {

    /**
     * 解析上传内容。
     *
     * @param content  文件内容流（由调用方负责关闭）
     * @param filename 客户端给出的原始文件名，可能为 {@code null}
     * @param itemType 目标 DTO 类型（生成产物传入本实体的 DTO class）
     * @param <T>      DTO 类型
     * @return 解析出的 DTO 列表；空文件应返回空列表而非 {@code null}
     * @throws IOException 读取或解析失败（含格式不符）——生成端点声明 {@code throws IOException}，
     *                     由使用方的异常处理决定对外形态
     */
    <T> List<T> parse(InputStream content, String filename, Class<T> itemType) throws IOException;
}
