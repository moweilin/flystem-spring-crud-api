package com.flystem.crud.api.io;

/**
 * 导出产物：一个可下载的文件（文件名 + Content-Type + 字节内容）。
 *
 * <p>本类型<b>刻意不含任何 HTTP 类型</b>（{@code ResponseEntity} / {@code HttpHeaders} /
 * {@code MediaType}）——那是控制器层的事。生成的下载端点把本对象映射为
 * {@code ResponseEntity<Resource>}（附 {@code Content-Disposition: attachment}），于是 {@code -api}
 * 不必依赖 spring-web，编解码实现也能在非 Web 场景（定时任务导出到磁盘 / 对象存储）复用。
 *
 * <p>异步任务（ADR 0022）下，本对象由<b>任务体</b>写出、经 {@code TaskContext.resultFile}
 * 交任务管理器保管，客户端再经下载端点取回——故它是「任务结果文件」的载体（EXPORT 全量导出或
 * IMPORT 的结果文件均可）。
 *
 * <p>不可变值对象，仅 getter。
 */
public final class ExportFile {

    private final String filename;
    private final String contentType;
    private final byte[] content;

    private ExportFile(String filename, String contentType, byte[] content) {
        this.filename = filename;
        this.contentType = contentType;
        this.content = content == null ? new byte[0] : content;
    }

    /**
     * @param filename    下载文件名（含扩展名，如 {@code book.json} 或含中文）；控制器经
     *                    {@code ContentDisposition} 按 UTF-8（RFC 5987）写入
     *                    {@code Content-Disposition}，实现方仍<b>不应</b>放入引号、换行等头部非法字符
     * @param contentType MIME 类型（如 {@code application/json}）
     * @param content     文件字节；{@code null} 视为空文件
     */
    public static ExportFile of(String filename, String contentType, byte[] content) {
        return new ExportFile(filename, contentType, content);
    }

    public String filename() {
        return filename;
    }

    public String contentType() {
        return contentType;
    }

    /** 文件字节（内部数组的直接引用，长度可能为 0，永不为 {@code null}）。 */
    public byte[] content() {
        return content;
    }

    @Override
    public String toString() {
        return "ExportFile{filename=" + filename + ", contentType=" + contentType
                + ", bytes=" + content.length + "}";
    }
}
