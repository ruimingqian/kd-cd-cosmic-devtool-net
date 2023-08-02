package kd.cd.net.log;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Api日志Dto
 *
 * @author qrm
 * @version 1.0
 * @since cosmic 5.0
 */
@Getter
@Setter
@NoArgsConstructor
public class LogDto {
    /**
     * 接口地址
     */
    private String url;
    /**
     * 请求方法
     */
    private String method;
    /**
     * 请求头
     */
    private String header;
    /**
     * 请求信息
     */
    private String request;
    /**
     * 响应信息
     */
    private String response;
    /**
     * 错误信息
     */
    private String errMsg;
    /**
     * Api编码
     */
    private String apiNum;
    /**
     * Api名称
     */
    private String apiName;
    /**
     * 通用状态（'true','false'）
     */
    private String status;
    /**
     * 调用日期
     */
    private Date opdate;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 登录客户端ip
     */
    private String ip;
    /**
     * 业务对象单
     */
    private String bizForm;
    /**
     * 应用
     */
    private String bizapp;
    /**
     * 云
     */
    private String cloud;
    /**
     * Call耗时
     */
    private Long timecost;
    /**
     * 请求阶段耗时追踪
     */
    private String trackInfo;
    /**
     * 操作耗时
     */
    private String opTimecost;
    /**
     * traceid
     */
    private String traceid;
    /**
     * 操作类型
     */
    private String operateType;
    /**
     * 自定义信息
     */
    private String customTag;
}