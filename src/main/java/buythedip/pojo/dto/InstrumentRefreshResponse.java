package buythedip.pojo.dto;

import buythedip.pojo.jpa.InstrumentsJPA;

import java.util.List;

public class InstrumentRefreshResponse {
    @SuppressWarnings("unused")
    public List<InstrumentsJPA> getNewEntities() {
        return newEntities;
    }

    @SuppressWarnings("unused")
    public void setNewEntities(List<InstrumentsJPA> newEntities) {
        this.newEntities = newEntities;
    }

    @SuppressWarnings("unused")
    public String getStatusText() {
        return statusText;
    }

    @SuppressWarnings("unused")
    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    private List<InstrumentsJPA> newEntities;
    private String statusText;

    public InstrumentRefreshResponse(List<InstrumentsJPA> pNewEntities, String pStatusText) {
        newEntities = pNewEntities;
        statusText = pStatusText;
    }
}
