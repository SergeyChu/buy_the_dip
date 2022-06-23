package buythedip.pojo.jpa;
import ru.tinkoff.piapi.contract.v1.Share;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
        indexes = {
                @Index(columnList = "figi", name = "adddate")
        })
public class InstrumentsJPA {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @SuppressWarnings("unused")
    private Integer stockid;
    private String ticker;
    private String figi;
    private String isin;
    private String currency;
    private String name;
    private String adddate;

    @SuppressWarnings("unused")
    public InstrumentsJPA() {}

    public InstrumentsJPA(Share pInst) {
        ticker = pInst.getTicker();
        figi = pInst.getFigi();
        isin = pInst.getIsin();
        currency = pInst.getCurrency();
        name = pInst.getName();
        adddate = LocalDate.now().toString();
    }

    public InstrumentsJPA(String pTicker, String pFigi, String pIsin, String pCurrency, String pName) {
        ticker = pTicker;
        figi = pFigi;
        isin = pIsin;
        currency = pCurrency;
        name = pName;
        adddate = LocalDate.now().toString();
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    public String getAddDate() {
        return adddate;
    }
}
