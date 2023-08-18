package kd.cd.webapi.log;

import com.alibaba.fastjson.JSONObject;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.logorm.LogORM;
import kd.bos.threads.ThreadPools;
import kd.cd.webapi.util.ExceptionUtils;
import kd.cd.webapi.util.JsonUtils;
import kd.cd.webapi.util.SystemPropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class OkLogger {
    private static final String LOG_FORM = SystemPropertyUtils.getString("outapilog.formid.log", "outapilog");
    private static final int THREAD_NUM = SystemPropertyUtils.getInt("outapilog.logwriter.threads", 10, 2, 30);
    private static final ExecutorService exectorService = ThreadPools.newExecutorService("outapi-logger", THREAD_NUM);
    private static final Log log = LogFactory.getLog(OkLogger.class);
    private static volatile OkLogger okLogger;

    private OkLogger() {
        if (okLogger != null) {
            throw new IllegalStateException("No reflection allowed here");
        }
    }

    public static OkLogger require() {
        if (okLogger == null) {
            synchronized (OkLogger.class) {
                if (okLogger == null) {
                    okLogger = new OkLogger();
                }
            }
        }
        return okLogger;
    }

    public void logAsync(LogOption logOption) {
        exectorService.execute(() -> logging(logOption));
    }

    public void log(LogOption logOption) {
        try (TXHandle ignored = TX.notSupported()) {
            logging(logOption);
        }
    }

    private static void logging(LogOption logOption) {
        try {
            RequestContext rc = logOption.requestContext;
            if (rc == null) {
                rc = RequestContext.get();
            }
            save(rc, logOption);

        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Save apilog failed", e);
            }
        }
    }

    private static void save(RequestContext rc, LogOption logOption) {
        JSONObject reqObj = logOption.reqInfo;

        String url = Optional.ofNullable(reqObj)
                .map(r -> r.getString("url"))
                .orElse("");

        String method = Optional.ofNullable(reqObj)
                .map(r -> r.getString("method"))
                .orElse("");

        String header = Optional.ofNullable(reqObj)
                .map(r -> StringUtils.chomp(r.getString("headers")))
                .orElse("");

        boolean format = logOption.enableFormat;
        String reqString = Optional.ofNullable(reqObj)
                .map(r -> format ? JsonUtils.fuzzyFormat(r.getString("body")) : r.getString("body"))
                .orElse("");

        JSONObject respObj = logOption.respInfo;

        String respString = Optional.ofNullable(respObj)
                .map(r -> format ? JsonUtils.fuzzyFormat(r.getString("body")) : r.getString("body"))
                .orElse("");
        Integer limitSize = logOption.respLimitSize;
        if (limitSize != null && limitSize > 0 && limitSize < respString.length()) {
            respString = respString.substring(0, limitSize);
        }

        String status;
        String errMsg = "";
        if (respObj == null) {
            status = String.valueOf(false);
            errMsg = formatExceptionText(logOption.exception);
        } else if (Boolean.TRUE.equals(respObj.getBoolean("success"))) {
            status = String.valueOf(true);
        } else {
            status = String.valueOf(false);
            errMsg = respObj.getString("message");
        }

        MainEntityType entityType = EntityMetadataCache.getDataEntityType(LOG_FORM);
        DynamicObject o = new DynamicObject(entityType);
        o.set("opdesc", url);
        o.set("reqmethod", method);
        o.set("reheader_tag", header);
        o.set("request_tag", reqString);
        o.set("response_tag", respString);
        o.set("errmsg_tag", errMsg);
        o.set("status", status);
        o.set("times", logOption.timeCost);
        o.set("times_tag", logOption.trackInfo);
        o.set("bizobject", logOption.bizobject);
        o.set("opname", logOption.opname);
        o.set("cloudname", logOption.cloudname);
        o.set("appname", logOption.appname);
        o.set("thirdappname", logOption.thirdappname);
        o.set("opdate", new Date());
        o.set("optype", logOption.optype);
        o.set("customtag", logOption.customTag);
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

    private static String formatExceptionText(Throwable t) {
        return String.format("--- %s --- %n%s", ExceptionUtils.getSimpleMsg(t), ExceptionUtils.getFullTraceString(t));
    }
}