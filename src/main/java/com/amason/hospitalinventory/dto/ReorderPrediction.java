package com.amason.hospitalinventory.dto;

import java.time.LocalDate;

// Every field here is honest about uncertainty - "dataSufficient" and 
// "note" exist specifically so the prediction never pretends to be 
// confident when the underlying history is too thin to trust
public class ReorderPrediction {

    private Long productId;
    private String productName;
    private int currentStock;
    private boolean dataSufficient;
    private String note;
    private Double averageDailyUsage;
    private Integer daysOfStockRemaining;
    private LocalDate predictedStockoutDate;
    private Integer suggestedReorderQuantity;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

    public boolean isDataSufficient() { return dataSufficient; }
    public void setDataSufficient(boolean dataSufficient) { this.dataSufficient = dataSufficient; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Double getAverageDailyUsage() { return averageDailyUsage; }
    public void setAverageDailyUsage(Double averageDailyUsage) { this.averageDailyUsage = averageDailyUsage; }

    public Integer getDaysOfStockRemaining() { return daysOfStockRemaining; }
    public void setDaysOfStockRemaining(Integer daysOfStockRemaining) { this.daysOfStockRemaining = daysOfStockRemaining; }

    public LocalDate getPredictedStockoutDate() { return predictedStockoutDate; }
    public void setPredictedStockoutDate(LocalDate predictedStockoutDate) { this.predictedStockoutDate = predictedStockoutDate; }

    public Integer getSuggestedReorderQuantity() { return suggestedReorderQuantity; }
    public void setSuggestedReorderQuantity(Integer suggestedReorderQuantity) { this.suggestedReorderQuantity = suggestedReorderQuantity; }
}
