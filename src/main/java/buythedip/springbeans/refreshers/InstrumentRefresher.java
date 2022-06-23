package buythedip.springbeans.refreshers;

import buythedip.pojo.dto.InstrumentRefreshResponse;
import buythedip.pojo.dto.RefreshStatus;
import buythedip.springbeans.WebRequestsForkJoinPool;
import buythedip.springbeans.DBService;
import buythedip.pojo.jpa.InstrumentsJPA;
import buythedip.auxiliary.RequestExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class InstrumentRefresher {
    private final RefreshStatus status = new RefreshStatus();
    private final AtomicBoolean isRefreshCalled = new AtomicBoolean(false);
    private final Logger logger = LogManager.getLogger(InstrumentRefresher.class);

    @Autowired
    protected DBService dbService;

    private final WebRequestsForkJoinPool webRequestsThreadPool;

    @Autowired
    public InstrumentRefresher(WebRequestsForkJoinPool webRequestsThreadPool) {
        this.webRequestsThreadPool = webRequestsThreadPool;
    }

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
            webRequestsThreadPool.getForkJoinPool().execute(() -> instrumentRefresh(result, isRefreshCalled));
        } else {
            logger.info("Instrument refresh is already scheduled");
            result.setResult(ResponseEntity.ok(new InstrumentRefreshResponse(new ArrayList<>(), "Instrument Refresh is already scheduled")));
        }
        return result;
    }

    private void instrumentRefresh(DeferredResult<ResponseEntity<InstrumentRefreshResponse>> result, AtomicBoolean isRefreshCalled) {
        status.reset();
        try {
            logger.info("Initiated instrument refresh");
            List<InstrumentsJPA> newInstruments = dbService.refreshInstruments(status);
            if (!newInstruments.isEmpty()) {
                logger.info(() -> String.format("Got %s new instruments! ", newInstruments.size()));
                result.setResult(ResponseEntity.ok(new InstrumentRefreshResponse(newInstruments, "New instruments found")));
            }
            logger.info("Completed instruments refresh");
            isRefreshCalled.set(false);
        } catch (CompletionException completionException) {
            String message = String.format("Got error during instruments refresh: %s ", completionException.getMessage());
            logger.error(message);
            status.setProgress(0);
            status.setStatus(message);
            result.setResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new InstrumentRefreshResponse(null, message)));
            isRefreshCalled.set(false);
            throw new RequestExecutionException(status.getStatus());
        }
    }
}
