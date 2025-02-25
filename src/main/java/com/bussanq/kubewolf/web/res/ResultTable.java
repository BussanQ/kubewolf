package com.bussanq.kubewolf.web.res;

import java.util.List;

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

    public static ResultJson ok(List data) {
        ResultJson jsonResult = ResultJson.ok();
        jsonResult.set("count", data.size());
        jsonResult.set("data", data);
        return jsonResult;
    }
}
