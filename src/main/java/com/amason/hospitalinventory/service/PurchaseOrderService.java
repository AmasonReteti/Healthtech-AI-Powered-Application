package com.amason.hospitalinventory.service;

import com.amason.hospitalinventory.model.Product;
import com.amason.hospitalinventory.model.PurchaseOrder;
import com.amason.hospitalinventory.repository.ProductRepository;
import com.amason.hospitalinventory.repository.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PurchaseOrderService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private StockService stockService;

    /**
     * Checks every product against its reorder level and creates a 
     * DRAFT purchase order for any that are at or below it - but only 
     * if a draft doesn't already exist, so running this repeatedly 
     * never creates duplicates for the same shortage.
     */
    public List<PurchaseOrder> generateDraftsForLowStock() {
        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            int currentStock = stockService.calculateCurrentStock(product.getId());

            if (currentStock > product.getReorderLevel()) {
                continue;
            }

            boolean draftAlreadyExists = !purchaseOrderRepository
                .findByProductIdAndStatus(product.getId(), "DRAFT")
                .isEmpty();

            if (draftAlreadyExists) {
                continue;
            }

            // Order enough to bring stock back up to double the 
            // reorder level - a simple, explainable buffer
            int suggestedQuantity = (product.getReorderLevel() * 2) - currentStock;

            PurchaseOrder order = new PurchaseOrder();
            order.setProduct(product);
            order.setSupplier(product.getSupplier());
            order.setQuantity(Math.max(suggestedQuantity, 1));
            order.setReason(String.format(
                "Stock at %d, at or below reorder level of %d",
                currentStock, product.getReorderLevel()
            ));

            purchaseOrderRepository.save(order);
        }

        return purchaseOrderRepository.findAll();
    }

    public PurchaseOrder submitOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Purchase order not found"));

        if (!"DRAFT".equals(order.getStatus())) {
            throw new IllegalStateException("Only DRAFT orders can be submitted.");
        }

        order.setStatus("SUBMITTED");
        return purchaseOrderRepository.save(order);
    }
}
