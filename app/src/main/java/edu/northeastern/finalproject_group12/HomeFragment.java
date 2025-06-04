package edu.northeastern.finalproject_group12;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import edu.northeastern.finalproject_group12.transaction.Transaction;
import android.util.Log;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.northeastern.finalproject_group12.transaction.TransactionAdapter;
import edu.northeastern.finalproject_group12.transaction.TransactionFragment;

public class HomeFragment extends Fragment {

    private Button calendarButton;
    private ViewPager2 viewPager;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddTransaction;
    private SQLiteControllerHelper dbHelper;

    private int selectedMonth;
    private int selectedYear;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        calendarButton = view.findViewById(R.id.calendar_button);
        viewPager = view.findViewById(R.id.view_pager);
        recyclerView = view.findViewById(R.id.recycler_view);
        fabAddTransaction = view.findViewById(R.id.fab_add_transaction);

        dbHelper = new SQLiteControllerHelper(getContext(), "transactions.db", null, 1);
        Calendar calendar = Calendar.getInstance();
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedYear = calendar.get(Calendar.YEAR);
        calendarButton.setText(android.text.format.DateFormat.format("MMMM yyyy", calendar));

        setupViewPager();
        setupRecyclerView();

        calendarButton.setOnClickListener(v -> showDatePickerDialog());
        fabAddTransaction.setOnClickListener(v -> openTransactionFragment());

        return view;
    }

    private void setupViewPager() {
        viewPager.setAdapter(new ViewPagerAdapter(this));
        TabLayout tabLayout = new TabLayout(getContext());
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Income");
            } else {
                tab.setText("Expenses");
            }
        }).attach();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TransactionAdapter(getContext(), getRecentTransactions()));
    }

    private List<Transaction> getRecentTransactions() {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String monthString = String.format("%02d", selectedMonth + 1);
        String dateFilter = monthString + "/%";

        Cursor cursor = db.query(
                SQLiteControllerHelper.getTransactionInfo(),
                null,
                "date LIKE ?",
                new String[]{dateFilter},
                null,
                null,
                "date DESC"
        );

        while (cursor.moveToNext()) {
            int transactionId = cursor.getInt(cursor.getColumnIndexOrThrow("transaction_ID"));
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            transactionList.add(new Transaction(transactionId, category, amount, date));
        }

        cursor.close();
        return transactionList;
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    selectedMonth = month;
                    selectedYear = year;
                    calendarButton.setText(android.text.format.DateFormat.format("MMMM yyyy", calendar));
                    setupRecyclerView();
                    refreshViewPagerFragments();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void refreshViewPagerFragments() {
        double totalBudget = dbHelper.getMonthlyBudgetForHome();
        double totalExpenses = dbHelper.getTotalExpensesForMonth(selectedMonth, selectedYear);
        double remainingBudget = totalBudget - totalExpenses;

        Log.d("HomeFragment", "Total Expenses: " + totalExpenses);
        Log.d("HomeFragment", "Remaining Budget: " + remainingBudget);

        ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager.getAdapter();
        if (adapter != null) {
            PlaceholderFragment incomeFragment = (PlaceholderFragment) adapter.getFragmentAtPosition(0);
            if (incomeFragment != null) {
                incomeFragment.updateRemainingBudget(remainingBudget);
            }

            PlaceholderFragment expensesFragment = (PlaceholderFragment) adapter.getFragmentAtPosition(1);
            if (expensesFragment != null) {
                expensesFragment.updateTotalExpenses(totalExpenses);
                Log.d("HomeFragment", "Updating total expenses in fragment: " + totalExpenses);
            }
        }
    }

    private void openTransactionFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new TransactionFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private class ViewPagerAdapter extends FragmentStateAdapter {
        private final SparseArray<Fragment> fragmentReferences = new SparseArray<>();

        public ViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            PlaceholderFragment fragment = new PlaceholderFragment(position);
            Bundle args = new Bundle();
            if (position == 0) {
                double totalBudget = dbHelper.getMonthlyBudgetForHome();
                double remainingBudget = totalBudget - dbHelper.getTotalExpensesForMonth(selectedMonth, selectedYear);
                args.putDouble("remainingBudget", remainingBudget);
            } else {
                double totalExpenses = dbHelper.getTotalExpensesForMonth(selectedMonth, selectedYear);
                args.putDouble("totalExpenses", totalExpenses);
            }
            fragment.setArguments(args);
            fragmentReferences.put(position, fragment);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        public Fragment getFragmentAtPosition(int position) {
            return fragmentReferences.get(position);
        }
    }

    public static class PlaceholderFragment extends Fragment {
        private int position;
        private TextView remainingBudgetTextView;
        private TextView totalExpensesTextView;

        public PlaceholderFragment(int position) {
            this.position = position;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view;
            if (position == 0) {
                view = inflater.inflate(R.layout.fragment_income, container, false);
                remainingBudgetTextView = view.findViewById(R.id.tv_remaining_budget);
                updateRemainingBudget(getArguments().getDouble("remainingBudget", 0));
            } else {
                view = inflater.inflate(R.layout.fragment_expenses, container, false);
                totalExpensesTextView = view.findViewById(R.id.tv_total_expenses);
                updateTotalExpenses(getArguments().getDouble("totalExpenses", 0));
            }
            return view;
        }

        public void updateRemainingBudget(double remainingBudget) {
            if (remainingBudgetTextView != null) {
                if (remainingBudget < 0) {
                    remainingBudgetTextView.setText("- $" + String.format("%.2f", Math.abs(remainingBudget)));
                } else {
                    remainingBudgetTextView.setText("$" + String.format("%.2f", remainingBudget));
                }
            } else {
                Log.d("PlaceholderFragment", "remainingBudgetTextView is null");
            }
        }

        public void updateTotalExpenses(double totalExpenses) {
            if (totalExpensesTextView != null) {
                if (totalExpenses < 0) {
                    totalExpensesTextView.setText("- $" + String.format("%.2f", Math.abs(totalExpenses)));
                } else {
                    totalExpensesTextView.setText("$" + String.format("%.2f", totalExpenses));
                }
            } else {
                Log.d("PlaceholderFragment", "totalExpensesTextView is null");
            }
        }
    }
}
