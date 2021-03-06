package buythedip.springbeans;

import buythedip.pojo.dto.RefreshStatus;
import buythedip.pojo.jpa.CandlesFreshnessJPA;
import buythedip.pojo.jpa.CandlesJPA;
import buythedip.pojo.jpa.InstrumentsJPA;
import buythedip.springbeans.repositories.CandlesFreshnessRepository;
import buythedip.springbeans.repositories.CandlesRepository;
import buythedip.springbeans.repositories.InstrumentsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.Share;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static ru.tinkoff.piapi.core.utils.DateUtils.timestampToString;


@Service
public class DBService {

    @Autowired
    @SuppressWarnings("unused")
    private InstrumentsRepository instrumentRepository;

    @Autowired
    @SuppressWarnings("unused")
    private CandlesRepository candlesRepository;

    @Autowired
    @SuppressWarnings("unused")
    private CandlesFreshnessRepository candlesFreshnessRepository;

    private Iterable<InstrumentsJPA> internalInstrumentsList;
    private final Logger logger = LogManager.getLogger(DBService.class);
    private static final int CANDLES_LOOKBACK_DAYS = 360;

    private buythedip.springbeans.InvestApi investApi;

    @Autowired
    @SuppressWarnings("unused")
    void setInvestApi(buythedip.springbeans.InvestApi investApi) {
        this.investApi = investApi;
    }

    //Retreives all the available instruments from API, updates internal database and returns new ones if appears
    public List<InstrumentsJPA> refreshInstruments(RefreshStatus status) {

        status.setStatus("Getting list of stocks from API");

        List<Share> externalInstrumentsList = investApi.getApi().getInstrumentsService().getTradableShares().join();
        status.setProgress(80);

        status.setStatus("Getting list of stocks from local store and evaluating new ones");
        internalInstrumentsList = instrumentRepository.findAll();
        List<InstrumentsJPA> finalList = externalInstrumentsList.stream()
                .map(this::checkInstrumentExists).filter(Objects::nonNull).collect(Collectors.toList());
        status.setProgress(90);

        if (!finalList.isEmpty()) {
            logger.debug(() -> String.format("Total new stocks found: %s", finalList));
            status.setStatus("Total new stocks found: " + finalList + " saving into repository");
            status.setProgress(95);
            for (InstrumentsJPA ins : finalList) {
                instrumentRepository.save(ins);
            }
            instrumentRepository.analyzeTable();
        }

        status.setProgress(100);
        status.setStatus("Done with refresh of instruments");
        return finalList;
    }

    public void refreshCandles(RefreshStatus status) {
        AtomicLong totalInstruments = new AtomicLong(instrumentRepository.count());
        AtomicLong updatedInstruments = new AtomicLong(0);
        status.setStatus("Refresh of candles is initiated");

        instrumentRepository.findAll().forEach(instrumentJPA -> {
            List<HistoricCandle> tempCandles;

            status.setStatus(String.format("Retreiving candles for %s %d/%d", instrumentJPA.getTicker(),
                    updatedInstruments.longValue(), totalInstruments.longValue()));
            tempCandles = new ArrayList<>(getCandles(instrumentJPA.getFigi(), CandleInterval.CANDLE_INTERVAL_DAY,
                    Instant.now().minus(CANDLES_LOOKBACK_DAYS, ChronoUnit.DAYS)));
            tempCandles.sort(Comparator.comparing(candle -> timestampToString(candle.getTime())));

            if (storeCandles(tempCandles, instrumentJPA.getFigi(), CandleInterval.CANDLE_INTERVAL_DAY.name())) {
                logger.debug(() -> String.format("Saved candle data into DB for %s", instrumentJPA.getTicker()));
                candlesRepository.analyzeTable();
            }

            status.setStatus(String.format("Refreshing candles %d done from %d", updatedInstruments.longValue(),
                    totalInstruments.longValue()));
            updatedInstruments.incrementAndGet();
            long currentProgress = updatedInstruments.longValue() / (totalInstruments.longValue() / 100);
            status.setProgress((int) currentProgress);
        });

        status.setStatus("Refresh of candles is completed");
        status.setProgress(100);
    }

