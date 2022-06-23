package buythedip.springbeans.repositories;

import buythedip.pojo.jpa.CandlesFreshnessJPA;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CandlesFreshnessRepository extends CrudRepository<CandlesFreshnessJPA, Long> {
    CandlesFreshnessJPA findFirstByFigi(String figi);

}
