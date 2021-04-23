package buythedip;

import buythedip.entities.InstrumentJPA;
import buythedip.entities.Trend;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.ApplicationContext;

import java.util.List;


@SpringBootApplication
public class Starter {

    private static final Logger mLg = LoggerSingleton.getInstance();

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = SpringApplication.run(Starter.class, args);
//        DBUtils DbUtils = ctx.getBean(DBUtils.class);
//        List<InstrumentJPA> tNewInstrs = DbUtils.refreshInstruments();
//
//        if(tNewInstrs.size() > 0) {
//            mLg.warn("Got " + tNewInstrs.size() + " new instruments! ");
//        }
//
//        DbUtils.getDailyCandles(tNewInstrs);

//        MDUtils tMdUtil = ctx.getBean(MDUtils.class);
//        List<Trend> tTrends = tMdUtil.getTrendDip(30, 10, 3, 2);
//        mLg.info("Get number of trends: " + tTrends.size());
//        tMdUtil.printTrends(tTrends);
  }
}
