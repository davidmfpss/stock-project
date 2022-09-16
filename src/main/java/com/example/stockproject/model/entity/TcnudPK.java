package com.example.stockproject.model.entity;

import java.io.Serializable;
import java.util.Objects;

public class TcnudPK implements Serializable {

    private String tradeDate;
    private String branchNo;
    private String customerSeq;
    private String docSeq;

    //覆蓋equals、hashcode
    //只有當它們的引用指向同一個對象時，兩個對象才被認為是相等的，兩個對象現在都指向同一個存儲桶並在存儲桶中保持相同的位置。
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        //返回引用中存儲的對象的實際類型
        if (o == null || getClass() != o.getClass()) return false;
        TcnudPK that = (TcnudPK) o;
        return Objects.equals(tradeDate, that.tradeDate) && Objects.equals(branchNo, that.branchNo) && Objects.equals(customerSeq, that.customerSeq) && Objects.equals(docSeq, that.docSeq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeDate, branchNo, customerSeq, docSeq);
    }

}
