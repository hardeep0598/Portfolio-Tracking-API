package com.application.portfoliotracker.entities;

import com.application.portfoliotracker.enums.TradeType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Table(name = "trades")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "All details about the Trade")
public class Trade {

    @Id
    @Column(name = "trade_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ApiModelProperty(notes = "Trade id is not required for an addition but required in case of an update")
    private Long tradeId;

    @Column(name = "ticker")
    @ApiModelProperty(notes = "Ticker for which the trade was executed")
    private String ticker;

    @Column(name = "transaction_type")
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(notes = "transactionType can only be BUY/SELL")
    private TransactionType transactionType;

    @Column(name = "price")
    @ApiModelProperty(notes = "Price of a share. It should be greater than 0")
    private BigDecimal price;

    @ApiModelProperty(notes = "Amount of shares. It should be greater than 0")
    @Column(name = "shares")
    private BigInteger shares;

    @ApiModelProperty(notes = "Time when the trade was executed")
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
