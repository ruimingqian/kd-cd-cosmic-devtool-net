package kd.cd.webapi.log;

import com.alibaba.fastjson.JSONObject;
import kd.bos.context.RequestContext;
import lombok.Setter;

/**
 * <b>自定义第三方日志参数</b>
 * <p>
 * <b>示例</b>
 * <pre> {@code
 *   LogOption logOption = RequestConfig.logOption("apinum");
 *   LogOption logOption = new LogOption("kdcd_test", "apinum", "测试接口");
 * }</pre>
 *
 * @author qrm
 * @version 1.0
 * @since cosmic 5.0
 */
public class LogOption implements Cloneable {

    /**
     * 是否开启新线程保存入库
     */
    @Setter
    protected boolean enableNewThread = true;

    /**
     * 是否记录完整请求信息（是否包含请求体内容）
     */
    @Setter
    protected boolean recordFullRequest = true;

    /**
     * 是否记录完整响应信息（是否包含响应体内容）
     */
    @Setter
    protected boolean recordFullResponse = true;

    /**
     * 是否格式化请求响应信息
     */
    @Setter
    protected boolean enableFormat;

    /**
     * 响应截取长度
     */
    @Setter
    protected Integer respLimitSize;

    /**
     * 表单标识
     */
    protected String bizobject;

    /**
     * 操作类型
     */
    protected String optype;

    /**
     * 云(可选，传入表单标识后可不传)
     */
    @Setter
    protected String cloudname;

    /**
     * 应用((可选，传入表单标识后可不传))
     */
    @Setter
    protected String appname;

    /**
     * 第三方API编码
     */
    protected String opname;

    /**
     * 第三方名称
     */
    protected String thirdappname;

    /**
     * 异常信息
     */
    protected Exception exception;

    /**
     * 请求耗时(ms)
     */
    protected long timeCost;

    /**
     * 自定义信息
     */
    @Setter
    protected String customTag;

    /**
     * 请求阶段耗时
     */
    protected String trackInfo;

    /**
     * 请求信息
     */
    protected JSONObject reqInfo;

    /**
     * 响应信息
     */
    protected JSONObject respInfo;

    /**
     * RequestContext
     */
    protected RequestContext requestContext;

    public LogOption(String entityId, String apiNumber, String apiName) {
        this.bizobject = entityId;
        this.opname = apiNumber;
        this.thirdappname = apiName;
    }

    @Override
    public LogOption clone() {
        try {
            return (LogOption) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}