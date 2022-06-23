package buythedip.auxiliary;

import ru.tinkoff.piapi.core.InvestApi;


public class ApiSingleton {
    private static volatile InvestApi mApi;
    private static final String TOKEN = "PUT_YOUR_TOKEN";
    public static InvestApi getInstance() {
        if (mApi == null) {
            synchronized (ApiSingleton.class) {
                if (mApi == null) {
                    mApi = InvestApi.create(TOKEN);
                }
            }
        }
        return mApi;
    }
}
