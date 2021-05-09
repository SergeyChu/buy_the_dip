package buythedip;

import java.util.*;
import java.util.stream.StreamSupport;

import buythedip.entities.*;
import buythedip.refreshers.InstrumentRefresher;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;


@RestController
@SuppressWarnings("unused")
class MainController {
    @Autowired
    @SuppressWarnings("unused")
    private InstrumentsRepository mInstrumentRepository;
    @Autowired
    @SuppressWarnings("unused")
    private CandlesRepository mCandlesRepository;
    private final Logger mLg = LoggerSingleton.getInstance();

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/api/dailydip")
    @SuppressWarnings("unused")
    List<CandlesJPA> getCandles() {
        return getDailyDip(20, 10);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/api/statistics")
    @SuppressWarnings("unused")
    StatisticsContainer getStat() {
        return getStatistics();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value = "/api/refresh/instruments")
    @SuppressWarnings("unused")
    DeferredResult<ResponseEntity<InstrumentRefreshResponse>> updateInstruments() {
        return InstrumentRefresher.getInstance().refresh();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/api/refresh/instruments/updatestatus")
    @SuppressWarnings("unused")
    String getInstrumentUpdateStatus() {
        return InstrumentRefresher.getInstance().getCurrentStatus();
    }

    @RequestMapping("/")
    @SuppressWarnings("unused")
    public String index() {
        return "I'm working!";
    }

    private List<CandlesJPA> getDailyDip(Integer pPercentageThresholdDip, Integer pPercThresholdRestore) {

        Iterable<InstrumentJPA> tInternalInstrumentsList = mInstrumentRepository.findAll();
        List<CandlesJPA> tResult = new LinkedList<>();

        if ( StreamSupport.stream(tInternalInstrumentsList.spliterator(), false).count() == 0) {
            mLg.warn("Empty instruments list, exiting");
            return tResult;
        }

        for (InstrumentJPA tInst : tInternalInstrumentsList) {
            mLg.trace("Checking md for instrument: " + tInst.getTicker());
            if (Objects.equals(tInst.getCurrency(), "RUB")) continue;
            String lastDipMessage = null;
            CandlesJPA tLastCandle = null;
            CandlesJPA tLastDipCandle = null;
            for (CandlesJPA tCand : mCandlesRepository.findBycFigi(tInst.getFigi())) {
                double tOpPx = tCand.getcOpenPrice().doubleValue();
                double tClPx = tCand.getcClosePrice().doubleValue();
                if (tOpPx * pPercentageThresholdDip/100 <=  tOpPx - tClPx) {
                    lastDipMessage = "Got the dip exceeding " + pPercentageThresholdDip + " % " + tCand.getcTime() + " " + tInst.getTicker() + " " + tCand.getcOpenPrice() + " " + tCand.getcClosePrice();
                    tLastDipCandle = tCand;
                }
                if (tLastCandle == null) tLastCandle = tCand;
                if (tLastCandle.getcTime().compareTo(tCand.getcTime()) < 0) {
                    tLastCandle = tCand;
                }
            }
            if (lastDipMessage != null) {
                if (tLastDipCandle.getcClosePrice().doubleValue() * (100 + pPercThresholdRestore)/100 > tLastCandle.getcClosePrice().doubleValue()) {
                    mLg.info(lastDipMessage);
                    tResult.add(tLastDipCandle);
                }

            }
        }
        return tResult;
    }

    private StatisticsContainer getStatistics(){
        return new StatisticsContainer(getTotalInstruments(), getTotalCandles(), getCandlesByTime());
    }

    private long getTotalInstruments() {
        return mInstrumentRepository.count();
    }

    private long getTotalCandles() {
        return mCandlesRepository.count();
    }

    private Map<String, Long> getCandlesByTime() {
        List<Statistics> tStatList = mCandlesRepository.findStats();
        final Map<String, Long> tGroupedResult = new HashMap<>();
        tStatList.forEach(s -> {
            if(tGroupedResult.containsKey(s.getC_time())) {
                long tCurrentCount = tGroupedResult.get(s.getC_time());
                tGroupedResult.put(s.getC_time(), tCurrentCount + 1);
            } else {
                tGroupedResult.put(s.getC_time(), 1L);
            }
        });
        return tGroupedResult;
    }
}
