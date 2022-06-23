package buythedip.springbeans;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import buythedip.auxiliary.LoggerSingleton;
import buythedip.pojo.dto.GroupedStatistics;
import buythedip.pojo.dto.InstrumentRefreshResponse;
import buythedip.pojo.dto.RefreshResponse;
import buythedip.pojo.dto.StatisticsContainer;
import buythedip.pojo.jpa.CandlesFreshnessJPA;
import buythedip.pojo.jpa.CandlesJPA;
import buythedip.pojo.jpa.InstrumentsJPA;
import buythedip.springbeans.repositories.InstrumentsRepository;
import buythedip.springbeans.refreshers.CandleRefresher;
import buythedip.springbeans.refreshers.InstrumentRefresher;
import buythedip.springbeans.repositories.CandlesRepository;
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
    @Autowired
    @SuppressWarnings("unused")
    private CandleRefresher candleRefresher;

    private final StatisticsContainer statistics =
            new StatisticsContainer(0, 0, null, null);

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
        this.statistics.setTotalCandles(getTotalCandles(true));
        this.statistics.setTotalInstruments(getTotalInstruments(true));
        this.statistics.setCandlesFreshness(getCandlesFreshness(true));
        this.statistics.setInstrumentsFreshness(getInstrumentsFreshness());
        return this.statistics;
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

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value = "/api/refresh/candles")
    @SuppressWarnings("unused")
    DeferredResult<ResponseEntity<RefreshResponse<CandlesJPA>>> updateCandles() {
        return candleRefresher.refresh();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/api/refresh/candles/updatestatus")
    @SuppressWarnings("unused")
    String getCandleUpdateStatus() {
        return candleRefresher.getCurrentStatus();
    }

    @RequestMapping("/")
    @SuppressWarnings("unused")
    public String index() {
        return "I'm working!";
    }

    private List<CandlesJPA> getDailyDip(Integer pPercentageThresholdDip, Integer pPercThresholdRestore) {

        Iterable<InstrumentsJPA> tInternalInstrumentsList = instrumentRepository.findAll();
        List<CandlesJPA> tResult = new LinkedList<>();

        if ( StreamSupport.stream(tInternalInstrumentsList.spliterator(), false).count() == 0) {
            logger.warn("Empty instruments list, exiting");
            return tResult;
        }

        for (InstrumentsJPA tInst : tInternalInstrumentsList) {
            logger.trace(() -> String.format("Checking md for instrument: %s" , tInst.getTicker()));
            if (Objects.equals(tInst.getCurrency(), "RUB")) continue;
            String lastDipMessage = null;
            CandlesJPA tLastCandle = null;
            CandlesJPA tLastDipCandle = null;
            for (CandlesJPA tCand : candlesRepository.findByfigi(tInst.getFigi())) {
                double tOpPx = tCand.getOpenPrice().doubleValue();
                double tClPx = tCand.getClosePrice().doubleValue();
                if (tOpPx * pPercentageThresholdDip/100 <=  tOpPx - tClPx) {
                    lastDipMessage = "Got the dip exceeding " + pPercentageThresholdDip + " % " + tCand.getTime() + " " + tInst.getTicker() + " " + tCand.getOpenPrice() + " " + tCand.getClosePrice();
                    tLastDipCandle = tCand;
                }
                if (tLastCandle == null) tLastCandle = tCand;
                if (tLastCandle.getTime().compareTo(tCand.getTime()) < 0) {
                    tLastCandle = tCand;
                }
            }
            if (lastDipMessage != null) {
                if (tLastDipCandle.getClosePrice().doubleValue() * (100 + pPercThresholdRestore)/100 > tLastCandle.getClosePrice().doubleValue()) {
                    logger.info(lastDipMessage);
                    tResult.add(tLastDipCandle);
                }

            }
        }
        return tResult;
    }

    private long getTotalInstruments(boolean useEstimatedCount) {
        long start = System.currentTimeMillis();
        long result = useEstimatedCount ? instrumentRepository.estimatedCount() : instrumentRepository.count();
        logger.info(() -> String.format("Done with getting instruments count in %d ms", (System.currentTimeMillis() - start)));
        return result;
    }

    private long getTotalCandles(boolean useEstimatedCount) {
        long start = System.currentTimeMillis();
        long result = useEstimatedCount ? candlesRepository.estimatedCount() : candlesRepository.count();
        logger.info(() -> String.format("Done with getting candles count in %d ms", (System.currentTimeMillis() - start)));
        return result;
    }

    private List<GroupedStatistics> getCandlesFreshness(boolean useSummaryTable) {
        long start = System.currentTimeMillis();
        List<CandlesFreshnessJPA> candlesPerInstrument =
                useSummaryTable ? candlesRepository.getCandlesFreshnessFromSummaryTable()
                        : candlesRepository.getCandlesFreshnessFromMainTable();
        final Map<String, Long> groupedByDateResult = new HashMap<>();
        candlesPerInstrument
                .forEach(s -> groupedByDateResult.merge(s.getTime(), 1L, Long::sum));
        List<GroupedStatistics> result = groupedByDateResult.entrySet().stream()
                .map(entry -> new GroupedStatistics(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        logger.info(() -> String.format("Done with getting candles freshness in %d ms", (System.currentTimeMillis() - start)));
        return result;
    }

    private List<GroupedStatistics> getInstrumentsFreshness() {
        long start = System.currentTimeMillis();
        List<GroupedStatistics> result = instrumentRepository.getInstrumentsFreshness();
        logger.info(() -> String.format("Done with getting instrument freshness in %d ms", (System.currentTimeMillis() - start)));
        return result;
    }
}
