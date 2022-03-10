package buythedip.refreshers;

import buythedip.LoggerSingleton;

public class InstrumentRefreshStatus {
    private String status;
    private Integer progress;

    public InstrumentRefreshStatus(String status, Integer progress) {
        this.status = status;
        this.progress = progress;
    }

    public void setStatus(String status) {
        LoggerSingleton.getInstance().debug(status);
        this.status = status;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getStatus() {
        return status;
    }

    public Integer getProgress() {
        return progress;
    }

    public void reset() {
        status = "";
        progress = 0;
    }
}
