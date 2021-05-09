package buythedip.entities;

import java.util.List;

public class InstrumentRefreshResponse {
    @SuppressWarnings("unused")
    public List<InstrumentJPA> getmNewEntities() {
        return mNewEntities;
    }

    @SuppressWarnings("unused")
    public void setmNewEntities(List<InstrumentJPA> mNewEntities) {
        this.mNewEntities = mNewEntities;
    }

    @SuppressWarnings("unused")
    public String getmStatusText() {
        return mStatusText;
    }

    @SuppressWarnings("unused")
    public void setmStatusText(String mStatusText) {
        this.mStatusText = mStatusText;
    }

    private List<InstrumentJPA> mNewEntities;
    private String mStatusText;

    public InstrumentRefreshResponse(List<InstrumentJPA> pNewEntities, String pStatusText) {
        mNewEntities = pNewEntities;
        mStatusText = pStatusText;
    }
}
