package com.application.portfoliotracker.entities;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "All details about the portfolio")
public class Portfolio {

    @ApiModelProperty(notes = "Ticker for which the trades were executed")
    private String ticker;

    @ApiModelProperty(notes = "List of trades per ticker")
    private List<Trade> trades;
}
