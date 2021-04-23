package buythedip;

import buythedip.entities.CandlesJPA;
import buythedip.entities.InstrumentJPA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.models.market.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.Logger;


@Component
public class DBUtils {

    @Autowired
    private InstrumentsRepository mInstrumentRepository;
    @Autowired
    private CandlesRepository mCandlesRepository;
    private Iterable<InstrumentJPA> tInternalInstrumentsList;
    private final Logger mLg = LoggerSingleton.getInstance();

    //Retreives all the available instruments from API, updates internal database and returns new ones if appears
    public List<InstrumentJPA> refreshInstruments() {
        OpenApi tApi = ApiSingleton.getInstance();
        mLg.debug("Getting list of stocks from API");
        InstrumentsList tExternalInstrumentsList = tApi.getMarketContext().getMarketStocks().join();
        mLg.debug("Getting list of stocks from local store");
        tInternalInstrumentsList = mInstrumentRepository.findAll();
        List<InstrumentJPA> tFinalList = tExternalInstrumentsList.instruments.stream()
                .map(this::checkInstrumentExists).filter(Objects::nonNull).collect(Collectors.toList());
        mLg.debug("Total new stocks found: " + tFinalList);
        for (InstrumentJPA tIns : tFinalList) {
            mInstrumentRepository.save(tIns);
        }
        return tFinalList;
    }

    public void getDailyCandles(List<InstrumentJPA> pInstruments) throws InterruptedException {
        mLg.info("Getting daily candles for " + pInstruments.size() + " instruments");
        int tCurrentInd = 0;
        for (InstrumentJPA tInstr : pInstruments) {
            boolean tInfoObtained = false;
            List<Candle> tTempCandles = null;
            do {
                try {
                    tTempCandles = getDailyCandle(tInstr.getFigi());
                    tInfoObtained = true;
                    tCurrentInd++;
                }
                catch (Exception e) {
                    mLg.warn("Breached the limit, waiting 1 minute and retrying");
                    TimeUnit.MINUTES.sleep(1);
                }
            } while (!tInfoObtained);
            mLg.info("Getting daily candles done for: " + tCurrentInd + "/" + pInstruments.size());
            if (storeCandles(tTempCandles)) mLg.trace("Saved candle data into DB for " + tInstr.getTicker());
        }
    }

    public static List<Candle> getCustomCandles(String pFigi, OffsetDateTime pDateFrom, OffsetDateTime pDateTo, CandleInterval pCandleInt) {
        Optional<HistoricalCandles> tCandles = Optional.empty();
        try {
            tCandles = ApiSingleton.getInstance()
                    .getMarketContext().getMarketCandles(pFigi, pDateFrom, pDateTo, pCandleInt).join();}
        catch (Exception e) {
            System.out.println("Got exception");
            e.printStackTrace();
        }

        return tCandles.map(historicalCandles -> historicalCandles.candles).orElse(null);
    }

    private boolean compareInstrs(Instrument pInst, InstrumentJPA pInsJPA) {
        return Objects.equals(pInst.ticker, pInsJPA.getTicker()) &&
                Objects.equals(pInst.isin, pInsJPA.getIsin()) &&
                Objects.equals(pInst.figi, pInsJPA.getFigi());
    }

    private InstrumentJPA checkInstrumentExists(Instrument pInst) {
        if (StreamSupport.stream(tInternalInstrumentsList.spliterator(), false)
                .anyMatch(insJPA -> compareInstrs(pInst, insJPA))) {
            return null;
        } else {
            assert pInst.currency != null;
            return new InstrumentJPA(pInst);
        }
    }

    private List<Candle> getDailyCandle(String pFigi) {
        OffsetDateTime tDateFrom = OffsetDateTime.now().minusYears(1);
        OffsetDateTime tDateTo = OffsetDateTime.now();
        CandleInterval tCandleInt = CandleInterval.DAY;
        Optional<HistoricalCandles> tCandles = Optional.empty();
        mLg.info("Getting candles for " + pFigi);
        try {
        tCandles = ApiSingleton.getInstance()
                        .getMarketContext().getMarketCandles(pFigi,tDateFrom,tDateTo, tCandleInt).join();}
        catch (Exception e) {
            System.out.println("Got exception");
            e.printStackTrace();
        }

        return tCandles.map(historicalCandles -> historicalCandles.candles).orElse(null);
    }

    private boolean storeCandles(List<Candle> pCandlesToStore) {
        if (pCandlesToStore == null || pCandlesToStore.size() == 0) return false;
        List<Candle> tNewCandles = pCandlesToStore.stream().filter( tExtCandle ->
                mCandlesRepository.findBycFigi(tExtCandle.figi).stream()
                        .filter(tIntCandle -> compareCandles(tIntCandle, tExtCandle)).noneMatch(obj -> true)
        ).collect(Collectors.toList());
        mLg.info("Total new candles found to store: " + tNewCandles.size());
        try {
            for (Candle tCan : tNewCandles) {
                mCandlesRepository.save(new CandlesJPA(tCan));
            }
            return true;
        } catch (IllegalArgumentException e) {
            mLg.info("Error upon storage of candles data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean compareCandles(CandlesJPA pJpa, Candle pCan) {
        return (Objects.equals(pJpa.getcFigi(), pCan.figi) &&
                Objects.equals(pJpa.getcInterval(), pCan.interval.name()) &&
                Objects.equals(pJpa.getcTime(), pCan.time.format(DateTimeFormatter.BASIC_ISO_DATE))
                );
    }
}
