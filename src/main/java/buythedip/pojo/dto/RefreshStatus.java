package buythedip.pojo.dto;

import buythedip.springbeans.refreshers.CandleRefresher;
import org.apache.logging.log4j.LogManager;

public class RefreshStatus {
    private String status;
    private Integer progress;

    public RefreshStatus() {
        this.status = "";
        this.progress = 0;
    }

    public void setStatus(String status) {
        LogManager.getLogger(RefreshStatus.class).debug(status);
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
