package buythedip.refreshers;

import buythedip.LoggerSingleton;
import buythedip.entities.InstrumentJPA;
import buythedip.entities.InstrumentRefreshResponse;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;


public class InstrumentRefresher {
    private String mCurrentStatus;
    private long mCurrentProgress;
    private final AtomicBoolean mIsRefreshCalled = new AtomicBoolean(false);
    private static volatile InstrumentRefresher mInstrumentRefresher;
    private final Logger mLg = LoggerSingleton.getInstance();

    public static InstrumentRefresher getInstance() {
        if (mInstrumentRefresher == null) {
            synchronized (InstrumentRefresher.class) {
                if (mInstrumentRefresher == null) {
                    mInstrumentRefresher = new InstrumentRefresher();
                }
            }
        }
        return mInstrumentRefresher;
    }

    public String getCurrentStatus() {
        return new JSONObject()
                .put("status", mCurrentStatus)
                .put("progress", mCurrentProgress)
                .toString();
    }

    public DeferredResult<ResponseEntity<InstrumentRefreshResponse>> refresh() {
        DeferredResult<ResponseEntity<InstrumentRefreshResponse>> tResult = new DeferredResult<>();
        if (!mIsRefreshCalled.get()) {
            mIsRefreshCalled.set(true);
            mLg.info("Initiated instrument refresh");
            ForkJoinPool.commonPool().submit(() -> instrumentRefresh(tResult));
        } else {
            mLg.info("Instrument refresh is already scheduled");
            tResult.setResult(ResponseEntity.ok(new InstrumentRefreshResponse(new ArrayList<>(), "Instrument Refresh is already scheduled")));
        }
        return tResult;
    }

    private void instrumentRefresh(DeferredResult<ResponseEntity<InstrumentRefreshResponse>> pResult) {
        try {
            mCurrentProgress = 1;
            mCurrentStatus = "Refresh is started";
            Thread.sleep(2000);
            mCurrentStatus = "First step is done";
            mCurrentProgress = 20;
            Thread.sleep(2000);
            mCurrentStatus = "Second step is done";
            mCurrentProgress = 40;
            Thread.sleep(2000);
            mCurrentStatus = "Third step is done";
            mCurrentProgress = 60;
            Thread.sleep(2000);
            mCurrentStatus = "Forth step is done";
            mCurrentProgress = 80;
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            pResult.setErrorResult(new RuntimeException("Error upon refreshing instrument: ") + e.getMessage());
        }
        mCurrentProgress = 100;
        mCurrentStatus = "All done!";
        List<InstrumentJPA> tResultInstrs = new ArrayList<>();
        tResultInstrs.add(new InstrumentJPA("QDEL","FUCK_THE_DUCK", "HHHHCCC", "USD", "Quidel Corp"));
        pResult.setResult(ResponseEntity.ok(new InstrumentRefreshResponse(tResultInstrs, "Done")));
        mIsRefreshCalled.set(false);
    }
}
