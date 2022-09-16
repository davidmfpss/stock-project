package com.example.stockproject.controller;

import com.example.stockproject.controller.request.Settlement;
import com.example.stockproject.controller.request.Stock;
import com.example.stockproject.controller.request.StockRequest;
import com.example.stockproject.controller.request.UpdateRequest;
import com.example.stockproject.controller.response.SettlementResponse;
import com.example.stockproject.controller.response.Update;
import com.example.stockproject.model.HcmioDetailRepo;
import com.example.stockproject.model.MstmbInfoRepo;
import com.example.stockproject.service.SettlementService;
import com.example.stockproject.service.StockService;
import com.example.stockproject.service.UpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/unreal")
public class MstmbController {
    @Autowired
    StockService stockService;
    @Autowired
    HcmioDetailRepo hcmioDetailRepo;
    @Autowired
    MstmbInfoRepo mstmbInfoRepo;
    @Autowired
    UpdateService updateService;
    @Autowired
    SettlementService settlementService;

    //caching stockInfo---------------------------------------------------------------------------------------------
    @PostMapping("/info")
    public Update cachingStock(@RequestBody Stock stock) {
        return updateService.cachingStock(stock);
    }

    @PostMapping("/settlement")
    public SettlementResponse todayPay(@RequestBody Settlement todayPay) {
        return settlementService.today(todayPay);
    }

    @PostMapping("/update")
    public Update updatePrice(@RequestBody UpdateRequest updateRequest) {
        return updateService.updatePrice(updateRequest);
    }
}
