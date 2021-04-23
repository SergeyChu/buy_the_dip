package buythedip;

import buythedip.entities.CandlesJPA;
import buythedip.entities.Statistics;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandlesRepository extends CrudRepository<CandlesJPA, Long> {
    List<CandlesJPA> findBycFigi(String pFigi);
    long count();
    @Query("SELECT new buythedip.entities.Statistics(cTime, cFigi) FROM CandlesJPA " +
            "GROUP BY cFigi " +
            "ORDER BY cTime DESC")
    List<Statistics> findStats();
}
