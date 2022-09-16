package com.example.stockproject.controller.response;

import com.example.stockproject.model.entity.MstmbInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Update {
    private MstmbInfo stockInfo;
    private String message;
}
