package com.opencode.alumxbackend.basics.Apooorv012.house.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opencode.alumxbackend.basics.Apooorv012.house.model.ApoorvHouseModel;

@Repository
public interface ApoorvHouseRepository extends JpaRepository<ApoorvHouseModel, Long> {
    
}
