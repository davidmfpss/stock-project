package com.example.stockproject.model;

import com.example.stockproject.model.entity.MstmbInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MstmbInfoRepo extends JpaRepository<MstmbInfo, String> {

    MstmbInfo findByStock(String stock);

}
