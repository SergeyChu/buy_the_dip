package buythedip;

import buythedip.entities.CandlesJPA;

import java.util.Comparator;

public class CandlesComparator implements Comparator<CandlesJPA> {

    @Override
    public int compare(CandlesJPA pC1, CandlesJPA pC2) {
        return pC1.getcTime().compareTo(pC2.getcTime());
    }
}
