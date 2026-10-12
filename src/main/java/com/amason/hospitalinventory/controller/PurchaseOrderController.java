package com.amason.hospitalinventory.controller;

import com.amason.hospitalinventory.model.PurchaseOrder;
import com.amason.hospitalinventory.repository.PurchaseOrderRepository;
import com.amason.hospitalinventory.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @GetMapping
    public List<PurchaseOrder> getAll() {
        return purchaseOrderRepository.findAll();
    }

    // Generating is safe to let any logged-in user trigger, since it 
    // only READS stock levels and creates drafts - nothing commits 
    // until an Admin actually submits one
    @PostMapping("/generate")
    public List<PurchaseOrder> generate() {
        return purchaseOrderService.generateDraftsForLowStock();
    }

    @PostMapping("/{id}/submit")
    public PurchaseOrder submit(@PathVariable Long id) {
        return purchaseOrderService.submitOrder(id);
    }
}
