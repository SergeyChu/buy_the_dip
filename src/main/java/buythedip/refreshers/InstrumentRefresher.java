package buythedip.refreshers;

import buythedip.LoggerSingleton;
import buythedip.entities.InstrumentRefreshResponse;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.concurrent.Callable;
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
            tResult.setResult(ResponseEntity.ok(new InstrumentRefreshResponse(new ArrayList<>(), "Instrument Refresh is done")));
        }
        return tResult;
    }

//    public Callable<String> refreshCallable() {
//        if (!mIsRefreshCalled.get()) {
//            mIsRefreshCalled.set(true);
//            mLg.info("Initiated instrument refresh");
//            return () -> instrumentRefreshCallable(true);
//        } else {
//            mLg.info("Instrument refresh is already scheduled");
//            return () -> instrumentRefreshCallable(false);
//        }
//    }
//
//    private String instrumentRefreshCallable(boolean isNotScheduled) {
//        if(isNotScheduled) {
//            try {
//                mCurrentProgress = 1;
//                mCurrentStatus = "Refresh is started";
//                Thread.sleep(2000);
//                mCurrentStatus = "First step is done";
//                mCurrentProgress = 20;
//                Thread.sleep(2000);
//                mCurrentStatus = "Second step is done";
//                mCurrentProgress = 40;
//                Thread.sleep(2000);
//                mCurrentStatus = "Third step is done";
//                mCurrentProgress = 60;
//                Thread.sleep(2000);
//                mCurrentStatus = "Forth step is done";
//                mCurrentProgress = 80;
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                return "Error upon refreshing instrument: " + e.getMessage();
//            }
//            mCurrentProgress = 100;
//            mCurrentStatus = "All done!";
//            mIsRefreshCalled.set(false);
//            return "Done with instrument refresh 2 new instruments were added";
//        } else {
//            return "Instrument refresh is already scheduled";
//        }
//    }

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
        pResult.setResult(ResponseEntity.ok(new InstrumentRefreshResponse(new ArrayList<>(), "Done")));
        mIsRefreshCalled.set(false);
    }
}
