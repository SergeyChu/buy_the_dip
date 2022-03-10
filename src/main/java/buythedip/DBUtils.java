package buythedip;

import buythedip.entities.CandlesJPA;
import buythedip.entities.InstrumentJPA;
import buythedip.refreshers.InstrumentRefreshStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.Logger;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InvestApi;


@Component
public class DBUtils {

    @Autowired
    @SuppressWarnings("unused")
    private InstrumentsRepository instrumentRepository;

    @Autowired
    @SuppressWarnings("unused")
    private CandlesRepository candlesRepository;
    private Iterable<InstrumentJPA> internalInstrumentsList;
    private final Logger logger = LoggerSingleton.getInstance();

    //Retreives all the available instruments from API, updates internal database and returns new ones if appears
    public List<InstrumentJPA> refreshInstruments(InstrumentRefreshStatus status) {
        InvestApi api = ApiSingleton.getInstance();
        status.setStatus("Getting list of stocks from API");
        List<Share> externalInstrumentsList = api.getInstrumentsService().getTradableShares().join();
        status.setProgress(80);

        status.setStatus("Getting list of stocks from local store and evaluating new ones");
        internalInstrumentsList = instrumentRepository.findAll();
        List<InstrumentJPA> finalList = externalInstrumentsList.stream()
                .map(this::checkInstrumentExists).filter(Objects::nonNull).collect(Collectors.toList());
        status.setProgress(90);

        if (!finalList.isEmpty()) {
            logger.debug(() -> String.format("Total new stocks found: %s", finalList));
            status.setStatus("Total new stocks found: " + finalList + " saving into repository");
            status.setProgress(95);
            for (InstrumentJPA ins : finalList) {
                instrumentRepository.save(ins);
            }
        }

        status.setProgress(100);
        status.setStatus("Done with refresh of instruments");
        return finalList;
    }

    @SuppressWarnings("unused")
    public void getDailyCandles(List<InstrumentJPA> instruments) throws InterruptedException {
        logger.info(() -> String.format("Getting daily candles for %d instruments", instruments.size()));
        AtomicInteger currentInd = new AtomicInteger(0);
        for (InstrumentJPA instr : instruments) {
            boolean infoObtained = false;
            List<HistoricCandle> tempCandles = new ArrayList<>();
            do {
                try {
                    tempCandles = getCandles(instr.getFigi(), CandleInterval.CANDLE_INTERVAL_DAY, Instant.now().minus(1, ChronoUnit.DAYS));
                    infoObtained = true;
                    currentInd.incrementAndGet();
                }
                catch (Exception e) {
                    logger.warn("Breached the limit, waiting 1 minute and retrying");
                    TimeUnit.MINUTES.sleep(1);
                }
            } while (!infoObtained);
            logger.info(() -> String.format("Getting daily candles done for %d from %d instruments", currentInd.intValue(), instruments.size()));

            if (storeCandles(tempCandles, instr.getFigi(), CandleInterval.CANDLE_INTERVAL_DAY.name()))
                logger.trace(() -> String.format("Saved candle data into DB for %s", instr.getTicker()));
        }
    }

    @SuppressWarnings("unused")
    public static List<HistoricCandle> getCustomCandles(String figi, Instant dateFrom, Instant dateTo, CandleInterval candleInterval) {
        List<HistoricCandle> candles = new ArrayList<>();
        try {
            candles = ApiSingleton.getInstance()
                    .getMarketDataService().getCandles(figi, dateFrom, dateTo, candleInterval).join();
        }
        catch (Exception e) {
            System.err.println("Got exception");
            e.printStackTrace();
        }

        return candles;
    }

    private boolean compareInstrs(Share instrument, InstrumentJPA instrumentJPA) {
        return Objects.equals(instrument.getTicker(), instrumentJPA.getTicker()) &&
                Objects.equals(instrument.getIsin(), instrumentJPA.getIsin()) &&
                Objects.equals(instrument.getFigi(), instrumentJPA.getFigi());
    }

    private InstrumentJPA checkInstrumentExists(Share instrument) {
        if (StreamSupport.stream(internalInstrumentsList.spliterator(), false)
                .anyMatch(insJPA -> compareInstrs(instrument, insJPA))) {
            return null;
        } else {
            return new InstrumentJPA(instrument);
        }
    }

    private List<HistoricCandle> getCandles(String figi, CandleInterval candleInterval, Instant fromDate) {
        Instant dateTo = Instant.now();
        List<HistoricCandle> candles = new ArrayList<>();
        logger.info(() -> String.format("Getting candles for %s", figi));
        try {
            return ApiSingleton.getInstance().getMarketDataService().getCandles(figi, fromDate, dateTo, candleInterval).join();
        }
        catch (Exception e) {
            logger.error("Got exception");
            e.printStackTrace();
        }
        return candles;
    }

    private boolean storeCandles(List<HistoricCandle> candlesToStore, String figi, String interval) {
        if (candlesToStore == null || candlesToStore.isEmpty()) return false;
        List<CandlesJPA> newCandles = candlesToStore.stream().map(historicCandle -> mapToCandleJPA(historicCandle, figi, interval)).filter(candleFromApi ->
                candlesRepository.findBycFigi(figi).stream()
                        .filter(candleFromStore -> compareCandles(candleFromStore, candleFromApi)).noneMatch(obj -> true)
        ).collect(Collectors.toList());
        logger.info(() -> String.format("Total new candles found to store: %s", newCandles.size()));
        try {
            newCandles.forEach(candlesRepository::save);
            return true;
        } catch (IllegalArgumentException e) {
            logger.info(String.format("Error upon storage of candles data: %s", e.getMessage()));
            e.printStackTrace();
            return false;
        }
    }

    private boolean compareCandles(CandlesJPA oldCandle, CandlesJPA newCandle) {
        return (Objects.equals(oldCandle.getcFigi(), newCandle.getcFigi()) &&
                Objects.equals(oldCandle.getcInterval(), newCandle.getcInterval()) &&
                Objects.equals(oldCandle.getcTime(), newCandle.getcTime())
                );
    }

    private CandlesJPA mapToCandleJPA(HistoricCandle historicCandle, String figi, String interval) {
        return new CandlesJPA(historicCandle, figi, interval);
    }
}
