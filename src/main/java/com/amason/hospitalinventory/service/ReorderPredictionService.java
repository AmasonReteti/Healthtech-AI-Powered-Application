package com.amason.hospitalinventory.service;

import com.amason.hospitalinventory.dto.ReorderPrediction;
import com.amason.hospitalinventory.model.MovementStatus;
import com.amason.hospitalinventory.model.MovementType;
import com.amason.hospitalinventory.model.Product;
import com.amason.hospitalinventory.model.StockMovement;
import com.amason.hospitalinventory.repository.ProductRepository;
import com.amason.hospitalinventory.repository.StockMovementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
public class ReorderPredictionService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private StockService stockService;

    private static final Set<MovementType> REDUCTION_TYPES = Set.of(
        MovementType.DISPENSED, MovementType.TRANSFER, MovementType.DAMAGE, MovementType.EXPIRED
    );

    // Below this many reduction events, any "average daily usage" 
    // number would be more coincidence than pattern - we refuse to 
    // guess rather than produce a confident-looking number from two 
    // data points
    private static final int MINIMUM_MOVEMENTS_REQUIRED = 3;

    public List<ReorderPrediction> predictForAllProducts() {
        return productRepository.findAll().stream()
            .map(this::predictForProduct)
            .toList();
    }

    public ReorderPrediction predictForProduct(Product product) {
        ReorderPrediction prediction = new ReorderPrediction();
        prediction.setProductId(product.getId());
        prediction.setProductName(product.getName());

        int currentStock = stockService.calculateCurrentStock(product.getId());
        prediction.setCurrentStock(currentStock);

        List<StockMovement> reductions = stockMovementRepository.findByProductId(product.getId()).stream()
            .filter(m -> REDUCTION_TYPES.contains(m.getType()))
            .filter(m -> m.getStatus() == MovementStatus.DIRECT || m.getStatus() == MovementStatus.APPROVED)
            .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
            .toList();

        if (reductions.size() < MINIMUM_MOVEMENTS_REQUIRED) {
            prediction.setDataSufficient(false);
            prediction.setNote(
                "Not enough movement history yet to predict reorder timing (" +
                reductions.size() + " of " + MINIMUM_MOVEMENTS_REQUIRED + " needed)."
            );
            return prediction;
        }

        LocalDate firstDate = reductions.get(0).getCreatedAt().toLocalDate();
        LocalDate lastDate = reductions.get(reductions.size() - 1).getCreatedAt().toLocalDate();
        long daySpan = ChronoUnit.DAYS.between(firstDate, lastDate);

        if (daySpan < 1) {
            // All the reductions happened on the same day - we genuinely 
            // can't estimate a daily RATE from a single day's data
            prediction.setDataSufficient(false);
            prediction.setNote("All recorded usage happened within a single day - too little time span to estimate a daily rate.");
            return prediction;
        }

        int totalReduced = reductions.stream().mapToInt(StockMovement::getQuantity).sum();
        double averageDailyUsage = (double) totalReduced / daySpan;

        prediction.setDataSufficient(true);
        prediction.setAverageDailyUsage(Math.round(averageDailyUsage * 100) / 100.0);

        if (averageDailyUsage <= 0) {
            prediction.setNote("Usage rate is effectively zero - no near-term reorder expected.");
            return prediction;
        }

        int daysRemaining = (int) Math.floor(currentStock / averageDailyUsage);
        prediction.setDaysOfStockRemaining(daysRemaining);
        prediction.setPredictedStockoutDate(LocalDate.now().plusDays(daysRemaining));

        // Suggests enough to cover roughly a month of usage at the 
        // current rate - a simple, explainable buffer, not a tuned 
        // forecasting model
        int suggestedQuantity = (int) Math.ceil(averageDailyUsage * 30);
        prediction.setSuggestedReorderQuantity(suggestedQuantity);

        prediction.setNote(String.format(
            "Based on %d units used over %d days (%.2f/day).",
            totalReduced, daySpan, averageDailyUsage
        ));

        return prediction;
    }
}
