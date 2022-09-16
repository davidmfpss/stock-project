package com.example.stockproject.model;

import com.example.stockproject.model.entity.Tcnud;
import com.example.stockproject.model.entity.TcnudPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TcnudRepo extends JpaRepository<Tcnud, TcnudPK> {

    @Query(value = "select * from tcnud where BranchNo = ?1 and CustSeq = ?2 and stock = ?3  ;", nativeQuery = true)
    List<Tcnud> findByBranchNoAndCustSeqAndStock(String branchNo, String custSeq, String stock);
    @Query(value = "select sum(Cost) from tcnud where BranchNo = ?1 and CustSeq = ?2 and TradeDate = ?3  ;", nativeQuery = true)
    Long findTodayBalance(String branchNo, String custSeq, String tradeDate);
    @Query(value = "select sum(Qty) from tcnud where BranchNo= ?1 and CustSeq= ?2 and stock= ?3 ;", nativeQuery = true)
    Double getRemainQty(String branchNo, String custSeq, String stock);

    @Query(value = "select distinct Stock from tcnud where BranchNo = ?1 and CustSeq = ?2 order by Stock  ;", nativeQuery = true)
    List<String> getAllStock(String branchNo, String custSeq);


}
