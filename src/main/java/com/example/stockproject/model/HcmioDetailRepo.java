package com.example.stockproject.model;

import com.example.stockproject.model.entity.HcmioDetail;
import com.example.stockproject.model.entity.HcmioDetailPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HcmioDetailRepo extends JpaRepository<HcmioDetail, HcmioDetailPK> {

    HcmioDetail findByDocSeqAndTradeDate(String docSeq, String tradeDate);

    @Query(value = "SELECT DocSeq FROM hcmio where TradeDate=?1 ORDER BY DocSeq DESC LIMIT 1", nativeQuery = true)
    String getNewDocSeq(String tradeDate);

    @Query(value = "SELECT * FROM hcmio ORDER BY TradeDate DESC, DocSeq DESC LIMIT 1", nativeQuery = true)
    HcmioDetail getNewDetail();


}
