package com.flystem.crud.api.enums;

/**
 * 产物包布局（CONTEXT.md「产物包布局」）：生成产物在 {@code generationPackage} 基准包下的组织方式。
 *
 * <p>与 {@code packagePerEntity}（实体根包）<b>正交</b>，可叠加：后者决定基准包是认领包（带
 * {@code @CrudGeneration} 的包）本身还是认领包 + 实体名小写，本枚举决定基准包内是否再按产物层分子包。四种组合：
 *
 * <table>
 *   <caption>布局组合</caption>
 *   <tr><th>packagePerEntity</th><th>packageLayout</th><th>Controller 产物包</th></tr>
 *   <tr><td>false</td><td>LAYERED（默认）</td><td>{@code com.acme.order.controller}</td></tr>
 *   <tr><td>false</td><td>FLAT</td><td>{@code com.acme.order}</td></tr>
 *   <tr><td>true</td><td>LAYERED（默认）</td><td>{@code com.acme.order.checkout.controller}</td></tr>
 *   <tr><td>true</td><td>FLAT</td><td>{@code com.acme.order.checkout}</td></tr>
 * </table>
 *
 * <p>子包名随目标生态而定（协议 §4.0「产物包布局」）：MyBatis 路径的数据访问层为 {@code mapper}，
 * JPA 路径将为 {@code repository}。
 */
public enum PackageLayout {

    /**
     * 分层（默认）：各产物层落在基准包下各自的子包（如 {@code controller} / {@code form} /
     * {@code service} / {@code service.impl} / {@code mapper} / {@code dto} / {@code assembler}），
     * 跨层引用由生成器自动补 import。按层归置便于分包扫描、模块边界与代码导航。
     */
    LAYERED,

    /**
     * 平铺：同一实体的全部产物落在同一包内——兄弟产物之间免 import，包数量少。切换布局后旧位置的
     * 产物由生成清单的 cleanStale 清理（协议 §3 步骤 7），不留双份。
     */
    FLAT
}