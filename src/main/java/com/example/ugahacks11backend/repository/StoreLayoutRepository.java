package com.example.ugahacks11backend.repository;

import com.example.ugahacks11backend.model.StoreLayout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoreLayoutRepository extends JpaRepository<StoreLayout, UUID> {

    Optional<StoreLayout> findByProductId(UUID productId);
}
