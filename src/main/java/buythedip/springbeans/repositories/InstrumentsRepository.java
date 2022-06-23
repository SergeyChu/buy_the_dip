package buythedip.springbeans.repositories;

import buythedip.pojo.dto.GroupedStatistics;
import buythedip.pojo.jpa.InstrumentsJPA;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstrumentsRepository extends CrudRepository<InstrumentsJPA, Long> {
    @SuppressWarnings("unused")
    InstrumentsJPA findById(long id);
    @SuppressWarnings("unused")
    InstrumentsJPA findByTicker(String ticker);
    @SuppressWarnings("unused")
    InstrumentsJPA findByFigi(String figi);
    long count();

    @Query(value = "select table_rows from information_schema.tables\n" +
            "where table_name = 'instrumentsjpa'", nativeQuery = true)
    long estimatedCount();

    @Query(value = "ANALYZE TABLE instrumentsjpa", nativeQuery = true)
    String analyzeTable();

    @Query("SELECT new buythedip.pojo.dto.GroupedStatistics(adddate, count(*)) FROM InstrumentsJPA " +
            "GROUP BY adddate " +
            "ORDER BY adddate DESC")
    List<GroupedStatistics> getInstrumentsFreshness();
}