    @SuppressWarnings("unused")
    public List<HistoricCandle> getCustomCandles(String figi, Instant dateFrom, Instant dateTo, CandleInterval candleInterval) {
        List<HistoricCandle> candles = new ArrayList<>();
        try {
            candles = investApi.getApi()
                    .getMarketDataService().getCandles(figi, dateFrom, dateTo, candleInterval).join();
        }
        catch (Exception e) {
            logger.error(e);
        }

        return candles;
    }

    private boolean compareInstrs(Share instrument, InstrumentsJPA instrumentJPA) {
        return Objects.equals(instrument.getTicker(), instrumentJPA.getTicker()) &&
                Objects.equals(instrument.getIsin(), instrumentJPA.getIsin()) &&
                Objects.equals(instrument.getFigi(), instrumentJPA.getFigi());
    }

    private InstrumentsJPA checkInstrumentExists(Share instrument) {
        if (StreamSupport.stream(internalInstrumentsList.spliterator(), false)
                .anyMatch(insJPA -> compareInstrs(instrument, insJPA))) {
            return null;
        } else {
            return new InstrumentsJPA(instrument);
        }
    }

    private List<HistoricCandle> getCandles(String figi, CandleInterval candleInterval, Instant fromDate) {
        Instant dateTo = Instant.now();
        logger.info(() -> String.format("Getting candles for %s", figi));
        return investApi.getApi().getMarketDataService().getCandles(figi, fromDate, dateTo, candleInterval).join();
    }

    private boolean storeCandles(List<HistoricCandle> candlesToStore, String figi, String interval) {
        if (candlesToStore == null || candlesToStore.isEmpty()) return false;
        List<CandlesJPA> newCandles = candlesToStore.stream().map(historicCandle -> mapToCandleJPA(historicCandle, figi, interval)).filter(candleFromApi ->
                candlesRepository.findByfigi(figi).stream()
                        .filter(candleFromStore -> compareCandles(candleFromStore, candleFromApi)).noneMatch(obj -> true)
        ).collect(Collectors.toList());
        logger.info(() -> String.format("Total new candles found to store: %s", newCandles.size()));
        try {
            if (!newCandles.isEmpty()) {
                newCandles.forEach(candlesRepository::save);
                HistoricCandle lastCandle = candlesToStore.get(candlesToStore.size() - 1);
                String lastCandleTime = timestampToString(lastCandle.getTime());
                upsertCandlesFreshness(new CandlesFreshnessJPA(figi, lastCandleTime));
                return true;
            } else return false;
        } catch (IllegalArgumentException e) {
            logger.info(String.format("Error upon storage of candles data: %s", e.getMessage()));
            e.printStackTrace();
            return false;
        }
    }

    private boolean compareCandles(CandlesJPA oldCandle, CandlesJPA newCandle) {
        return (Objects.equals(oldCandle.getFigi(), newCandle.getFigi()) &&
                Objects.equals(oldCandle.getInterval(), newCandle.getInterval()) &&
                Objects.equals(oldCandle.getTime(), newCandle.getTime())
                );
    }

    private CandlesJPA mapToCandleJPA(HistoricCandle historicCandle, String figi, String interval) {
        return new CandlesJPA(historicCandle, figi, interval);
    }

    public void updateCandlesFreshness() {
        candlesRepository.getCandlesFreshnessFromMainTable().forEach(this::upsertCandlesFreshness);
    }

    private void upsertCandlesFreshness(CandlesFreshnessJPA candlesFreshness) {
        CandlesFreshnessJPA storedCandle = candlesFreshnessRepository.findFirstByFigi(candlesFreshness.getFigi());
        if (storedCandle != null) {
            if (storedCandle.getFigi().compareTo(candlesFreshness.getFigi()) > 0)
                storedCandle.setTime(candlesFreshness.getTime());
        } else {
            candlesFreshnessRepository.save(candlesFreshness);
        }
    }
}
