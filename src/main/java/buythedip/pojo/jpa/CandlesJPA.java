package buythedip.pojo.jpa;


import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import javax.persistence.*;
import java.math.BigDecimal;

import static ru.tinkoff.piapi.core.utils.DateUtils.timestampToString;
import static ru.tinkoff.piapi.core.utils.MapperUtils.quotationToBigDecimal;

@Entity
@Table(
    indexes = {
        @Index(columnList = "figi", name = "time")
    })
public class CandlesJPA {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @SuppressWarnings("unused")
    private Integer candleid;

    @SuppressWarnings("unused")
    public Integer getCandleid() {
        return candleid;
    }

    public String getFigi() {
        return figi;
    }

    public String getInterval() {
        return interval;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    @SuppressWarnings("unused")
    public BigDecimal getHighestPrice() {
        return highestPrice;
    }

    @SuppressWarnings("unused")
    public BigDecimal getLowestPrice() {
        return lowestPrice;
    }

    @SuppressWarnings("unused")
    public Long getValue() {
        return value;
    }

    public String getTime() {
        return time;
    }

    private String figi;
    @Column(name="`interval`")
    private String interval;
    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private BigDecimal highestPrice;
    private BigDecimal lowestPrice;
    @Column(name="`value`")
    private Long value;
    @Column(name="`time`")
    private String time;

    @SuppressWarnings("unused")
    CandlesJPA() {}

    public CandlesJPA(HistoricCandle historicCandle, String figi, String interval) {
        this.figi = figi;
        this.interval = interval;
        openPrice = quotationToBigDecimal(historicCandle.getOpen());
        closePrice = quotationToBigDecimal(historicCandle.getClose());
        highestPrice = quotationToBigDecimal(historicCandle.getHigh());
        lowestPrice = quotationToBigDecimal(historicCandle.getLow());
        value = historicCandle.getVolume();
        time = timestampToString(historicCandle.getTime());
    }
}
