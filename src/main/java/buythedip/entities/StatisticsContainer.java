package buythedip.entities;

import java.util.Map;

public class StatisticsContainer {
    public long getTotalInstruments() {
        return totalInstruments;
    }

    public void setTotalInstruments(long totalInstruments) {
        this.totalInstruments = totalInstruments;
    }

    public long getTotalCandles() {
        return totalCandles;
    }

    public void setTotalCandles(long totalCandles) {
        this.totalCandles = totalCandles;
    }

    public Map<String, Long> getCandlesFreshness() {
        return candlesFreshness;
    }

    public void setCandlesFreshness(Map<String, Long> candlesFreshness) {
        this.candlesFreshness = candlesFreshness;
    }

    private long totalInstruments;
    private long totalCandles;
    private Map<String, Long> candlesFreshness;

    public StatisticsContainer(long pTotalInstruments, long pTotalCandles, Map<String, Long> pCandlesFreshness) {
        totalInstruments = pTotalInstruments;
        totalCandles = pTotalCandles;
        candlesFreshness = pCandlesFreshness;
    }
}
