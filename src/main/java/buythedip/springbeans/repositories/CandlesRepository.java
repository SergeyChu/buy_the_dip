package buythedip.springbeans.repositories;

import buythedip.pojo.jpa.CandlesFreshnessJPA;
import buythedip.pojo.jpa.CandlesJPA;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandlesRepository extends CrudRepository<CandlesJPA, Long> {
    List<CandlesJPA> findByfigi(String figi);
    long count();

    @Query(value = "select table_rows from information_schema.tables\n" +
            "where table_name = 'candlesjpa'", nativeQuery = true)
    long estimatedCount();

    @Query(value = "ANALYZE TABLE candlesjpa", nativeQuery = true)
    String analyzeTable();

    @Query("SELECT new buythedip.pojo.jpa.CandlesFreshnessJPA(figi, time) FROM CandlesJPA " +
            "GROUP BY figi " +
            "ORDER BY time DESC")
    List<CandlesFreshnessJPA> getCandlesFreshnessFromMainTable();

    @Query("SELECT new buythedip.pojo.jpa.CandlesFreshnessJPA(figi, time) FROM CandlesFreshnessJPA ")
    List<CandlesFreshnessJPA> getCandlesFreshnessFromSummaryTable();

}
