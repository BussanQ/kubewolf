package com.bussanq.kubewolf.web.res;
import com.jfinal.kit.Kv;

/**
 * 返回结果对象
 * @author bussanq
 * @date 2024/11/17
 */
public class ResultJson extends Kv {
    private static final long serialVersionUID = 1L;

    private ResultJson() {
    }

    /**
     * 返回成功
     */
    public static ResultJson ok() {
        return ok("操作成功");
    }

    /**
     * 返回成功
     */
    public static ResultJson ok(String message) {
        return ok(200, message);
    }

    /**
     * 返回成功
     */
    public static ResultJson ok(int code, String message) {
        ResultJson jsonResult = new ResultJson();
        jsonResult.put("code", code);
        jsonResult.put("message", message);
        return jsonResult;
    }

    /**
     * 返回失败
     */
    public static ResultJson error() {
        return error("操作失败");
    }

    /**
     * 返回失败
     */
    public static ResultJson error(String messag) {
        return error(500, messag);
    }

    /**
     * 返回失败
     */
    public static ResultJson error(int code, String message) {
        return ok(code, message);
    }

    /**
     * 设置code
     */
    public ResultJson setCode(int code) {
        super.put("code", code);
        return this;
    }

    /**
     * 设置message
     */
    public ResultJson setMessage(String message) {
        super.put("message", message);
        return this;
    }

    public String getMessage(){
        String message = this.get("message").toString();
        return message;
    }

    public int getCode(){
        int code = Integer.parseInt(this.get("code").toString());
        return code;
    }

    public boolean isOk(){
        return getCode() == 200;
    }

}