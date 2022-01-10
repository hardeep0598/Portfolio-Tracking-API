package com.application.portfoliotracker.repositories;

import com.application.portfoliotracker.entities.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, String> {

}