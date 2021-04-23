package buythedip.entities;

import java.util.List;

public class InstrumentRefreshResponse {
    private List<InstrumentJPA> mNewEntities;
    private String mStatusText;

    public InstrumentRefreshResponse(List<InstrumentJPA> pNewEntities, String pStatusText) {
        mNewEntities = pNewEntities;
        mStatusText = pStatusText;
    }
}
