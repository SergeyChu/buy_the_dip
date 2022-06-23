package buythedip.springbeans.refreshers;

import buythedip.pojo.dto.RefreshStatus;
import buythedip.springbeans.APIRequestsForkJoinPool;
import buythedip.springbeans.DBService;
import buythedip.auxiliary.LoggerSingleton;
import buythedip.pojo.jpa.CandlesJPA;
import buythedip.pojo.dto.RefreshResponse;
import buythedip.springbeans.RequestExecutionException;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class CandleRefresher {
    private final RefreshStatus status = new RefreshStatus();
    private final AtomicBoolean isRefreshCalled = new AtomicBoolean(false);
    private final Logger logger = LoggerSingleton.getInstance();

    @Autowired
    protected DBService dbService;

    private final APIRequestsForkJoinPool apiThreadPool;

    @Autowired
    public CandleRefresher(APIRequestsForkJoinPool apiThreadPool) {
        this.apiThreadPool = apiThreadPool;
    }

    public String getCurrentStatus() {
        return new JSONObject()
                .put("status", status.getStatus())
                .put("progress", status.getProgress())
                .toString();
    }

    public DeferredResult<ResponseEntity<RefreshResponse<CandlesJPA>>> refresh() {
        DeferredResult<ResponseEntity<RefreshResponse<CandlesJPA>>> result = new DeferredResult<>();
        if (!isRefreshCalled.get()) {
            isRefreshCalled.set(true);
            logger.info("Initiated candles refresh");
            apiThreadPool.getForkJoinPool().execute(() -> candleRefresh(result, isRefreshCalled));
        } else {
            logger.info("Candles refresh is already scheduled");
            result.setResult(ResponseEntity.ok(new RefreshResponse<>(new ArrayList<>(),
                    "Candles Refresh is already scheduled")));
        }
        return result;
    }

    private void candleRefresh(DeferredResult<ResponseEntity<RefreshResponse<CandlesJPA>>> result, AtomicBoolean isRefreshCalled) {
        status.reset();
        try {
            this.dbService.refreshCandles(status);
            result.setResult(ResponseEntity.ok(new RefreshResponse<>(null, status.getStatus())));
            logger.info("Completed candles refresh");
            isRefreshCalled.set(false);
        } catch (CompletionException completionException) {
            String message = String.format("Got error during instruments refresh: %s ", completionException.getMessage());
            logger.error(message);
            status.setProgress(0);
            status.setStatus(message);
            result.setResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RefreshResponse<>(null, message)));
            isRefreshCalled.set(false);
            throw new RequestExecutionException(status.getStatus());
        }
    }
}
