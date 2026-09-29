package com.flystem.crud.api.io;

import java.io.IOException;
import java.util.List;

/**
 * 导出序列化 SPI（协议 §4.5）：把 DTO 列表写成可下载的文件。生成的 Service#exportBatch 返回的
 * {@code TaskBody} 在工作线程内经本接口写出 {@link ExportFile} 并交给 {@code TaskContext.resultFile}
 * （ADR 0022）。文件名主干由生成产物按 {@code Meta#ENTITY} 给出。
 *
 * <p>默认实现 {@link JsonExportWriter}（JSON 数组，{@code <baseName>.json}）由
 * {@code FlystemSpringCrudAutoConfiguration} 在使用方未声明同类型 bean 时注册；声明自己的 bean 即
 * <b>整体替换</b>（换 CSV / Excel 等）。
 */
public interface ExportWriter {

    /**
     * 序列化为一件导出文件。
     *
     * @param items    待导出的 DTO 列表（已经过 Service 的租户过滤，ADR 0013）；{@code null} 视为空
     * @param itemType DTO 类型（供基于反射 / 注解取列头的实现使用）
     * @param baseName 文件名主干（不含扩展名），由生成产物按实体名小写给出，如 {@code book}
     * @param <T>      DTO 类型
     * @return 导出文件（文件名由实现决定，通常 {@code baseName + 扩展名}）
     * @throws IOException 序列化失败
     */
    <T> ExportFile write(List<T> items, Class<T> itemType, String baseName) throws IOException;
}
