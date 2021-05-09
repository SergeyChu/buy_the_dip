package buythedip.entities;


public class Statistics {
    private final String c_time;
    private final String c_figi;

    public Statistics(String pTime, String pFigi) {
        c_time = pTime;
        c_figi = pFigi;
    }

    public String getC_time() {
        return c_time;
    }

    @SuppressWarnings("unused")
    public String getC_figi() {
        return c_figi;
    }
}
