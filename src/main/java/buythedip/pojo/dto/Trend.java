package buythedip.pojo.dto;


import buythedip.pojo.jpa.CandlesJPA;

public class Trend {
    CandlesJPA firstCandle;
    CandlesJPA lastCandle;
    int numberOfCandles;
    int dipPercentage;
    int restorePercentage;
    double startPx;
    double endPx;
    String interval;

    public Trend(CandlesJPA firstTrendCand, CandlesJPA lastTrendCand, int pNum, String pInt, int dipPerc, int restorePerc) {
        firstCandle = firstTrendCand;
        lastCandle = lastTrendCand;
        numberOfCandles = pNum;
        interval = pInt;
        startPx = firstTrendCand.getClosePrice().doubleValue();
        endPx = lastTrendCand.getClosePrice().doubleValue();
        dipPercentage =  dipPerc;
        restorePercentage = restorePerc;
    }

    public CandlesJPA getFirstCandle() {
        return firstCandle;
    }

    @SuppressWarnings("unused")
    public CandlesJPA getLastCandle() {
        return lastCandle;
    }

    @SuppressWarnings("unused")
    public int getNumberOfCandles() {
        return numberOfCandles;
    }

    public int getDipPercentage() {
        return dipPercentage;
    }

    public int getRestorePercentage() {
        return restorePercentage;
    }

    @SuppressWarnings("unused")
    public String getInterval() {
        return interval;
    }

    @SuppressWarnings("unused")
    public double getStartPx() { return startPx; }

    @SuppressWarnings("unused")
    public double getEndPx() { return endPx; }
}
