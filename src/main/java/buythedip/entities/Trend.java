package buythedip.entities;


public class Trend {
    CandlesJPA mFirstCandle;
    CandlesJPA mLastCandle;
    int mNumberOfCandles;
    int mDipPercentage;
    int mRestorePercentage;
    String mInterval;

    public Trend(CandlesJPA firstTrendCand, CandlesJPA lastTrendCand, int pNum, String pInt, int dipPerc, int restorePerc) {
        mFirstCandle = firstTrendCand;
        mLastCandle = lastTrendCand;
        mNumberOfCandles = pNum;
        mInterval = pInt;
        double tStartPx = firstTrendCand.getcClosePrice().doubleValue();
        double tEndPx = lastTrendCand.getcClosePrice().doubleValue();
        mDipPercentage =  dipPerc;
        mRestorePercentage = restorePerc;
    }

    public CandlesJPA getmFirstCandle() {
        return mFirstCandle;
    }

    public CandlesJPA getmLastCandle() {
        return mLastCandle;
    }

    public int getmNumberOfCandles() {
        return mNumberOfCandles;
    }

    public int getmDipPercentage() {
        return mDipPercentage;
    }

    public int getmRestorePercentage() {
        return mRestorePercentage;
    }

    public String getmInterval() {
        return mInterval;
    }
}
