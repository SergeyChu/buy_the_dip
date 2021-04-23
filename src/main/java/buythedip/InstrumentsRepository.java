package buythedip;

import buythedip.entities.InstrumentJPA;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentsRepository extends CrudRepository<InstrumentJPA, Long> {
    InstrumentJPA findById(long pId);
    InstrumentJPA findByTicker(String pTicker);
    InstrumentJPA findByFigi(String pTicker);
    long count();
}
