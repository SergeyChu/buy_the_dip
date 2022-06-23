package buythedip.pojo.dto;

import java.util.List;

public class RefreshResponse<T> {
    @SuppressWarnings("unused")
    public List<T> getNewEntities() {
        return newEntities;
    }

    @SuppressWarnings("unused")
    public void setNewEntities(List<T> newEntities) {
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

    private List<T> newEntities;
    private String statusText;

    public RefreshResponse(List<T> pNewEntities, String pStatusText) {
        newEntities = pNewEntities;
        statusText = pStatusText;
    }
}
