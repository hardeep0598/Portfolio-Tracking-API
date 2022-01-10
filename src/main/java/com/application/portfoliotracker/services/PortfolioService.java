package com.application.portfoliotracker.services;

import com.application.portfoliotracker.entities.Holding;
import com.application.portfoliotracker.entities.Portfolio;
import com.application.portfoliotracker.entities.Trade;
import com.application.portfoliotracker.entities.TransactionType;
import com.application.portfoliotracker.exceptions.InvalidTransactionException;
import com.application.portfoliotracker.exceptions.NotFoundException;
import com.application.portfoliotracker.repositories.HoldingRepository;
import com.application.portfoliotracker.repositories.TradeRepository;
import com.application.portfoliotracker.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PortfolioService  {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    //adding new trade
    public Trade addTrade(Trade trade) {
        Holding holding = Utils.addTradeToHolding(getHoldingByTrade(trade), trade);
        saveHolding(holding);
        return tradeRepository.save(trade);
    }


    //update the trade
    public Trade updateTrade(Long id, Trade trade) {
        Trade existingTrade= tradeRepository.findById(id).orElseThrow(
                ()-> new NotFoundException("trade id - "+id+ "is not found")
        );

        assertHoldingPresent(trade.getTicker());
        List<Holding> holdings = Utils.updateTradeInHolding(getHoldingByTrade(existingTrade), existingTrade, getHoldingByTrade(trade), trade);
        holdingRepository.saveAll(holdings);

        //saving existingTrade to update them
        tradeRepository.save(trade);
        return trade;
    }

    //method to delete trade by id
    public Optional<Trade> deleteTrade(Long id) {
        Optional<Trade> tradeOptional = getTrade(id);
        if (!tradeOptional.isPresent()) {
            return Optional.empty();
        }
        Trade trade = tradeOptional.get();
        assertHoldingPresent(trade.getTicker());
        Holding holding = Utils.deleteTradeFromHolding(getHoldingByTrade(trade), trade);
        saveHolding(holding);
        deleteTrade(trade);
        return tradeOptional;
    }

    //Groups all the securities and trades corresponding to it.
    public List<Portfolio> getPortfolio() {
        return getHoldings().stream().map(Holding::getTicker)
                .map(this::getPortfolioByTicker)
                .collect(Collectors.toList());
    }

    //Groups all the trades corresponding to a security.
    private Portfolio getPortfolioByTicker(String ticker) {
        return Portfolio.builder()
                .ticker(ticker)
                .trades(getTradesByTicker(ticker))
                .build();
    }


    //Saves or updates holding in db. If given holding has shares or price as zero,
    //it deletes the holding from db
    public void saveHolding(Holding holding) {
        if (BigInteger.ZERO.equals(holding.getShares())) {
            holdingRepository.deleteById(holding.getTicker());
            return;
        }
        holdingRepository.save(holding);
    }

    //method to get trade based on trade id
    public Optional<Trade> getTrade(Long id) {
        return tradeRepository.findById(id);
    }

    //method to return the list of all trades
    public List<Trade> getTrades() {
        return tradeRepository.findAll();
    }

    //method to get holding corresponding to the ticker symbol
    public Optional<Holding> getHolding(String ticker) {
        return holdingRepository.findById(ticker);
    }

    //method to return list of all holdings
    public List<Holding> getHoldings() {
        return holdingRepository.findAll();
    }

    //Refreshes all the holdings by recalculating based on all trades present.
    public List<Holding> refreshHoldings() {
        return holdingRepository.saveAll(Utils.getHoldingsFromTrades(getTrades()));
    }



    //Takes all the holdings and calculates the returns
    public Optional<BigDecimal> getReturns() {
        return getHoldings().stream()
                .map(this::getReturnsByHolding)
                .reduce(BigDecimal::add);
    }

    //Takes holding and calculates its return
    private BigDecimal getReturnsByHolding(Holding holding) {
        BigDecimal priceDifference = getCurrentPrice(holding).subtract(holding.getAverageBuyPrice());
        return priceDifference.multiply(BigDecimal.valueOf(holding.getShares().intValue()));
    }

    //checks if that Holding is present for a ticker.
    private void assertHoldingPresent(String ticker) {
        Optional<Holding> holdingOptional = getHolding(ticker);
        if (!holdingOptional.isPresent()) {
            log.error("No Holding found with ticker : {}", ticker);
        }
    }

    //Gets all the trades corresponding to a ticker
    public List<Trade> getTradesByTicker(String ticker) {
        return tradeRepository.findByTicker(ticker);
    }

    //Takes the holding and tells it's current price using it's ticker (default-100)
    private BigDecimal getCurrentPrice(Holding holding) {
        return BigDecimal.valueOf(100);
    }

    //method to add trade to holdings, if not present
    private Holding getHoldingByTrade(Trade trade) {
        return holdingRepository.findById(trade.getTicker())
                .orElse(Holding.builder()
                        .ticker(trade.getTicker())
                        .shares(BigInteger.ZERO)
                        .totalPrice(BigDecimal.ZERO)
                        .averageBuyPrice(BigDecimal.ZERO)
                        .build());
    }

    //check whether the given trade is valid or not
    //checks for all basic validations
    public void isValidTrade(Trade trade) {
        Optional<Holding> holding = getHolding(trade.getTicker());
        if (TransactionType.SELL == trade.getTransactionType() && !holding.isPresent()) {
            throw new InvalidTransactionException("No shares available to sell");
        } else if (0 == BigInteger.ZERO.compareTo(trade.getShares())) {
            throw new InvalidTransactionException("Shares cannot be zero");
        } else if (0 < BigInteger.ZERO.compareTo(trade.getShares())) {
            throw new InvalidTransactionException("Shares cannot less than zero");
        } else if (0 == BigDecimal.ZERO.compareTo(trade.getPrice())) {
            throw new InvalidTransactionException("Price cannot be zero");
        } else if (0 < BigDecimal.ZERO.compareTo(trade.getPrice())) {
            throw new InvalidTransactionException("Price cannot less than zero");
        }
    }

    //method to delete the existing trade
    public void deleteTrade(Trade trade) {
        tradeRepository.delete(trade);
    }

}
