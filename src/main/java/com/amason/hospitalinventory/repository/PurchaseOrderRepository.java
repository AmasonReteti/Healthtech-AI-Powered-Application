package com.amason.hospitalinventory.repository;

import com.amason.hospitalinventory.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    List<PurchaseOrder> findByProductIdAndStatus(Long productId, String status);
}
