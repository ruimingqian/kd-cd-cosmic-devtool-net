package kd.cd.webapi.okhttp.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.id.ID;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.logorm.LogORM;
import kd.bos.threads.ThreadPools;
import kd.cd.webapi.log.LogDto;
import kd.cd.webapi.log.LogOption;
import kd.cd.webapi.log.Logger;
import kd.cd.webapi.util.ExceptionUtils;
import kd.cd.webapi.util.JsonUtils;
import kd.cd.webapi.util.SystemPropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class OkLogger implements Logger<LogOption> {
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

    @Override
    public void logAsync(LogOption logOption) {
        exectorService.execute(() -> log(logOption));
    }

    @Override
    public void log(LogOption logOption) {
        try {
            LogDto logDto = convert(logOption);
            RequestContext rc = logOption.getRequestContext();
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

    private static LogDto convert(LogOption logOption) {
        LogDto logDto = new LogDto();
        ObjectNode reqNode = logOption.getReqInfo();
        ObjectNode respNode = logOption.getRespInfo();

        logDto.setUrl(Optional.ofNullable(reqNode)
                .map(o -> o.get("url").asText())
                .orElse(""));
        logDto.setMethod(Optional.ofNullable(reqNode)
                .map(r -> r.get("method").asText())
                .orElse(""));
        logDto.setHeader(Optional.ofNullable(reqNode)
                .map(r -> StringUtils.chomp(r.get("headers").asText()))
                .orElse(""));

        boolean format = logOption.isEnableFormat();
        logDto.setRequest(Optional.ofNullable(reqNode)
                .map(r -> format ? JsonUtils.fuzzyFormat(r.get("body").asText()) : r.get("body").asText())
                .orElse(""));

        String respString = Optional.ofNullable(respNode)
                .map(r -> format ? JsonUtils.fuzzyFormat(r.get("body").asText()) : r.get("body").asText())
                .orElse("");
        Integer limitSize = logOption.getRespLimitSize();
        if (limitSize != null && limitSize > 0 && limitSize < respString.length()) {
            respString = respString.substring(0, limitSize);
        }
        logDto.setResponse(respString);

        if (respNode == null) {
            logDto.setStatus(String.valueOf(false));
            logDto.setErrMsg(formatExceptionText(logOption.getException()));
        } else if (respNode.get("success").asBoolean()) {
            logDto.setStatus(String.valueOf(true));
        } else {
            logDto.setStatus(String.valueOf(false));
            logDto.setErrMsg(respNode.get("message").asText());
        }

        logDto.setTimecost(logOption.getTimeCost());
        logDto.setTrackInfo(logOption.getTrackInfo());
        logDto.setApiNum(logOption.getOpname());
        logDto.setApiName(logOption.getThirdappname());
        logDto.setBizForm(logOption.getBizobject());
        logDto.setOpdate(new Date());
        logDto.setOperateType(logOption.getOptype());
        logDto.setCustomTag(logOption.getCustomTag());

        return logDto;
    }

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

    private static String formatExceptionText(Throwable t) {
        return String.format("--- %s --- %n%s", ExceptionUtils.getSimpleMsg(t), ExceptionUtils.getFullTraceString(t));
    }
}