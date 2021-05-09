package buythedip;

import buythedip.entities.CandlesJPA;
import buythedip.entities.InstrumentJPA;
import buythedip.entities.Trend;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.StreamSupport;

@Component
public class MDUtils {
    @Autowired
    @SuppressWarnings("unused")
    private InstrumentsRepository mInstrumentRepository;
    @Autowired
    @SuppressWarnings("unused")
    private CandlesRepository mCandlesRepository;
    private Iterable<InstrumentJPA> tInternalInstrumentsList;
    private final Logger mLg = LoggerSingleton.getInstance();
    private final Map<String, String> figiTicker = new HashMap<>();

    @SuppressWarnings("unused")
    public List<CandlesJPA> getDailyDip(Integer pPercentageThresholdDip, Integer pPercThresholdRestore) {

        tInternalInstrumentsList = mInstrumentRepository.findAll();
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

    @SuppressWarnings("unused")
    public List<Trend> getTrendDip(Integer pPercentageThresholdDip, Integer pPercThresholdRestore,
                            Integer pDaysToBreakTrend, Integer pMinTrendDays) {

        tInternalInstrumentsList = mInstrumentRepository.findAll();
        List<Trend> tTrends = new LinkedList<Trend>();

        long instrumentsSize = StreamSupport.stream(tInternalInstrumentsList.spliterator(), false).count();
        if (instrumentsSize == 0) {
            mLg.warn("Empty instruments list, exiting");
            return tTrends;
        }

        int currInst = 1;
        for (InstrumentJPA tInst : tInternalInstrumentsList) {
            mLg.info("Checking md for instrument: " + tInst.getTicker() + " " + currInst + "/" + instrumentsSize);
            currInst++;
            if (Objects.equals(tInst.getCurrency(), "RUB")) continue;

            List<CandlesJPA> tCandles = mCandlesRepository.findBycFigi(tInst.getFigi());
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
                                        tTrendDuration, tFirstCandleInTrend.getcInterval(), currDipPerc, currRestorePerc));
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
                                tTrendDuration, tFirstCandleInTrend.getcInterval(), currDipPerc, currRestorePerc));
                    }
                }
            }
        }

        return tTrends;
    }

    private int getPercentageDip(CandlesJPA firstCandle, CandlesJPA secondCandle) {
        if (secondCandle == null || firstCandle == null) return 0;
        double firstPx = firstCandle.getcClosePrice().doubleValue();
        double secondPx = secondCandle.getcClosePrice().doubleValue();
        // Return difference in percents between closing prices
        return (int) Math.round((firstPx - secondPx)/firstPx * 100);
    }

    private boolean isPriceFalls(CandlesJPA firstCandle, CandlesJPA secondCandle) {
        //If first candle is not initalized assuming that the candle starts trend
        if (firstCandle == null) return true;
        if (secondCandle == null) {
            mLg.error("Current candle can't be null");
            return false;
        }
        return firstCandle.getcClosePrice().doubleValue() > secondCandle.getcClosePrice().doubleValue();
    }

    @SuppressWarnings("unused")
    public void printTrends(List<Trend> trends) {
        for (InstrumentJPA tInst : tInternalInstrumentsList) {
            figiTicker.put(tInst.getFigi(), tInst.getTicker());
        }
        for (Trend tmpTrend : trends) {
            mLg.info(resolveTicker(tmpTrend.getmFirstCandle().getcFigi()) + " " + tmpTrend.getmDipPercentage() + " " + tmpTrend.getmRestorePercentage());
        }
    }

    private String resolveTicker(String figi) {
        return figiTicker.getOrDefault(figi, figi);
    }
}
