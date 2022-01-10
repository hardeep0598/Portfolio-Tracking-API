package com.application.portfoliotracker.repositories;

import com.application.portfoliotracker.entities.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByTicker(String ticker);

}
