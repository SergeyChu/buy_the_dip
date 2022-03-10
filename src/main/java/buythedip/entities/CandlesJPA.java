package buythedip.entities;


import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import javax.persistence.*;
import java.math.BigDecimal;

import static ru.tinkoff.piapi.core.utils.DateUtils.timestampToString;
import static ru.tinkoff.piapi.core.utils.MapperUtils.quotationToBigDecimal;

@Entity
@Table(
    indexes = {
        @Index(columnList = "cfigi", name = "cfigi")
    })
public class CandlesJPA {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer candleid;

    public Integer getCandleid() {
        return candleid;
    }

    public String getcFigi() {
        return cFigi;
    }

    public String getcInterval() {
        return cInterval;
    }

    public BigDecimal getcOpenPrice() {
        return cOpenPrice;
    }

    public BigDecimal getcClosePrice() {
        return cClosePrice;
    }

    public BigDecimal getcHighestPrice() {
        return cHighestPrice;
    }

    public BigDecimal getcLowestPrice() {
        return cLowestPrice;
    }

    public Long getcValue() {
        return cValue;
    }

    public String getcTime() {
        return cTime;
    }

    private String cFigi;
    private String cInterval;
    private BigDecimal cOpenPrice;
    private BigDecimal cClosePrice;
    private BigDecimal cHighestPrice;
    private BigDecimal cLowestPrice;
    private Long cValue;
    private String cTime;

    CandlesJPA() {}

    public CandlesJPA(HistoricCandle historicCandle, String figi, String interval) {
        cFigi = figi;
        cInterval = interval;
        cOpenPrice = quotationToBigDecimal(historicCandle.getOpen());
        cClosePrice = quotationToBigDecimal(historicCandle.getClose());
        cHighestPrice = quotationToBigDecimal(historicCandle.getHigh());
        cLowestPrice = quotationToBigDecimal(historicCandle.getLow());
        cValue = historicCandle.getVolume();
        cTime = timestampToString(historicCandle.getTime());
    }
}
