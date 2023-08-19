package kd.cd.webapi.log;

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
import kd.cd.webapi.okhttp.BufferedRequest;
import kd.cd.webapi.okhttp.BufferedResponse;
import kd.cd.webapi.util.ExceptionUtils;
import kd.cd.webapi.util.JsonUtils;
import kd.cd.webapi.util.SystemPropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class OkLogger {
    private static final String LOG_FORM = SystemPropertyUtils.getString("outapilog.formid.log", "outapilog");
    private static final int THREAD_NUM = SystemPropertyUtils.getInt("outapilog.logwriter.threads", 10, 2, 30);
    private static final Log log = LogFactory.getLog(OkLogger.class);
    private static final ExecutorService exectorService = ThreadPools.newExecutorService("outapi-logger", THREAD_NUM);
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
        BufferedRequest bfReq = logOption.bufferedReq;
        String url = bfReq.getUrl();
        String method = bfReq.getMethod();
        String header = StringUtils.chomp(bfReq.getHeaders());
        String reqString = logOption.enableFormat ? JsonUtils.fuzzyFormat(bfReq.getBody()) : bfReq.getBody();

        BufferedResponse bfResp = logOption.bufferedResp;
        String respString = logOption.enableFormat ? JsonUtils.fuzzyFormat(bfResp.getBody()) : bfResp.getBody();
        Integer limitSize = logOption.respLimitSize;
        if (limitSize != null && limitSize > 0 && limitSize < respString.length()) {
            respString = respString.substring(0, limitSize);
        }

        String status = String.valueOf(bfResp.isSuccess());
        String errMsg = "";
        if (!bfResp.isSuccess()) {
            if (logOption.exception == null) {
                errMsg = formatExceptionText(logOption.exception);
            } else {
                errMsg = bfResp.getMessage();
            }
        }

        if (logOption.finalAdjustment != null) {
            try {
                logOption.finalAdjustment.accept(logOption);
            } catch (Exception ignored) {
                // ignore
            }
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