package buythedip;

import buythedip.springbeans.DBService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.ApplicationContext;


@SpringBootApplication
public class Starter {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Starter.class, args);
        DBService dbService = ctx.getBean(DBService.class);
        dbService.updateCandlesFreshness();

  }
}
