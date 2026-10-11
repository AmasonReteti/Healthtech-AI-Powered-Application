package com.amason.hospitalinventory.controller;

import com.amason.hospitalinventory.dto.ReorderPrediction;
import com.amason.hospitalinventory.service.ReorderPredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reorder-predictions")
public class ReorderPredictionController {

    @Autowired
    private ReorderPredictionService reorderPredictionService;

    @GetMapping
    public List<ReorderPrediction> getAllPredictions() {
        return reorderPredictionService.predictForAllProducts();
    }
}
