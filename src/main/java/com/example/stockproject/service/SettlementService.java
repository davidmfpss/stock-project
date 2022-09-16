package com.example.stockproject.service;

import com.example.stockproject.controller.request.Settlement;
import com.example.stockproject.controller.response.SettlementResponse;
import com.example.stockproject.model.HcmioDetailRepo;
import com.example.stockproject.model.TcnudRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Service
public class SettlementService {
    @Autowired
    HcmioDetailRepo hcmioDetailRepo;
    @Autowired
    TcnudRepo tcnudRepo;
    public SettlementResponse today(Settlement todayPay) {
        Calendar theDay = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        int count = 0;
        while (count < 2) {
            theDay.add(Calendar.DATE, -1);
            if (theDay.get(Calendar.DAY_OF_WEEK) != 1 && theDay.get(Calendar.DAY_OF_WEEK) != 7) {
                count++;
            }
        }
        if (null == theDay) {
            return new SettlementResponse("Today's payment is 0", 0l);
        }
        return new SettlementResponse("", tcnudRepo.findTodayBalance(todayPay.getBranchNo(), todayPay.getCustSeq(), sdf.format(theDay.getTime())));
    }
}
