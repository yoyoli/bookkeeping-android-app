package edu.northeastern.finalproject_group12;

public class CategoryExpense {
    public String category;
    public double totalAmount;
    public double percentage;

    public CategoryExpense(String category, double totalAmount, double percentage) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
    }

    public String getCategory() {
        return category;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getPercentage() {
        return percentage;
    }
}
