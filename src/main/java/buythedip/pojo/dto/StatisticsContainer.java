package buythedip.pojo.dto;

import java.util.List;

public class StatisticsContainer {

    public void setTotalInstruments(long totalInstruments) {
        this.totalInstruments = totalInstruments;
    }

    public void setTotalCandles(long totalCandles) {
        this.totalCandles = totalCandles;
    }

    public void setCandlesFreshness(List<GroupedStatistics> candlesFreshness) {
        this.candlesFreshness = candlesFreshness;
    }

    // Not used getters are required to serialize successfully
    @SuppressWarnings("unused")
    public long getTotalInstruments() {
        return totalInstruments;
    }
    @SuppressWarnings("unused")
    public long getTotalCandles() {
        return totalCandles;
    }
    @SuppressWarnings("unused")
    public List<GroupedStatistics> getCandlesFreshness() {
        return candlesFreshness;
    }
    @SuppressWarnings("unused")
    public List<GroupedStatistics> getInstrumentsFreshness() {
        return instrumentsFreshness;
    }

    public void setInstrumentsFreshness(List<GroupedStatistics> instrumentsFreshness) {
        this.instrumentsFreshness = instrumentsFreshness;
    }

    @SuppressWarnings("unused")
    private long totalInstruments;
    @SuppressWarnings("unused")
    private long totalCandles;
    @SuppressWarnings("unused")
    private List<GroupedStatistics> candlesFreshness;
    @SuppressWarnings("unused")
    private List<GroupedStatistics> instrumentsFreshness;

    public StatisticsContainer(long pTotalInstruments, long pTotalCandles, List<GroupedStatistics> candlesFreshness,
                               List<GroupedStatistics> instrumentsFreshness) {
        totalInstruments = pTotalInstruments;
        totalCandles = pTotalCandles;
        this.candlesFreshness = candlesFreshness;
        this.instrumentsFreshness = instrumentsFreshness;
    }
}
