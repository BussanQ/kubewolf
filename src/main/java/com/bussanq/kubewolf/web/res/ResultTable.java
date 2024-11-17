package com.bussanq.kubewolf.web.res;

/**
 * @author bussanq
 * @date 2024/11/17
 */
public class ResultTable {

    /**
     * 返回成功
     */
    public static ResultJson ok(long count, Object data) {
        ResultJson jsonResult = ResultJson.ok();
        jsonResult.set("count", count);
        jsonResult.set("data", data);
        return jsonResult;
    }
}
