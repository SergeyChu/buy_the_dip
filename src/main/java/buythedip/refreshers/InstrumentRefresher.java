package buythedip.refreshers;

import buythedip.DBUtils;
import buythedip.LoggerSingleton;
import buythedip.entities.InstrumentJPA;
import buythedip.entities.InstrumentRefreshResponse;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class InstrumentRefresher {
    private final InstrumentRefreshStatus status = new InstrumentRefreshStatus("", 0);
    private final AtomicBoolean isRefreshCalled = new AtomicBoolean(false);
    private final Logger logger = LoggerSingleton.getInstance();

    @Autowired
    protected DBUtils dbUtils;

    public String getCurrentStatus() {
        return new JSONObject()
                .put("status", status.getStatus())
                .put("progress", status.getProgress())
                .toString();
    }

    public DeferredResult<ResponseEntity<InstrumentRefreshResponse>> refresh() {
        DeferredResult<ResponseEntity<InstrumentRefreshResponse>> result = new DeferredResult<>();
        if (!isRefreshCalled.get()) {
            isRefreshCalled.set(true);
            logger.info("Initiated instrument refresh");
            ForkJoinPool.commonPool().submit(() -> instrumentRefresh(result, isRefreshCalled));
        } else {
            logger.info("Instrument refresh is already scheduled");
            result.setResult(ResponseEntity.ok(new InstrumentRefreshResponse(new ArrayList<>(), "Instrument Refresh is already scheduled")));
        }
        return result;
    }

    private void instrumentRefresh(DeferredResult<ResponseEntity<InstrumentRefreshResponse>> pResult, AtomicBoolean isRefreshCalled) {
        status.reset();
        List<InstrumentJPA> newInstruments = this.dbUtils.refreshInstruments(status);

        if(!newInstruments.isEmpty()) {
            logger.warn(() -> String.format("Got %s new instruments! ", newInstruments.size()));
        } else {
            pResult.setResult(ResponseEntity.ok(new InstrumentRefreshResponse(newInstruments, "Done, no new instruments found")));
        }

        isRefreshCalled.set(false);
    }
}
