package buythedip;

import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApiFactory;

import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ApiSingleton {
    private static volatile OpenApi mApi;
    //Crappy logger which is required by Tikoff API
    private static final Logger mLg = Logger.getLogger(ApiSingleton.class.getName());
    private static final String mToken = "PUT_YOUR_TOKEN";
    public static OpenApi getInstance() {
        if (mApi == null) {
            synchronized (ApiSingleton.class) {
                if (mApi == null) {
                    OkHttpOpenApiFactory factory = new OkHttpOpenApiFactory(mToken, mLg);
                    mApi = factory.createOpenApiClient(Executors.newSingleThreadExecutor());
                }
            }
        }
        return mApi;
    }
}
