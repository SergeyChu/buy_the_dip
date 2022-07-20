package buythedip.springbeans;

import buythedip.pojo.dto.InstrumentRefreshResponse;
import buythedip.pojo.dto.RefreshResponse;
import buythedip.pojo.jpa.CandlesJPA;
import buythedip.springbeans.refreshers.CandleRefresher;
import buythedip.springbeans.refreshers.InstrumentRefresher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("refresh")
@SuppressWarnings("unused")
public class RefreshController {
    @Autowired
    @SuppressWarnings("unused")
    private InstrumentRefresher instrumentRefresher;
    @Autowired
    @SuppressWarnings("unused")
    private CandleRefresher candleRefresher;

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value = "instruments")
    @SuppressWarnings("unused")
    DeferredResult<ResponseEntity<InstrumentRefreshResponse>> updateInstruments() {
        return instrumentRefresher.refresh();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("instruments/updatestatus")
    @SuppressWarnings("unused")
    String getInstrumentUpdateStatus() {
        return instrumentRefresher.getCurrentStatus();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value = "candles")
    @SuppressWarnings("unused")
    DeferredResult<ResponseEntity<RefreshResponse<CandlesJPA>>> updateCandles() {
        return candleRefresher.refresh();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("candles/updatestatus")
    @SuppressWarnings("unused")
    String getCandleUpdateStatus() {
        return candleRefresher.getCurrentStatus();
    }
}
