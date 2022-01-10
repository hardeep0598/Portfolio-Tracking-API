package com.application.portfoliotracker.entities;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Table(name = "holdings")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "All details about the holding")
public class Holding {

    @Id
    @Column(name = "ticker")
    private String ticker;

    @Column(name = "total_price")
    @ApiModelProperty(notes = "Total price per ticker")
    private BigDecimal totalPrice;

    @Column(name = "average_buy_price")
    @ApiModelProperty(notes = "Average price per ticker")
    private BigDecimal averageBuyPrice;

    @Column(name = "shares")
    @ApiModelProperty(notes = "Total shares per ticker")
    private BigInteger shares;

    @Column(name = "last_updated")
    @ApiModelProperty(notes = "Time when the last trade was executed")
    private LocalDateTime lastUpdated;
}