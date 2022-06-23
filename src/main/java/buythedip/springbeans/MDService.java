package buythedip.springbeans;

import buythedip.auxiliary.CandlesComparator;
import buythedip.auxiliary.LoggerSingleton;
import buythedip.pojo.jpa.CandlesJPA;
import buythedip.pojo.jpa.InstrumentsJPA;
import buythedip.pojo.dto.Trend;
import buythedip.springbeans.repositories.CandlesRepository;
import buythedip.springbeans.repositories.InstrumentsRepository;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.StreamSupport;

@Service
public class MDService {
    @Autowired
    @SuppressWarnings("unused")
    private InstrumentsRepository instrumentsRepository;

    @Autowired
    @SuppressWarnings("unused")
    private CandlesRepository candlesRepository;

    private Iterable<InstrumentsJPA> internalInstrumentsList;
    private final Logger logger = LoggerSingleton.getInstance();
    private final Map<String, String> figiTicker = new HashMap<>();

    @SuppressWarnings("unused")
    public List<CandlesJPA> getDailyDip(Integer pPercentageThresholdDip, Integer pPercThresholdRestore) {

        internalInstrumentsList = instrumentsRepository.findAll();
        List<CandlesJPA> tResult = new LinkedList<>();

        if ( StreamSupport.stream(internalInstrumentsList.spliterator(), false).count() == 0) {
            logger.warn("Empty instruments list, exiting");
            return tResult;
        }

        for (InstrumentsJPA tInst : internalInstrumentsList) {
            logger.trace("Checking md for instrument: " + tInst.getTicker());
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

    @SuppressWarnings("unused")
    public List<Trend> getTrendDip(Integer pPercentageThresholdDip, Integer pPercThresholdRestore,
                            Integer pDaysToBreakTrend, Integer pMinTrendDays) {

        internalInstrumentsList = instrumentsRepository.findAll();
        List<Trend> tTrends = new LinkedList<Trend>();

        long instrumentsSize = StreamSupport.stream(internalInstrumentsList.spliterator(), false).count();
        if (instrumentsSize == 0) {
            logger.warn("Empty instruments list, exiting");
            return tTrends;
        }

        int currInst = 1;
        for (InstrumentsJPA tInst : internalInstrumentsList) {
            logger.info(String.format("Checking md for instrument: %s %d/%d", tInst.getTicker(), currInst, instrumentsSize));
            currInst++;
            if (Objects.equals(tInst.getCurrency(), "RUB")) continue;

            List<CandlesJPA> tCandles = candlesRepository.findByfigi(tInst.getFigi());
            tCandles.sort(new CandlesComparator());

            int tIndexTrendBreach = 0;
            int tTrendDuration = 0;
            int tDaysTrendBreached = 0;
            CandlesJPA tFirstCandleInTrend = null;
            CandlesJPA tLastCandleInTrend = null;
            for (int i = 0; i < tCandles.size(); i++) {
                CandlesJPA currentCandle = tCandles.get(i);
                if (isPriceFalls(tLastCandleInTrend, currentCandle)) {
                    // Means that we are starting a new trend
                   if (tFirstCandleInTrend == null) {
                       tFirstCandleInTrend = currentCandle;
                       tLastCandleInTrend = currentCandle;
                       tTrendDuration = 1;
                   } else {
                       tLastCandleInTrend = currentCandle;
                       tTrendDuration += 1;
                   }
                    tIndexTrendBreach = 0;
                    tDaysTrendBreached = 0;
                } else {
                    if (tIndexTrendBreach == 0) {
                        tIndexTrendBreach = i;
                    }
                    tDaysTrendBreached++;
                    int currDipPerc = getPercentageDip(tFirstCandleInTrend, tLastCandleInTrend);
                    int currRestorePerc = getPercentageDip(tFirstCandleInTrend, tCandles.get(tCandles.size() - 1));
                    //If trend is broken for more than specified days, considering it as a beginning of a new trend
                    if (tDaysTrendBreached >= pDaysToBreakTrend) {
                        if (tTrendDuration >= pMinTrendDays) {
                            if (currDipPerc >= pPercentageThresholdDip && currRestorePerc >= pPercThresholdRestore) {
                                tTrends.add(new Trend(tFirstCandleInTrend, tLastCandleInTrend,
                                        tTrendDuration, tFirstCandleInTrend.getInterval(), currDipPerc, currRestorePerc));
                            }
                        }
                        tFirstCandleInTrend = null;
                        tLastCandleInTrend = null;
                        i = tIndexTrendBreach - 1;
                        tIndexTrendBreach = 0;
                        tTrendDuration = 0;
                        tDaysTrendBreached = 0;
                    }
                }
            }
            if (!tCandles.isEmpty()) {
                int currDipPerc = getPercentageDip(tFirstCandleInTrend, tLastCandleInTrend);
                int currRestorePerc = getPercentageDip(tFirstCandleInTrend, tCandles.get(tCandles.size() - 1));
                if (tFirstCandleInTrend != null && currDipPerc >= pPercentageThresholdDip && currRestorePerc > pPercThresholdRestore) {
                    if (tTrendDuration >= pMinTrendDays) {
                        tTrends.add(new Trend(tFirstCandleInTrend, tLastCandleInTrend,
                                tTrendDuration, tFirstCandleInTrend.getInterval(), currDipPerc, currRestorePerc));
                    }
                }
            }
        }

        return tTrends;
    }

    private int getPercentageDip(CandlesJPA firstCandle, CandlesJPA secondCandle) {
        if (secondCandle == null || firstCandle == null) return 0;
        double firstPx = firstCandle.getClosePrice().doubleValue();
        double secondPx = secondCandle.getClosePrice().doubleValue();
        // Return difference in percents between closing prices
        return (int) Math.round((firstPx - secondPx)/firstPx * 100);
    }

    private boolean isPriceFalls(CandlesJPA firstCandle, CandlesJPA secondCandle) {
        //If first candle is not initalized assuming that the candle starts trend
        if (firstCandle == null) return true;
        if (secondCandle == null) {
            logger.error("Current candle can't be null");
            return false;
        }
        return firstCandle.getClosePrice().doubleValue() > secondCandle.getClosePrice().doubleValue();
    }

    @SuppressWarnings("unused")
    public void printTrends(List<Trend> trends) {
        for (InstrumentsJPA tInst : internalInstrumentsList) {
            figiTicker.put(tInst.getFigi(), tInst.getTicker());
        }
        for (Trend tmpTrend : trends) {
            logger.info(resolveTicker(tmpTrend.getFirstCandle().getFigi()) + " " + tmpTrend.getDipPercentage() + " " + tmpTrend.getRestorePercentage());
        }
    }

    private String resolveTicker(String figi) {
        return figiTicker.getOrDefault(figi, figi);
    }
}
