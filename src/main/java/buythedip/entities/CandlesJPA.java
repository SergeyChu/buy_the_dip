package buythedip.entities;

import ru.tinkoff.invest.openapi.models.market.Candle;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

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

    public BigDecimal getcValue() {
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
    private BigDecimal cValue;
    private String cTime;

    CandlesJPA() {}

    public CandlesJPA(Candle pCan) {
        cFigi = pCan.figi;
        cInterval = pCan.interval.name();
        cOpenPrice = pCan.openPrice;
        cClosePrice = pCan.closePrice;
        cHighestPrice = pCan.highestPrice;
        cLowestPrice = pCan.lowestPrice;
        cValue = pCan.tradesValue;
        cTime = pCan.time.format(DateTimeFormatter.BASIC_ISO_DATE);
    }
}
