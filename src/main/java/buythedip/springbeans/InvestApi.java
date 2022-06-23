package buythedip.springbeans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class InvestApi {
    private final ru.tinkoff.piapi.core.InvestApi api;

    @Autowired
    public InvestApi(Environment env) {
        String token = env.getProperty("tinkoff.api.token");
        api = ru.tinkoff.piapi.core.InvestApi.create(token);
    }

    public ru.tinkoff.piapi.core.InvestApi getApi() {
        return api;
    }
}
