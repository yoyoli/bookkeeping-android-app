package edu.northeastern.finalproject_group12.transaction;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.finalproject_group12.R;
import edu.northeastern.finalproject_group12.SQLiteControllerHelper;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private SQLiteControllerHelper dbHelper;
    private Context context;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.transactionList = transactionList;
        this.dbHelper = new SQLiteControllerHelper(context, "transactions.db", null, 1);
        this.context = context;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.categoryTextView.setText(transaction.getCategory());
        holder.amountTextView.setText("$" + String.format("%.2f", transaction.getAmount()));
        holder.dateTextView.setText(transaction.getDate());

        holder.deleteButton.setOnClickListener(v -> {
            int transactionId = transaction.getTransactionId();
            dbHelper.deleteTransaction(transactionId);
            removeTransaction(position);
        });

        holder.editButton.setOnClickListener(v -> {
            // Create a new instance of TransactionFragment
            TransactionFragment transactionFragment = new TransactionFragment();

            // Pass the current transaction to the fragment
            Bundle bundle = new Bundle();
            bundle.putParcelable("transaction", transaction);
            transactionFragment.setArguments(bundle);

            // Replace the current fragment with the TransactionFragment
            FragmentTransaction fragmentTransaction = ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, transactionFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public Transaction getTransactionAt(int position) {
        return transactionList.get(position);
    }

    public void removeTransaction(int position) {
        transactionList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, transactionList.size());
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {

        TextView categoryTextView;
        TextView amountTextView;
        TextView dateTextView;
        Button editButton, deleteButton;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            editButton = itemView.findViewById(R.id.btnEdit);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }
    }
}
