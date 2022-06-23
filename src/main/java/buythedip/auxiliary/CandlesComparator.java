package buythedip.auxiliary;

import buythedip.pojo.jpa.CandlesJPA;

import java.util.Comparator;

public class CandlesComparator implements Comparator<CandlesJPA> {

    @Override
    public int compare(CandlesJPA pC1, CandlesJPA pC2) {
        return pC1.getTime().compareTo(pC2.getTime());
    }
}
