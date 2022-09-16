package com.example.stockproject.service;

import com.example.stockproject.controller.request.StockRequest;
import com.example.stockproject.controller.request.NoProfitRequest;
import com.example.stockproject.controller.response.SumProfit;
import com.example.stockproject.controller.response.HcmioDetailResponse;
import com.example.stockproject.controller.response.NoProfitResult;
import com.example.stockproject.controller.response.Result;
import com.example.stockproject.model.TcnudRepo;
import com.example.stockproject.model.MstmbInfoRepo;
import com.example.stockproject.model.HcmioDetailRepo;
import com.example.stockproject.model.entity.Tcnud;
import com.example.stockproject.model.entity.MstmbInfo;
import com.example.stockproject.model.entity.HcmioDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//交易
@Service
public class StockService {

    @Autowired
    MstmbInfoRepo mstmbInfoRepo;
    @Autowired
    HcmioDetailRepo hcmioDetailRepo;
    @Autowired
    TcnudRepo tcnudRepo;

    //交易--------------------------------------------------------------------------------------------------------
    @Transactional
    public HcmioDetailResponse transaction(StockRequest transactionRequest) {
        //check:request資訊是否正確、股票餘額是否足夠
        if (null != check(transactionRequest)) return new HcmioDetailResponse(null, "002", check(transactionRequest));
        //過程
        //創建明細--------------------------------------------------------------------------------------------------
        getRandomPrice(transactionRequest.getStock());// 讓股票資訊價格隨機更動
        HcmioDetail hcmioDetail = new HcmioDetail();
        List<Result> results = new ArrayList<>();
        try {
            {
                hcmioDetail.setTradeDate(transactionRequest.getTradeDate());//tradeDate
                hcmioDetail.setBranchNo(transactionRequest.getBranchNo());//branchNo
                hcmioDetail.setCustomerSeq(transactionRequest.getCustSeq());//customerSeq
                hcmioDetail.setDocSeq(transactionRequest.getDocSeq());//docSeq
                hcmioDetail.setStock(transactionRequest.getStock());//stock
                hcmioDetail.setBsType("B");//bsType
                hcmioDetail.setPrice(transactionRequest.getPrice());//price
                hcmioDetail.setQty(transactionRequest.getQty());//qty
                hcmioDetail.setAmt(getAmt(hcmioDetail.getPrice(), hcmioDetail.getQty()));//單價*股數=amt
                hcmioDetail.setFee(getFee(hcmioDetail.getAmt()));//fee
                hcmioDetail.setTax(getTax(hcmioDetail.getAmt(), hcmioDetail.getBsType()));//根據bsType決定tax
                hcmioDetail.setTransferTax(0.0);//交易稅目前為0
                hcmioDetail.setNetAmt(getNetAmt(hcmioDetail.getAmt(), hcmioDetail.getFee(), hcmioDetail.getTax(), hcmioDetail.getBsType()));//根據四項數據得到淨收付
                hcmioDetail.setModDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));//modDate
                hcmioDetail.setModTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));//modTime
                hcmioDetail.setModUser("DAVID");//modUser
                hcmioDetailRepo.save(hcmioDetail);//存進sql
            }
            //更新餘額--------------------------------------------------------------------------------------------------
            {
                Tcnud newStockBalance = new Tcnud();
                newStockBalance.setTradeDate(hcmioDetail.getTradeDate());
                newStockBalance.setBranchNo(hcmioDetail.getBranchNo());
                newStockBalance.setCustomerSeq(hcmioDetail.getCustomerSeq());
                newStockBalance.setDocSeq(hcmioDetail.getDocSeq());
                newStockBalance.setStock(hcmioDetail.getStock());
                newStockBalance.setPrice(getBalancePrice(0.0, 0.0, hcmioDetail.getPrice(), hcmioDetail.getQty()));
                newStockBalance.setQty(getBalanceQty(0.0, hcmioDetail.getQty(), hcmioDetail.getBsType()));
                newStockBalance.setRemainQty(getBalanceQty(0.0, hcmioDetail.getQty(), hcmioDetail.getBsType()));
                newStockBalance.setFee(hcmioDetail.getFee());
                newStockBalance.setCost(getBalanceCost(0.0, hcmioDetail.getNetAmt(), hcmioDetail.getBsType()));
                newStockBalance.setModDate(hcmioDetail.getModDate());
                newStockBalance.setModTime(hcmioDetail.getModTime());
                newStockBalance.setModUser(hcmioDetail.getModUser());
                tcnudRepo.save(newStockBalance);
            }

            results.add(new Result(
                    hcmioDetail.getTradeDate(),
                    hcmioDetail.getDocSeq(),
                    hcmioDetail.getStock(),
                    mstmbInfoRepo.findByStock(transactionRequest.getStock()).getStockName(),
                    hcmioDetail.getPrice(),
                    mstmbInfoRepo.findByStock(transactionRequest.getStock()).getCurPrice(),
                    hcmioDetail.getQty(),
                    tcnudRepo.getRemainQty(transactionRequest.getBranchNo(), transactionRequest.getCustSeq(), transactionRequest.getStock()),
                    hcmioDetail.getFee(),
                    Math.abs(hcmioDetail.getNetAmt()),
                    Math.round(mstmbInfoRepo.findByStock(hcmioDetail.getStock()).getCurPrice() * hcmioDetail.getQty()),
                    getUnreal(hcmioDetail.getStock(), Math.abs(hcmioDetail.getNetAmt()), hcmioDetail.getQty())
            ));
        } catch (Exception ex) {
            ex.printStackTrace();
            return new HcmioDetailResponse(
                    results,
                    "005",
                    "伺服器內部錯誤");
        }
        return new HcmioDetailResponse(
                results,
                "000",
                "");
    }


    //查詢彙總未實現損益------------------------------------------------------------------------------------------------
    public SumProfit sumUnrealizedGainsAndLosses(NoProfitRequest unrealProfitRequest) {
        //check
        List<NoProfitResult> noProfitResults = new ArrayList<>();
        try {
            if ("001".equals(check(unrealProfitRequest))) return new SumProfit(null, "001", "查無符合資料");
            if (null != check(unrealProfitRequest)) return new SumProfit(null, "002", check(unrealProfitRequest));
            //過程
            //將Stock放入list
            List<String> stockList;
            //STOCK不必要
            if (unrealProfitRequest.getStock().isBlank()) {
                stockList = tcnudRepo.getAllStock(unrealProfitRequest.getBranchNo(), unrealProfitRequest.getCustSeq());
            } else {
                stockList = new ArrayList<>();
                stockList.add(unrealProfitRequest.getStock());
            }
            for (String stock : stockList) {
                getRandomPrice(stock);
                MstmbInfo stockInfo = mstmbInfoRepo.findByStock(stock);
                NoProfitResult noProfitResult = new NoProfitResult();
                noProfitResult.setDetailList(getResultList(new NoProfitRequest(unrealProfitRequest.getBranchNo(), unrealProfitRequest.getCustSeq(), stock)));
                for (Result result : noProfitResult.getDetailList()) {
                    noProfitResult.setSumRemainQty((null == noProfitResult.getSumRemainQty()) ? result.getRemainQty() : noProfitResult.getSumRemainQty() + result.getQty());
                    noProfitResult.setSumFee((null == noProfitResult.getSumFee()) ? result.getFee() : noProfitResult.getSumFee() + result.getFee());
                    noProfitResult.setSumCost((null == noProfitResult.getSumCost()) ? result.getCost() : noProfitResult.getSumCost() + result.getCost());
                    noProfitResult.setSumUnrealProfit((null == noProfitResult.getSumUnrealProfit()) ? result.getUnrealProfit() : noProfitResult.getSumUnrealProfit() + result.getUnrealProfit());

                }
                noProfitResult.setStock(stock);
                noProfitResult.setStockName(stockInfo.getStockName());
                noProfitResult.setNowPrice(stockInfo.getCurPrice());
                noProfitResult.setSumMarketValue(noProfitResult.getNowPrice() * noProfitResult.getSumRemainQty());
                noProfitResults.add(noProfitResult);
            }
        } catch (Exception ex) {
            return new SumProfit(
                    noProfitResults,
                    "005",
                    "伺服器內部錯誤"
            );
        }
        return new SumProfit(
                noProfitResults,
                "000",
                ""
        );

    }

    //查詢個別未實現損益------------------------------------------------------------------------------------------------
    public HcmioDetailResponse unrealProfit(NoProfitRequest unrealProfitRequest) {
        //check
        HcmioDetailResponse hcmioDetailResponse = new HcmioDetailResponse();
        try {
            if ("001".equals(check(unrealProfitRequest))) return new HcmioDetailResponse(null, "001", "查無符合資料");
            if (null != check(unrealProfitRequest))
                return new HcmioDetailResponse(null, "002", check(unrealProfitRequest));
            //過程


            List<String> stockList;
            //STOCK不必要
            if (unrealProfitRequest.getStock().isBlank()) {
                stockList = tcnudRepo.getAllStock(unrealProfitRequest.getBranchNo(), unrealProfitRequest.getCustSeq());
            } else {
                stockList = new ArrayList<>();
                stockList.add(unrealProfitRequest.getStock());
            }
            List<Result> resultList = new ArrayList<>();
            for (String stock : stockList) {
                //查到資料放入
                getRandomPrice(stock);
                for (Result result : getResultList(new NoProfitRequest(unrealProfitRequest.getBranchNo(), unrealProfitRequest.getCustSeq(), stock))) {
                    resultList.add(result);
                }
            }

            hcmioDetailResponse.setResultList(resultList);
            hcmioDetailResponse.setResponseCode("000");
            hcmioDetailResponse.setMessage("");
        } catch (Exception ex) {
            hcmioDetailResponse.setResponseCode("005");
            hcmioDetailResponse.setMessage("伺服器內部錯誤");
        }
        return hcmioDetailResponse;
    }

    //method-------------------------------------------------------------------------------------------------------
    private List<Result> getResultList(NoProfitRequest unrealProfitRequest) {
        List<Tcnud> stockBalances = tcnudRepo.findByBranchNoAndCustSeqAndStock(unrealProfitRequest.getBranchNo(), unrealProfitRequest.getCustSeq(), unrealProfitRequest.getStock());
        List<Result> results = new ArrayList<>();
        MstmbInfo stockInfo = mstmbInfoRepo.findByStock(unrealProfitRequest.getStock());
        String stockName = stockInfo.getStockName();
        Double curPrice = stockInfo.getCurPrice();
        for (Tcnud stockBalance : stockBalances) {

            results.add(new Result(
                    stockBalance.getTradeDate(),
                    stockBalance.getDocSeq(),
                    stockBalance.getStock(),
                    stockName,
                    stockBalance.getPrice(),
                    curPrice,
                    stockBalance.getQty(),
                    stockBalance.getRemainQty(),
                    stockBalance.getFee(),
                    stockBalance.getCost(),
                    Math.round(stockInfo.getCurPrice() * stockBalance.getQty()),
                    getUnreal(stockBalance.getStock(), stockBalance.getCost(), stockBalance.getQty())

            ));
        }
        return results;
    }

    private String check(NoProfitRequest unrealProfitRequest) {
        if (unrealProfitRequest.getBranchNo().isBlank()) {
            return "BranchNo data wrong";
        }
        if (unrealProfitRequest.getCustSeq().isBlank()) return "CustSeq data wrong";
        if (unrealProfitRequest.getStock().isBlank()) {
            return null;
        } else if (null == mstmbInfoRepo.findByStock(unrealProfitRequest.getStock())) return "Stock doesn't exist";
        if (null == tcnudRepo.findByBranchNoAndCustSeqAndStock(unrealProfitRequest.getBranchNo(), unrealProfitRequest.getBranchNo(), unrealProfitRequest.getStock()))
            return "001";
        return null;
    }

    private String check(StockRequest transactionRequest) {
        //check:request資訊是否正確、股票餘額是否足夠
        //check:tradeDate
        if (transactionRequest.getTradeDate().isBlank()) return "TradeDate data wrong";
        //check:branchNo
        if (transactionRequest.getBranchNo().isBlank()) return "BranchNo data wrong";
        //check:custSeq
        if (transactionRequest.getCustSeq().isBlank()) return "CustSeq data wrong";
        //check:docSeq
        if (transactionRequest.getDocSeq().isBlank()) return "DocSeq data wrong";
        //check:price
        if (transactionRequest.getPrice() <= 0) return "Price data wrong";
        //check:docSeq是否存在
        if (null != hcmioDetailRepo.findByDocSeqAndTradeDate(transactionRequest.getDocSeq(), transactionRequest.getTradeDate()))
            return "This DocSeq already exist";
        //check:stock是否存在
        if (null == mstmbInfoRepo.findByStock(transactionRequest.getStock())) return "This Stock doesn't exist";
        //check:qty不得為空或小於等於0或含有小數
        if (transactionRequest.getQty() <= 0 || null == transactionRequest.getQty() || transactionRequest.getQty() % 1 != 0)
            return "Qty data wrong";
        //qty不得超過9位數
        if (transactionRequest.getQty() >= 1_000_000_000) return "Qty too much";
        if (null != tcnudRepo.getRemainQty(
                transactionRequest.getBranchNo(),
                transactionRequest.getCustSeq(),
                transactionRequest.getStock()
        ) && transactionRequest.getQty() +
                tcnudRepo.getRemainQty(
                        transactionRequest.getBranchNo(),
                        transactionRequest.getCustSeq(),
                        transactionRequest.getStock()) >= 1_000_000_000) {//remainQty不得超過9位數
            return "RemainQty too much";
        }
        return null;
    }

    private Double getUnreal(String stock, Double cost, Double qty) {
        Double curPrice = mstmbInfoRepo.findByStock(stock).getCurPrice();
        return (double) Math.round((curPrice * qty) - cost - getFee(getAmt(curPrice, qty)) - getTax(getAmt(curPrice, qty), "S"));
    }

    private String getNewDocSeq(String tradeDate) {//流水單號
        String lastDocSeqEng = "AA";
        int lastDocSeqInt = 0;
        if (null != hcmioDetailRepo.getNewDocSeq(tradeDate)) {
            lastDocSeqEng = hcmioDetailRepo.getNewDocSeq(tradeDate).substring(0, 2);//取英文0~1
            lastDocSeqInt = Integer.parseInt(hcmioDetailRepo.getNewDocSeq(tradeDate).substring(2, 5));//取數字2~4
        }
        List<Integer> engToAscii = lastDocSeqEng.chars().boxed().collect(Collectors.toList());//英文轉ascii,box()之作用為將int轉為INTEGER
        //數字+1
        lastDocSeqInt++;
        //進位處理--------------------------------------------------------------------------------------------------
        {
            if (lastDocSeqInt > 999) {//如果超過999則歸1且英文進位
                lastDocSeqInt = 1;//歸1
                engToAscii.set(1, engToAscii.get(1) + 1);//英文進位
                if (engToAscii.get(1) > 90) {//如果超過Z
                    engToAscii.set(1, 65);//歸A
                    engToAscii.set(0, engToAscii.get(0) + 1);//進位
                    if (engToAscii.get(0) > 90 && engToAscii.get(0) < 97) {//如果超過Z
                        engToAscii.set(0, 97);//超過預設數據最大量，若超過給臨時數據庫aA001~zA999
                    }
                }
            }
        }
        //數值轉字串之檢查---------------------------------------------------------------------------------------------
        {
            String newDocSeqInt = String.format("%03d", lastDocSeqInt);//數值轉字串，%03d：表示補0到第3位
            String newDocSeqEng = "";
            for (int ascii : engToAscii) {
                newDocSeqEng += Character.toString(ascii);//list英文ascii轉字串
            }
            return newDocSeqEng + newDocSeqInt;
        }
    }

    private Double getAmt(Double price, Double qty) {
        return price * qty;
    }

    private Integer getFee(Double amt) {
        return (int) Math.round(amt * 0.001425);
    }

    private Integer getTax(Double amt, String bsType) {
        return (bsType.equals("S")) ? (int) Math.round(amt * 0.003) : 0;
    }

    private Double getNetAmt(Double amt, Integer fee, Integer tax, String bsType) {
        return (bsType.equals("B")) ? (-(amt + fee)) : (amt - fee - tax);
    }

    private Double getBalancePrice(Double oldPrice, Double oldQty, Double newPrice, Double newQty) {//依比例計算價格
        return (oldPrice * oldQty + newPrice * newQty) / (oldQty + newQty);
    }

    private Double getBalanceCost(Double oldNetAmt, Double newNetAmt, String bsType) {
        return (bsType.equals("B")) ? (Math.abs(oldNetAmt) + Math.abs(newNetAmt)) : (Math.abs(oldNetAmt - newNetAmt));
    }

    private Double getBalanceQty(Double oldQty, Double newQty, String bsType) {
        return (bsType.equals("B")) ? (oldQty + newQty) : (oldQty - newQty);
    }

    private void getRandomPrice(String stock) {
        MstmbInfo stockInfo = mstmbInfoRepo.findByStock(stock);
        Double oldPrice = stockInfo.getCurPrice();
        Double newPrice, r;
        do {
            r = Math.random() / 10;//0~9.99，最高漲跌幅9.99％
            newPrice = ((Math.random() * 10) < 5) ? (oldPrice * (1 + r)) : (oldPrice * (1 - r));//取二分之一機率
        } while (newPrice < 10);//股票最低價10
        stockInfo.setCurPrice(Math.round(newPrice * 100.0) / 100.0);//取小數點後第四位四捨五入
        mstmbInfoRepo.save(stockInfo);
    }

    private Double profitability(Double count) {
        return Math.round(count * 100) / 100.0;
    }

}
