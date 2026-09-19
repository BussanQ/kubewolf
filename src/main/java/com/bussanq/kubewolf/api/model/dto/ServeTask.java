package com.bussanq.kubewolf.api.model.dto;

import com.bussanq.kubewolf.api.model.base.BaseServeTask;
import java.util.Date;

@SuppressWarnings("serial")
public class ServeTask extends BaseServeTask<ServeTask> {
    public static final ServeTask dao = new ServeTask().dao();
    private String status;
    public String getStatus() { return status == null ? getActualStatus() : status; }
    public void setStatus(String value) { status = value; }
    public String getModelId() { return getStr("model_id"); }
    public Integer getGatewayChannelId() { return getInt("gateway_channel_id"); }
    public String getNamespace() { return getStr("namespace"); }
    public String getResourceName() { return getStr("resource_name"); }
    public String getDesiredState() { return getStr("desired_state"); }
    public String getActualStatus() { return getStr("actual_status"); }
    public String getLastError() { return getStr("last_error"); }
    public int getRetryCount() { Integer n = getInt("retry_count"); return n == null ? 0 : n; }
    public long getGeneration() { Number n = get("generation"); return n == null ? 0 : n.longValue(); }
    public boolean isGatewayRegistered() { Boolean b = getBoolean("gateway_registered"); return Boolean.TRUE.equals(b); }
    public String getModelName() { return getStr("model_name"); }
    public String getGpuResource() { return getStr("gpu_resource"); }
    public Date getLastSyncTime() { return getDate("last_sync_time"); }
}
