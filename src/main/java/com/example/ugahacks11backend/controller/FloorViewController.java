package com.example.ugahacks11backend.controller;

import com.example.ugahacks11backend.dto.FloorViewDTO;
import com.example.ugahacks11backend.service.FloorViewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/floorview")
public class FloorViewController {

    private final FloorViewService floorViewService;

    public FloorViewController(FloorViewService floorViewService) {
        this.floorViewService = floorViewService;
    }

    // GET /floorview — Get complete floor view with inventory pressure
    @GetMapping
    public List<FloorViewDTO> getFloorView() {
        return floorViewService.getFloorView();
    }
}
