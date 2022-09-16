package com.example.stockproject.controller;

import com.example.stockproject.controller.request.NoProfitRequest;
import com.example.stockproject.controller.request.StockRequest;
import com.example.stockproject.controller.response.HcmioDetailResponse;
import com.example.stockproject.controller.response.SumProfit;
import com.example.stockproject.model.HcmioDetailRepo;
import com.example.stockproject.model.MstmbInfoRepo;
import com.example.stockproject.model.TcnudRepo;
import com.example.stockproject.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/unreal")
public class StockController {

    @Autowired
    StockService stockService;
    @Autowired
    HcmioDetailRepo hcmioDetailRepo;
    @Autowired
    MstmbInfoRepo mstmbInfoRepo;
    @Autowired
    TcnudRepo tcnudRepo;

    //交易--------------------------------------------------------------------------------------------------------
    @PostMapping("/add")
    public HcmioDetailResponse transaction(@RequestBody StockRequest stockRequest) {
        return stockService.transaction(stockRequest);
    }

    //查詢彙總未實現損益------------------------------------------------------------------------------------------------
    @PostMapping("/sum")
    public SumProfit sumUnrealizedGainsAndLosses(@RequestBody NoProfitRequest noProfitRequest) {
        return stockService.sumUnrealizedGainsAndLosses(noProfitRequest);
    }

    //查詢個別未實現損益------------------------------------------------------------------------------------------------
    @PostMapping("/detail")
    public HcmioDetailResponse unrealizedGainsAndLosses(@RequestBody NoProfitRequest noProfitRequest) {
        return stockService.unrealProfit(noProfitRequest);
    }


}
