package com.example.stockproject.service;

import com.example.stockproject.controller.request.Stock;
import com.example.stockproject.controller.request.StockRequest;
import com.example.stockproject.controller.request.UpdateRequest;
import com.example.stockproject.controller.response.Update;
import com.example.stockproject.model.MstmbInfoRepo;
import com.example.stockproject.model.entity.MstmbInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UpdateService {
    @Autowired
    MstmbInfoRepo mstmbInfoRepo;

    //方法的指定參數作為鍵，返回值作為緩存中的值
    @Cacheable(cacheNames = "stockInfo_cache", key = "#stock.getStock()")
    public Update cachingStock(Stock stock) {
        //check
        if (null == mstmbInfoRepo.findByStock(stock.getStock()))
            return new Update(null, "Stock data wrong");
        MstmbInfo mstmbInfo = mstmbInfoRepo.findByStock(stock.getStock());
        return new Update(mstmbInfo, "");
    }

    //同時更新匹配的條目
    @CachePut(cacheNames = "stockInfo_cache", key = "#updateRequest.getStock()")
    public Update updatePrice(UpdateRequest updateRequest) {
        MstmbInfo mstmbInfo = mstmbInfoRepo.findByStock(updateRequest.getStock());
        mstmbInfo.setCurPrice(updateRequest.getPrice());
        mstmbInfoRepo.save(mstmbInfo);
        return new Update(mstmbInfo, "");
    }

}
