package com.example.ugahacks11backend.controller;

import com.example.ugahacks11backend.dto.TransactionRequest;
import com.example.ugahacks11backend.model.WasteLog;
import com.example.ugahacks11backend.service.InventoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // POST /inventory/checkout — Customer sale
    @PostMapping("/checkout")
    public Map<String, Object> checkout(@RequestBody TransactionRequest request) {
        return inventoryService.checkout(request);
    }

    // POST /inventory/restock — Move from Back → Front
    @PostMapping("/restock")
    public Map<String, Object> restock(@RequestBody TransactionRequest request) {
        return inventoryService.restock(request);
    }

    // POST /inventory/waste — Log expired/damaged goods
    @PostMapping("/waste")
    public Map<String, Object> logWaste(@RequestBody TransactionRequest request) {
        return inventoryService.logWaste(request);
    }

    // GET /inventory/waste — All waste history
    @GetMapping("/waste")
    public List<WasteLog> getAllWasteHistory() {
        return inventoryService.getAllWasteHistory();
    }

    // GET /inventory/waste/{barcode} — Waste history for a specific product
    @GetMapping("/waste/{barcode}")
    public List<WasteLog> getWasteHistory(@PathVariable String barcode) {
        return inventoryService.getWasteHistory(barcode);
    }
}
