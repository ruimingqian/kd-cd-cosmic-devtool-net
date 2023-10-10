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
import kd.cd.webapi.util.ExceptionUtils;
import kd.cd.webapi.util.JsonUtils;
import kd.cd.webapi.util.SystemPropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class LogWriter {
    private static final String LOG_FORM = SystemPropertyUtils.getString("outapilog.formid.log", "outapilog");
    private static final int THREAD_NUM = SystemPropertyUtils.getInt("outapilog.logwriter.threads", 10, 2, 30);
    private static final Log log = LogFactory.getLog(LogWriter.class);
    private static final ExecutorService exectorService = ThreadPools.newExecutorService("outapi-logger", THREAD_NUM);
    private static volatile LogWriter logWriter;

    private LogWriter() {
        if (logWriter != null) {
            throw new IllegalStateException("No reflection allowed here");
        }
    }

    public static LogWriter require() {
        if (logWriter == null) {
            synchronized (LogWriter.class) {
                if (logWriter == null) {
                    logWriter = new LogWriter();
                }
            }
        }
        return logWriter;
    }

    public void writeAsync(LogOption logOption) {
        exectorService.execute(() -> save(logOption));
    }

    public void write(LogOption logOption) {
        try (TXHandle ignored = TX.notSupported()) {
            save(logOption);
        }
    }

    private static void save(LogOption logOption) {
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
        String reqString = logOption.enableFormat ? JsonUtils.fuzzyFormat(bfReq.getBody()) : bfReq.getBody();
        reqString = chop(reqString, logOption.chopSize);

        BufferedResponse bfResp = logOption.bufferedResp;
        String respString = logOption.enableFormat ? JsonUtils.fuzzyFormat(bfResp.getBody()) : bfResp.getBody();
        respString = chop(respString, logOption.chopSize);

        String errMsg = "";
        if (!bfResp.isSuccess()) {
            if (logOption.exception != null) {
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
        o.set("opdesc", bfReq.getUrl());
        o.set("reqmethod", bfReq.getMethod());
        o.set("reheader_tag", StringUtils.chomp(bfReq.getHeaders()));
        o.set("request_tag", reqString);
        o.set("response_tag", respString);
        o.set("requestext_tag", logOption.requestExt);
        o.set("responseext_tag", logOption.responseExt);
        o.set("code", bfResp.getCode());
        o.set("errmsg_tag", errMsg);
        o.set("status", String.valueOf(bfResp.isSuccess()));
        o.set("times", logOption.timeCost);
        o.set("times_tag", logOption.trackInfo);
        o.set("bizobject", logOption.bizFormId);
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
            o.set("accountid", rc.getAccountId());
            o.set("client", rc.getClient());
            o.set("traceid", rc.getTraceId());
        }

        List<DynamicObject> list = new ArrayList<>(1);
        list.add(o);
        LogORM.create().insert(list);
    }

    private static String chop(String content, Integer chopSize) {
        if (chopSize != null && chopSize > 0 && chopSize < content.length()) {
            return content.substring(0, chopSize);
        }
        return content;
    }

    private static String formatExceptionText(Throwable t) {
        return String.format("--- %s --- %n%s", ExceptionUtils.getSimpleMsg(t), ExceptionUtils.getFullTraceString(t));
    }
}