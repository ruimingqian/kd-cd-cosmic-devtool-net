package kd.cd.net.log;

import com.fasterxml.jackson.databind.node.ObjectNode;
import kd.cd.net.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Optional;

public class OkHttpLogger extends AbstractNetLogger {

    public LogDto mapping(LogParam logParam) {
        LogDto logDto = new LogDto();
        ObjectNode reqNode = logParam.getReqInfo();
        ObjectNode respNode = logParam.getRespInfo();

        logDto.setUrl(Optional.ofNullable(reqNode).map(o -> o.get("url").asText()).orElse(""));
        logDto.setMethod(Optional.ofNullable(reqNode).map(r -> r.get("method").asText()).orElse(""));
        logDto.setHeader(Optional.ofNullable(reqNode).map(r -> StringUtils.chomp(r.get("headers").asText())).orElse(""));
        boolean format = logParam.isEnableFormat();
        logDto.setRequest(Optional.ofNullable(reqNode).map(r -> format ? JsonUtils.fuzzyFormat(r.get("body").asText()) : r.get("body").asText()).orElse(""));
        logDto.setResponse(Optional.ofNullable(respNode).map(r -> format ? JsonUtils.fuzzyFormat(r.get("body").asText()) : r.get("body").asText()).orElse(""));

        if (respNode == null) {
            logDto.setStatus(String.valueOf(false));
            logDto.setErrMsg(formatException(logParam.getException()));
        } else if (respNode.get("success").asBoolean()) {
            logDto.setStatus(String.valueOf(true));
        } else {
            logDto.setStatus(String.valueOf(false));
            logDto.setErrMsg(respNode.get("message").asText());
        }

        logDto.setTimecost(logParam.getTimeCost());
        logDto.setTrackInfo(logParam.getTrackInfo());
        logDto.setApiNum(logParam.getOpname());
        logDto.setApiName(logParam.getThirdappname());
        logDto.setBizForm(logParam.getBizobject());
        logDto.setOpdate(new Date());
        logDto.setOperateType(logParam.getOptype());
        logDto.setCustomTag(logParam.getCustomTag());

        return logDto;
    }
}