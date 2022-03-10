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
    private InstrumentsRepository instrumentRepository;
    @Autowired
    @SuppressWarnings("unused")
    private CandlesRepository candlesRepository;
    @Autowired
    @SuppressWarnings("unused")
    private InstrumentRefresher instrumentRefresher;

    private final Logger logger = LoggerSingleton.getInstance();

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
        return instrumentRefresher.refresh();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/api/refresh/instruments/updatestatus")
    @SuppressWarnings("unused")
    String getInstrumentUpdateStatus() {
        return instrumentRefresher.getCurrentStatus();
    }

    @RequestMapping("/")
    @SuppressWarnings("unused")
    public String index() {
        return "I'm working!";
    }

    private List<CandlesJPA> getDailyDip(Integer pPercentageThresholdDip, Integer pPercThresholdRestore) {

        Iterable<InstrumentJPA> tInternalInstrumentsList = instrumentRepository.findAll();
        List<CandlesJPA> tResult = new LinkedList<>();

        if ( StreamSupport.stream(tInternalInstrumentsList.spliterator(), false).count() == 0) {
            logger.warn("Empty instruments list, exiting");
            return tResult;
        }

        for (InstrumentJPA tInst : tInternalInstrumentsList) {
            logger.trace(() -> String.format("Checking md for instrument: %s" , tInst.getTicker()));
            if (Objects.equals(tInst.getCurrency(), "RUB")) continue;
            String lastDipMessage = null;
            CandlesJPA tLastCandle = null;
            CandlesJPA tLastDipCandle = null;
            for (CandlesJPA tCand : candlesRepository.findBycFigi(tInst.getFigi())) {
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
                    logger.info(lastDipMessage);
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
        return instrumentRepository.count();
    }

    private long getTotalCandles() {
        return candlesRepository.count();
    }

    private Map<String, Long> getCandlesByTime() {
        List<Statistics> tStatList = candlesRepository.findStats();
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
