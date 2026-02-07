package com.example.ugahacks11backend.repository;

import com.example.ugahacks11backend.model.WasteLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WasteLogRepository extends JpaRepository<WasteLog, UUID> {

    List<WasteLog> findByBarcode(String barcode);
}
