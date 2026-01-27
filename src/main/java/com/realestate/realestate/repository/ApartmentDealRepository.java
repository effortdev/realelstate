package com.realestate.realestate.repository;

import com.realestate.realestate.domain.ApartmentDeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApartmentDealRepository extends JpaRepository<ApartmentDeal, Long> {
}