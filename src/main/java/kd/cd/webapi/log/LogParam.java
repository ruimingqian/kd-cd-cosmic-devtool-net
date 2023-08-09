package kd.cd.webapi.log;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kd.bos.context.RequestContext;
import lombok.Getter;
import lombok.Setter;

/**
 * <b>自定义第三方日志参数</b>
 * <p>
 * <b>示例</b>
 * <pre> {@code
 *   LogParam logParam = RequestConfPool.getLogParam("apinum");
 *   LogParam logParam = new LogParam("kdcd_test", "apinum", "测试接口");
 * }</pre>
 *
 * @author qrm
 * @version 1.0
 * @since cosmic 5.0
 */
@Getter
@Setter
public class LogParam implements Cloneable {
    /**
     * 是否开启新线程保存入库
     */
    private boolean enableNewThread = true;

    /**
     * 是否记录完整请求信息（是否包含请求体内容）
     */
    private boolean recordFullRequest = true;

    /**
     * 是否记录完整响应信息（是否包含响应体内容）
     */
    private boolean recordFullResponse = true;

    /**
     * 是否格式化请求响应信息
     */
    private boolean enableFormat;

    /**
     * 响应截取长度
     */
    private Integer respLimitSize;

    /**
     * 表单标识
     */
    private String bizobject;

    /**
     * 操作类型
     */
    private String optype;

    /**
     * 云(可选，传入表单标识后可不传)
     */
    private String cloudname;

    /**
     * 应用((可选，传入表单标识后可不传))
     */
    private String appname;

    /**
     * 第三方API编码
     */
    private String opname;

    /**
     * 第三方名称
     */
    private String thirdappname;

    /**
     * 异常信息
     */
    private Exception exception;

    /**
     * 请求耗时(ms)
     */
    private long timeCost;

    /**
     * 自定义信息
     */
    private String customTag;

    /**
     * 请求阶段耗时
     */
    private String trackInfo;

    /**
     * 请求信息
     */
    private ObjectNode reqInfo;

    /**
     * 响应信息
     */
    private ObjectNode respInfo;

    /**
     * RequestContext
     */
    private RequestContext requestContext;

    public LogParam(String entityId, String apiNumber, String apiName) {
        this.bizobject = entityId;
        this.opname = apiNumber;
        this.thirdappname = apiName;
    }

    public LogParam(String entityId, String apiNumber, String apiName, String optype) {
        this.bizobject = entityId;
        this.optype = optype;
        this.opname = apiNumber;
        this.thirdappname = apiName;
    }

    @Override
    public LogParam clone() {
        try {
            return (LogParam) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}