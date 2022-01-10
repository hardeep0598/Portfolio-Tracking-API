package com.application.portfoliotracker.controllers;

import com.application.portfoliotracker.entities.Holding;
import com.application.portfoliotracker.entities.Portfolio;
import com.application.portfoliotracker.entities.Trade;
import com.application.portfoliotracker.exceptions.BadRequestException;
import com.application.portfoliotracker.exceptions.NotFoundException;
import com.application.portfoliotracker.services.PortfolioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @PostMapping("/add-trade")
    public ResponseEntity<Trade> addTrade(@RequestBody Trade trade) {
        log.info("Add or update trade request came for user for trade id : {}", trade.getTradeId());
        portfolioService.isValidTrade(trade);
        return new ResponseEntity<>(portfolioService.addTrade(trade), HttpStatus.CREATED);
    }

    @PutMapping("/update-trade/{id}")
    public ResponseEntity<Trade> UpdateTrade(@PathVariable Long id,@RequestBody Trade trade) {
        log.info("Add or update trade request came for user for trade id : {}", trade.getTradeId());
        portfolioService.isValidTrade(trade);
        return new ResponseEntity<>(portfolioService.updateTrade(id,trade), HttpStatus.OK);
    }

    @DeleteMapping("/trades/{id}")
    public ResponseEntity<Trade> deleteTrade(@PathVariable String id) {
        log.info("Delete trade request came for user for trade id : {}", id);
        return portfolioService.deleteTrade(getTradeId(id))
                .map(trade -> new ResponseEntity<>(trade, HttpStatus.OK))
                .orElseThrow(() -> new NotFoundException("No Trade found for id : " + id));
    }

    @GetMapping("/trades/{id}")
    public ResponseEntity<Trade> getTrade(@PathVariable String id) {
        log.info("Show trade request came for user for trade id : {}", id);
        return portfolioService.getTrade(getTradeId(id))
                .map(trade -> new ResponseEntity<>(trade, HttpStatus.OK))
                .orElseThrow(() -> new NotFoundException("No Trade found for id : " + id));
    }

    @GetMapping("/trades/all")
    public ResponseEntity<List<Trade>> getTrades() {
        log.info("Show trades request came for user");
        return Optional.of(portfolioService.getTrades())
                .filter(list -> !list.isEmpty())
                .map(trades -> new ResponseEntity<>(trades, HttpStatus.OK))
                .orElseThrow(() -> new NotFoundException("No Trade found for user"));
    }

    @GetMapping("/holding-for/{ticker}")
    public ResponseEntity<Holding> getHolding(@PathVariable String ticker) {
        log.info("Show holding request came for user for ticker : {}", ticker);
        return portfolioService.getHolding(ticker)
                .map(holding -> new ResponseEntity<>(holding, HttpStatus.OK))
                .orElseThrow(() -> new NotFoundException(String.format("No Holding found for ticker : %s", ticker)));
    }

    @GetMapping("/holdings/all")
    public ResponseEntity<List<Holding>> getHoldings() {
        log.info("Get holding request came for user");
        return Optional.of(portfolioService.getHoldings())
                .filter(list -> !list.isEmpty())
                .map(holdings -> new ResponseEntity<>(holdings, HttpStatus.OK))
                .orElseThrow(() -> new NotFoundException("User has no securities"));
    }

    @GetMapping("/fetch-portfolio")
    public ResponseEntity<List<Portfolio>> getPortfolio() {
        log.info("Show portfolio request came for user");

        return Optional.of(portfolioService.getPortfolio())
                .filter(list -> !list.isEmpty())
                .map(holdings -> new ResponseEntity<>(holdings, HttpStatus.OK))
                .orElseThrow(() -> new NotFoundException("User has no securities"));
    }

    @GetMapping("/fetch-returns")
    public ResponseEntity<BigDecimal> getReturns() {
        log.info("Show returns request came for user");
        return portfolioService.getReturns()
                .map(returns -> new ResponseEntity<>(returns, HttpStatus.OK))
                .orElseThrow(() -> new NotFoundException("User has no securities"));
    }

    private Long getTradeId(String id) {
        try {
            return Long.valueOf(id);
        }  catch (NumberFormatException numberFormatException) {
            throw new BadRequestException("Trade id should be a number");
        }
    }

}
