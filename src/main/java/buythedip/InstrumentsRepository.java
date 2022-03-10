package buythedip;

import buythedip.entities.InstrumentJPA;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentsRepository extends CrudRepository<InstrumentJPA, Long> {
    InstrumentJPA findById(long id);
    InstrumentJPA findByTicker(String ticker);
    InstrumentJPA findByFigi(String figi);
    long count();
}
