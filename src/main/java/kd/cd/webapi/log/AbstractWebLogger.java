package kd.cd.webapi.log;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.id.ID;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.logorm.LogORM;
import kd.bos.threads.ThreadPools;
import kd.cd.webapi.utils.ExceptionUtils;
import kd.cd.webapi.utils.SystemPropertyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 接口日志
 *
 * @author qrm
 * @version 1.0
 * @since cosmic 5.0
 */
public abstract class AbstractWebLogger implements WebLog<LogParam> {
    private static final Log log = LogFactory.getLog(AbstractWebLogger.class);
    private static final String LOG_FORM = SystemPropertyUtils.getString("outapilog.formid.log", "outapilog");
    private static final int THREAD_NUM = SystemPropertyUtils.getInt("outapilog.logwriter.threads", 10, 2, 30);
    private static final ExecutorService exectorService = ThreadPools.newExecutorService("outapi-logger", THREAD_NUM);

    public void logAsync(LogParam logParam) {
        exectorService.execute(() -> log(logParam));
    }

    public void log(LogParam logParam) {
        try {
            LogDto logDto = mapping(logParam);
            RequestContext rc = logParam.getRequestContext();
            if (rc == null) {
                rc = RequestContext.get();
            }
            save(LOG_FORM, rc, logDto);

        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Save apilog failed", e);
            }
        }
    }

    protected abstract LogDto mapping(LogParam logParam);

    public static void save(String logFormId, RequestContext rc, LogDto logDto) {
        MainEntityType entityType = EntityMetadataCache.getDataEntityType(logFormId);
        DynamicObject o = new DynamicObject(entityType);
        o.getDataEntityType().getPrimaryKey().setValueFast(o, ID.genLongId());

        o.set("opname", logDto.getApiNum());
        o.set("thirdappname", logDto.getApiName());
        o.set("cloudname", logDto.getCloud());
        o.set("appname", logDto.getBizapp());
        o.set("bizobject", logDto.getBizForm());
        o.set("opdesc", logDto.getUrl());
        o.set("reqmethod", logDto.getMethod());
        o.set("reheader_tag", logDto.getHeader());
        o.set("request_tag", logDto.getRequest());
        o.set("response_tag", logDto.getResponse());
        o.set("errmsg_tag", logDto.getErrMsg());
        o.set("status", logDto.getStatus());
        o.set("times", logDto.getTimecost());
        o.set("times_tag", logDto.getTrackInfo());
        o.set("operationtimecost", logDto.getOpTimecost());
        o.set("opdate", logDto.getOpdate());
        o.set("optype", logDto.getOperateType());
        o.set("customtag", logDto.getCustomTag());
        if (rc != null) {
            o.set("userid", rc.getCurrUserId());
            o.set("username", rc.getUserName());
            o.set("ip", rc.getLoginIP());
            o.set("tenantid", rc.getTenantCode());
            o.set("client", rc.getClient());
            o.set("traceid", rc.getTraceId());
        }

        List<DynamicObject> list = new ArrayList<>(1);
        list.add(o);
        LogORM.create().insert(list);
    }

    public String formatException(Throwable t) {
        return String.format("[--- %s ---] %n%s", ExceptionUtils.getSimpleMsg(t), ExceptionUtils.getFullTraceString(t));
    }
}
