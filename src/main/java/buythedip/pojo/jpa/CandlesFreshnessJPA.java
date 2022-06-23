package buythedip.pojo.jpa;

import javax.persistence.*;


@Entity
@Table(
    indexes = {
        @Index(columnList = "figi", name = "time")
    })
public class CandlesFreshnessJPA {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @SuppressWarnings("unused")
    private Integer candlefreshnessid;

    @SuppressWarnings("unused")
    public Integer getCandleFreshnessid() {
        return candlefreshnessid;
    }

    public String getFigi() {
        return figi;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String figi;
    @Column(name="`time`")
    private String time;

    @SuppressWarnings("unused")
    CandlesFreshnessJPA() {}

    public CandlesFreshnessJPA(String figi, String time) {
        this.figi = figi;
        this.time = time;
    }
}
