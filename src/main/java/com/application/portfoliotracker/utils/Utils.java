package com.application.portfoliotracker.utils;

import com.application.portfoliotracker.entities.Holding;
import com.application.portfoliotracker.entities.Trade;
import com.application.portfoliotracker.entities.TradeType;
import com.application.portfoliotracker.entities.TransactionType;
import com.application.portfoliotracker.exceptions.InvalidTransactionException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {

    private Utils() {
    }

    //Updates holdings price and shares after adding trade to it
    public static Holding addTradeToHolding(Holding holding, Trade trade) {
        return updateTradeInHolding(TradeType.ADD, holding, trade, null);
    }

    //Updates holdings price and shares after deleting trade to it
    public static Holding deleteTradeFromHolding(Holding holding, Trade trade) {
        return updateTradeInHolding(TradeType.DELETE, holding, trade, null);
    }

    //Updates holdings price and shares after updating it with newTrade instead of oldTrade.
    //Both trades should be of same ticker
    private static Holding updateTradeInHolding(Holding holding, Trade oldTrade, Trade newTrade) {
        return updateTradeInHolding(TradeType.UPDATE, holding, oldTrade, newTrade);
    }

    //Updates holdings price and shares after adding, updating or deleting trade
    //Both trades should be of same ticker
    private static Holding updateTradeInHolding(TradeType tradeType, Holding holding, Trade trade, Trade newTrade) {

        if (TradeType.UPDATE == tradeType) {
            updatePriceAndShare(TradeType.DELETE, holding, trade);
            updatePriceAndShare(TradeType.ADD, holding, newTrade);
        } else {
            updatePriceAndShare(tradeType, holding, trade);
        }
        if (holding.getTotalPrice().doubleValue() < 0) {
            throw new InvalidTransactionException("Invalid Transaction Causing Price Negative");
        }
        if (holding.getShares().longValue() < 0) {
            throw new InvalidTransactionException("Invalid Transaction Causing Shares Negative");
        }
        holding.setLastUpdated(LocalDateTime.now());
        return holding;
    }



    //Updates holding's price and shares after deleting oldTrade from it
    //Updates newHolding's price and shares after adding newTrade from it
    //Both trades can be of different ticker
    public static List<Holding> updateTradeInHolding(Holding oldHolding, Trade oldTrade, Holding newHolding, Trade newTrade) {
        if (!oldHolding.getTicker().equals(newTrade.getTicker())) {
            assert null != newHolding;
            assert newHolding.getTicker().equals(newTrade.getTicker());
        }
        assert oldHolding.getTicker().equals(oldTrade.getTicker());
        if (null == newHolding || oldTrade.getTicker().equals(newTrade.getTicker())) {
            return Collections.singletonList(updateTradeInHolding(oldHolding, oldTrade, newTrade));
        }
        return Arrays.asList(deleteTradeFromHolding(oldHolding, oldTrade), addTradeToHolding(newHolding, newTrade));
    }



    /**
     * Updates all holdings price and shares after updating it based on trades
     *
     * @param trades Trades based on which the holdings will get updated
     * @return Holdings after updating it's price and shares
     */
    public static List<Holding> getHoldingsFromTrades(List<Trade> trades) {
        return trades.stream()
                .collect(Collectors.groupingBy(Trade::getTicker))
                .values()
                .stream()
                .map(Utils::getTradesCombinedByTicker)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(trade -> !BigInteger.ZERO.equals(trade.getShares()))
                .map(Utils::getHoldings)
                .collect(Collectors.toList());
    }


     /* Updates holdings price and shares after adding or deleting trade
     *
     * @param tradeType Tells whether trade is to be added or deleted trade
     * @param holding   Holding to be updated
     * @param trade     Trade to be added or deleted
     */
    private static void updatePriceAndShare(TradeType tradeType, Holding holding, Trade trade) {
        BigDecimal tradeShares = BigDecimal.valueOf(trade.getShares().longValue());
        BigDecimal price = TransactionType.BUY == trade.getTransactionType() ?
                tradeShares.multiply(trade.getPrice()) :
                tradeShares.multiply(holding.getTotalPrice())
                        .divide(BigDecimal.valueOf(holding.getShares().longValue()), MathContext.DECIMAL128);
        if (TradeType.DELETE == tradeType) {
            trade.setTransactionType(TransactionType.BUY == trade.getTransactionType() ? TransactionType.SELL : TransactionType.BUY);
        }
        if (TransactionType.BUY == trade.getTransactionType()) {
            holding.setTotalPrice(holding.getTotalPrice().add(price));
            holding.setShares(holding.getShares().add(trade.getShares()));
            if (TradeType.DELETE != tradeType) {
                holding.setAverageBuyPrice(
                        holding.getTotalPrice().divide(
                                BigDecimal.valueOf(holding.getShares().intValue()), MathContext.DECIMAL128
                        )
                );
            }
        } else {
            BigDecimal netPrice = holding.getTotalPrice().subtract(price);
            holding.setTotalPrice(netPrice);
            BigInteger netShares = holding.getShares().subtract(trade.getShares());
            holding.setShares(netShares);
            if (BigInteger.ZERO.equals(holding.getShares())) {
                holding.setAverageBuyPrice(BigDecimal.ZERO);
            } else if (TradeType.DELETE == tradeType) {
                holding.setAverageBuyPrice(
                        holding.getTotalPrice().divide(
                                BigDecimal.valueOf(holding.getShares().intValue()), MathContext.DECIMAL128
                        )
                );
            }
        }
    }

    /**
     * Combines trades by ticker
     *
     * @param trades List of trades to be combined
     * @return Optional of trade combined by ticker i.e one trade per ticker.
     * Empty optional is returned if list is empty
     */
    private static Optional<Trade> getTradesCombinedByTicker(List<Trade> trades) {
        return trades.stream()
                .collect(Collectors.groupingBy(Trade::getTransactionType))
                .values()
                .stream()
                .map(Utils::getTradesCombinedByTransactionType)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce((t1, t2) -> {
                    Trade trade1, trade2;
                    if (TransactionType.BUY == t1.getTransactionType()) {
                        trade1 = t1;
                        trade2 = t2;
                    } else {
                        trade1 = t2;
                        trade2 = t1;
                    }
                    BigInteger trade1Shares = trade1.getShares();
                    BigInteger trade2Shares = trade2.getShares();
                    BigInteger netShares = trade1Shares.subtract(trade2Shares);
                    BigDecimal netPrice = trade1.getPrice()
                            .multiply(BigDecimal.valueOf(netShares.intValue()))
                            .divide(BigDecimal.valueOf(trade1Shares.intValue()), MathContext.DECIMAL128);
                    LocalDateTime lastUpdated = trade2.getLastUpdated().isAfter(trade1.getLastUpdated()) ? trade2.getLastUpdated() : trade1.getLastUpdated();
                    return Trade.builder()
                            .ticker(trade2.getTicker())
                            .shares(netShares)
                            .price(netPrice)
                            .lastUpdated(lastUpdated)
                            .build();
                });
    }

    /**
     * Combines trades by transactionType
     *
     * @param trades List of trades to be combined
     * @return Optional of trade combined By transactionType i.e one trade per transactionType.
     * Empty optional is returned if list is empty
     */
    private static Optional<Trade> getTradesCombinedByTransactionType(List<Trade> trades) {
        return trades.stream()
                .sorted(Comparator.comparing(Trade::getLastUpdated))
                .peek(trade -> trade.setPrice(trade.getPrice().multiply(BigDecimal.valueOf(trade.getShares().intValue()))))
                .reduce((trade1, trade2) -> {
                    return Trade.builder()
                            .ticker(trade2.getTicker())
                            .shares(trade1.getShares().add(trade2.getShares()))
                            .price(trade1.getPrice().add(trade2.getPrice()))
                            .transactionType(trade2.getTransactionType())
                            .lastUpdated(trade2.getLastUpdated())
                            .build();
                });
    }

    /**
     * Gets holding from trade combined by ticker
     *
     * @param trade Trade combined by ticker
     * @return Holding obtained from trade combined By ticker
     */
    private static Holding getHoldings(Trade trade) {
        BigDecimal averagePrice = trade.getPrice()
                .divide(BigDecimal.valueOf(trade.getShares().intValue()), MathContext.DECIMAL128);
        return Holding.builder()
                .ticker(trade.getTicker())
                .shares(trade.getShares())
                .totalPrice(trade.getPrice())
                .averageBuyPrice(averagePrice)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

}
