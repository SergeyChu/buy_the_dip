package buythedip.entities;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
public class InstrumentJPA {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer stockid;
    private String ticker;
    private String figi;
    private String isin;
    private String currency;
    private String name;
    private String adddate;

    public InstrumentJPA() {}

    public InstrumentJPA(Instrument pInst) {
        ticker = pInst.ticker;
        figi = pInst.figi;
        isin = pInst.isin;
        currency = pInst.currency == null ? "" : pInst.currency.toString();
        name = pInst.name;
        adddate = LocalDate.now().toString();
    }

    public InstrumentJPA(String pTicker, String pFigi, String pIsin, String pCurrency, String pName) {
        ticker = pTicker;
        figi = pFigi;
        isin = pIsin;
        currency = pCurrency;
        name = pName;
        adddate = LocalDate.now().toString();
    }

    public Integer getStockid() {
        return stockid;
    }

    public String getTicker() {
        return ticker;
    }

    public String getFigi() {
        return figi;
    }

    public String getIsin() {
        return isin;
    }

    public String getCurrency() {
        return currency;
    }

    public String getName() {
        return name;
    }

    public String getAddDate() {
        return adddate;
    }
}
