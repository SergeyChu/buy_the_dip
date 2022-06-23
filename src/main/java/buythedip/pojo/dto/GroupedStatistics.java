package buythedip.pojo.dto;


public class GroupedStatistics {
    private final String date;
    private final Long count;

    public GroupedStatistics(String date, Long count) {
        this.date = date;
        this.count = count;
    }

    @SuppressWarnings("unused")
    public String getDate() {
        return this.date;
    }

    @SuppressWarnings("unused")
    public Long getCount() {
        return this.count;
    }
}
