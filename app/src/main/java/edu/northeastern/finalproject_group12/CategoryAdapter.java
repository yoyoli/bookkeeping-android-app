package edu.northeastern.finalproject_group12;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private final List<CategoryExpense> categoryExpenses;

    public CategoryAdapter(List<CategoryExpense> categoryExpenses) {
        this.categoryExpenses = categoryExpenses;
        Collections.sort(this.categoryExpenses, new Comparator<CategoryExpense>() {
            @Override
            public int compare(CategoryExpense o1, CategoryExpense o2) {
                return Double.compare(o2.totalAmount, o1.totalAmount);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryExpense categoryExpense = categoryExpenses.get(position);

        holder.indexTextView.setText(String.valueOf(position + 1));
        holder.categoryTextView.setText(categoryExpense.category);
        holder.totalAmountTextView.setText(String.format("%.2f", categoryExpense.totalAmount));
        holder.percentageTextView.setText(String.format("%.2f%%", categoryExpense.percentage));
    }

    @Override
    public int getItemCount() {
        return categoryExpenses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView indexTextView;
        public TextView categoryTextView;
        public TextView totalAmountTextView;
        public TextView percentageTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            indexTextView = itemView.findViewById(R.id.category_index);
            categoryTextView = itemView.findViewById(R.id.category_name);
            totalAmountTextView = itemView.findViewById(R.id.category_amount);
            percentageTextView = itemView.findViewById(R.id.category_percentage);
        }
    }
}

