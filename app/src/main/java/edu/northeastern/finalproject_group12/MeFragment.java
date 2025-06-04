package edu.northeastern.finalproject_group12;

import android.util.Patterns;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class MeFragment extends Fragment {

    private TextInputEditText editTextName;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextNewCategory;
    private TextInputEditText editTextNewPaymentMethod;
    private ChipGroup chipGroupExpenseCategories;
    private ChipGroup chipGroupPaymentMethods;
    private TextInputEditText editTextMonthlyBudget;

    private SQLiteControllerHelper dbHelper;
    private SQLiteDatabase db;

    private static final String USER_TABLE = "user";
    private static final String EXPENSE_CATEGORIES_TABLE = "expenseCategories";
    private static final String PAYMENT_METHODS_TABLE = "paymentMethods";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, container, false);
        dbHelper = new SQLiteControllerHelper(getContext(), "transactions.db", null, 1);
        db = dbHelper.getWritableDatabase();

        // Check if the "user" table exists
        if (!dbHelper.isTableExists("user")) {
            Log.e("MeFragment", "Table 'user' does not exist in the database.");
        }

        editTextName = view.findViewById(R.id.editTextName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextMonthlyBudget = view.findViewById(R.id.editTextMonthlyBudget);
        editTextNewCategory = view.findViewById(R.id.editTextNewCategory);
        editTextNewPaymentMethod = view.findViewById(R.id.editTextNewPaymentMethod);
        chipGroupExpenseCategories = view.findViewById(R.id.chipGroupExpenseCategories);
        chipGroupPaymentMethods = view.findViewById(R.id.chipGroupPaymentMethods);

        loadUserProfile();

        view.findViewById(R.id.buttonSaveProfile).setOnClickListener(v -> saveUserProfile());
        view.findViewById(R.id.buttonAddCategory).setOnClickListener(v -> addNewCategory());
        view.findViewById(R.id.buttonAddPaymentMethod).setOnClickListener(v -> addNewPaymentMethod());
        view.findViewById(R.id.buttonSaveBudget).setOnClickListener(v -> saveMonthlyBudget());

        return view;
    }

    private void loadUserProfile() {
        Cursor cursor = db.query(USER_TABLE, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            editTextName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            editTextEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            editTextMonthlyBudget.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("monthlyBudget"))));
        }
        cursor.close();

        loadCategories();
        loadPaymentMethods();
    }

    private void loadCategories() {
        Cursor cursor = db.query(EXPENSE_CATEGORIES_TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            addChipToGroup(chipGroupExpenseCategories, category);
        }
        cursor.close();
    }

    private void loadPaymentMethods() {
        Cursor cursor = db.query(PAYMENT_METHODS_TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String method = cursor.getString(cursor.getColumnIndexOrThrow("payment"));
            addChipToGroup(chipGroupPaymentMethods, method);
        }
        cursor.close();
    }

    private void saveUserProfile() {
        String name = editTextName.getText().toString();
        String email = editTextEmail.getText().toString();

        if (!name.isEmpty() && !email.isEmpty()) {

            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                db.execSQL("DELETE FROM " + USER_TABLE);  // Clear existing data

                db.execSQL("INSERT INTO " + USER_TABLE + " (name, email) VALUES (?, ?)",
                        new Object[]{name, email});

                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(getContext(), "Please enter both name & email.", Toast.LENGTH_SHORT).show();
        }

    }

    private void addNewCategory() {
        String newCategory = editTextNewCategory.getText().toString();
        if (!newCategory.isEmpty()) {
            db.execSQL("INSERT INTO " + EXPENSE_CATEGORIES_TABLE + " (category) VALUES (?)", new Object[]{newCategory});
            addChipToGroup(chipGroupExpenseCategories, newCategory);
            editTextNewCategory.setText("");
            Toast.makeText(getContext(), "Category added successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Please enter a category", Toast.LENGTH_SHORT).show();
        }
    }

    private void addNewPaymentMethod() {
        String newPaymentMethod = editTextNewPaymentMethod.getText().toString();
        if (!newPaymentMethod.isEmpty()) {
            db.execSQL("INSERT INTO " + PAYMENT_METHODS_TABLE + " (payment) VALUES (?)", new Object[]{newPaymentMethod});
            addChipToGroup(chipGroupPaymentMethods, newPaymentMethod);
            editTextNewPaymentMethod.setText("");
            Toast.makeText(getContext(), "Payment method added successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Please enter a payment method", Toast.LENGTH_SHORT).show();
        }
    }

    private void addChipToGroup(ChipGroup chipGroup, String text) {
        Chip chip = new Chip(getContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            removeChipFromDb(chipGroup, text);
        });
        chipGroup.addView(chip);
    }

    private void removeChipFromDb(ChipGroup chipGroup, String text) {
        if (chipGroup == chipGroupExpenseCategories) {
            db.execSQL("DELETE FROM " + EXPENSE_CATEGORIES_TABLE + " WHERE category = ?", new Object[]{text});
        } else {
            db.execSQL("DELETE FROM " + PAYMENT_METHODS_TABLE + " WHERE payment = ?", new Object[]{text});
        }
    }

    private void saveMonthlyBudget() {
        String monthlyBudget = editTextMonthlyBudget.getText().toString();
        if (!monthlyBudget.isEmpty()) {
            double monthlyBudgetDouble = Double.parseDouble(editTextMonthlyBudget.getText().toString());
            db.execSQL("UPDATE " + USER_TABLE + " SET monthlyBudget = ?", new Object[]{monthlyBudgetDouble});
            Toast.makeText(getContext(), "Monthly budget updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Please enter monthly budget", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
