package edu.northeastern.finalproject_group12.transaction;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.FragmentTransaction;
import edu.northeastern.finalproject_group12.HomeFragment;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import edu.northeastern.finalproject_group12.R;
import edu.northeastern.finalproject_group12.SQLiteControllerHelper;

import android.app.Activity;
import android.database.Cursor;



public class TransactionFragment extends Fragment {

    private RecyclerView transactionRecycler;
    private InputAdapter inputAdapter;
    private View recyclerViewConstraint;
    private EditText numberDecimal;
    private TextView dateHolder;
    private TextView displayCategoryClicked;
    private ChipGroup chipGroupExpenseCategories;
    private TextView displayPaymentClicked;
    private ChipGroup chipGroupPaymentMethods;
    private Button advancedButton;
    private Button pickLocationButton;
    private TextView latAndLong;
    private Button clearButton;
    private TextInputEditText notesEditText;
    private Button submitButton;
    private boolean advancedIsClicked;
    private SQLiteDatabase db;
    private static final int MAPS_REQUEST_CODE = 100;
    private static final String EXPENSE_CATEGORIES_TABLE = "expenseCategories";
    private static final String PAYMENT_METHODS_TABLE = "paymentMethods";

    private double FinalLatitude = Double.NaN;
    private double FinalLongitude = Double.NaN;

    private Transaction transactionToEdit;
    private long transactionIdToEdit = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void populateFieldsForEdit(Transaction transaction) {
        if (transaction != null) {
            numberDecimal.setText(String.valueOf(transaction.getAmount()));
            dateHolder.setText(transaction.getDate());
            displayCategoryClicked.setText(transaction.getCategory());
            if (!Double.isNaN(transaction.getLatitude()) && !Double.isNaN(transaction.getLongitude())) {
                String temp = "(" + transaction.getLatitude() + ", " + transaction.getLongitude() + ")";
                latAndLong.setText(temp);
            }
            if (transaction.getNotes() != null) {
                notesEditText.setText(transaction.getNotes());
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        recyclerViewConstraint = inflater.inflate(R.layout.fragment_transaction, container, false);

        db = (new SQLiteControllerHelper(getContext(), "transactions.db", null, 1)).getWritableDatabase();

        transactionRecycler = recyclerViewConstraint.findViewById(R.id.transactionRecycler);
        transactionRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        inputAdapter = new InputAdapter(createComponents(inflater));
        transactionRecycler.setAdapter(inputAdapter);

        View addressComponent = inflater.inflate(R.layout.component_address, transactionRecycler, false);
        latAndLong = addressComponent.findViewById(R.id.latAndLong);
        pickLocationButton = addressComponent.findViewById(R.id.pickLocationButton);
        clearButton = addressComponent.findViewById(R.id.clearButton);

        pickLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivityForResult(intent, MAPS_REQUEST_CODE);
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                latAndLong.setText("(Lat, Long)");
            }
        });


        View notesComponent = inflater.inflate(R.layout.component_notes, transactionRecycler, false);
        notesEditText = notesComponent.findViewById(R.id.notesEditText);

        advancedIsClicked = false;
        advancedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                inputAdapter.advancedFeature(advancedIsClicked,
                        addressComponent,
                        notesComponent
                );
                advancedIsClicked = !advancedIsClicked;
            }
        });

        db = (new SQLiteControllerHelper(getContext(), "transactions.db", null, 1)).getWritableDatabase();

        if (getArguments() != null && getArguments().containsKey("transaction")) {
            transactionToEdit = getArguments().getParcelable("transaction");
            if (transactionToEdit != null) {
                transactionIdToEdit = transactionToEdit.getTransactionId();
                populateFieldsForEdit(transactionToEdit);
            }
        }

        return recyclerViewConstraint;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MAPS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                double latitude = data.getDoubleExtra("latitude", 0);
                double longitude = data.getDoubleExtra("longitude", 0);
                this.FinalLatitude = latitude;
                this.FinalLongitude = longitude;
                if (!Double.isNaN( this.FinalLatitude) && !Double.isNaN( this.FinalLongitude)) {
                    String temp = "("+String.valueOf(latitude)+", "+String.valueOf(longitude)+")";
                    latAndLong.setText(temp);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putString("numberDecimal", numberDecimal.getText().toString());
        outState.putString("dateHolder", dateHolder.getText().toString());
        outState.putString("latAndLong", latAndLong.getText().toString());
        outState.putString("notesEditText", notesEditText.getText().toString());
        outState.putString("displayCategoryClicked", displayCategoryClicked.getText().toString());
        outState.putString("displayPaymentClicked", displayPaymentClicked.getText().toString());
    }

    @Override
    public void onViewStateRestored(@NonNull Bundle savedInstanceState) {

        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState == null) {
            return;
        }
        numberDecimal.setText(savedInstanceState.getString("numberDecimal"));
        dateHolder.setText(savedInstanceState.getString("dateHolder"));
        latAndLong.setText(savedInstanceState.getString("latAndLong"));
        notesEditText.setText(savedInstanceState.getString("notesEditText"));
        displayCategoryClicked.setText(savedInstanceState.getString("displayCategoryClicked"));
        displayPaymentClicked.setText(savedInstanceState.getString("displayPaymentClicked"));
        advancedIsClicked = false;
    }

    private List<View> createComponents(LayoutInflater inflater) {

        List<View> viewList = new ArrayList<View>();

        viewList.add(createAmountComponent(inflater));
        viewList.add(createDateComponent(inflater));
        viewList.add(createCategoryComponent(inflater));
        viewList.add(createPaymentComponent(inflater));
        viewList.add(createAdvancedComponent(inflater));
        viewList.add(createSubmitComponent(inflater));

        return viewList;
    }

    private View createAmountComponent(LayoutInflater inflater) {

        View amount = inflater.inflate(R.layout.component_amount, transactionRecycler, false);
        numberDecimal = amount.findViewById(R.id.numberDecimal);

        return amount;
    }

    private View createDateComponent(LayoutInflater inflater) {

        View date = inflater.inflate(R.layout.component_date, transactionRecycler, false);
        dateHolder = date.findViewById(R.id.dateHolder);

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().build();
        datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long aLong) {

                LocalDate localDate = Instant.ofEpochMilli(aLong + 86400000L).atZone(ZoneId.systemDefault()).toLocalDate();

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

                TextView dateHolder = date.findViewById(R.id.dateHolder);
                dateHolder.setText(localDate.format(dateFormatter));

            }
        });
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        date.findViewById(R.id.calendarPicker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                datePicker.show(fragmentManager, "DATE_PICKER");
            }
        });

        return date;
    }

    private View createCategoryComponent(LayoutInflater inflater) {

        View category = inflater.inflate(R.layout.component_category, transactionRecycler, false);

        displayCategoryClicked = category.findViewById(R.id.displayCategoryClicked);
        chipGroupExpenseCategories = category.findViewById(R.id.chipGroupExpenseCategories);
        loadCategories();

        return category;
    }

    private void loadCategories() {

        Cursor cursor = db.query(EXPENSE_CATEGORIES_TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            addChipToGroup(chipGroupExpenseCategories, category, displayCategoryClicked);
        }
        cursor.close();
    }

    private View createPaymentComponent(LayoutInflater inflater) {

        View payment = inflater.inflate(R.layout.component_payment, transactionRecycler, false);
        displayPaymentClicked = payment.findViewById(R.id.displayPaymentClicked);
        chipGroupPaymentMethods = payment.findViewById(R.id.chipGroupPaymentMethods);
        loadPaymentMethods();

        return payment;
    }

    private void loadPaymentMethods() {
        Cursor cursor = db.query(PAYMENT_METHODS_TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String method = cursor.getString(cursor.getColumnIndexOrThrow("payment"));
            addChipToGroup(chipGroupPaymentMethods, method, displayPaymentClicked);
        }
        cursor.close();
    }

    private void addChipToGroup(ChipGroup chipGroup, String text, TextView view) {

        Chip chip = new Chip(getContext());
        chip.setText(text);
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                view.setText(text);
            }
        });

        chipGroup.addView(chip);
    }

    private View createAdvancedComponent(LayoutInflater inflater) {

        View advanced = inflater.inflate(R.layout.component_advanced, transactionRecycler, false);
        advancedButton = advanced.findViewById(R.id.advanced);

        return advanced;
    }

    private View createSubmitComponent(LayoutInflater inflater) {

        View submit = inflater.inflate(R.layout.component_submit, transactionRecycler, false);
        submitButton = submit.findViewById(R.id.submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (allFieldsCorrect()) {

                    ContentValues tableInsertData = new ContentValues();
                    tableInsertData.put("amount", numberDecimal.getText().toString());
                    tableInsertData.put("date", dateHolder.getText().toString());
                    tableInsertData.put("category", displayCategoryClicked.getText().toString());
                    tableInsertData.put("payment", displayPaymentClicked.getText().toString());

                    if (transactionIdToEdit != -1) {

                        db.update(SQLiteControllerHelper.getTransactionInfo(), tableInsertData, "transaction_ID = ?", new String[]{String.valueOf(transactionIdToEdit)});
                        Toast.makeText(getContext(), "Transaction updated!", Toast.LENGTH_SHORT).show();
                    } else {

                        long autoIncrementId = db.insert(SQLiteControllerHelper.getTransactionInfo(), null, tableInsertData);


                        String loc = latAndLong.getText().toString();
                        if (!loc.equals("(Lat, Long)")) {
                            ContentValues locationData = new ContentValues();
                            locationData.put("transaction_ID", autoIncrementId);
                            if (!Double.isNaN(FinalLatitude) && !Double.isNaN(FinalLongitude)) {
                                locationData.put("latitude", FinalLatitude);
                                locationData.put("longitude", FinalLongitude);
                            }
                            db.insert(SQLiteControllerHelper.getLocation(), null, locationData);
                        }


                        String notesText = notesEditText.getText().toString();
                        if (!notesText.matches("^\\s*$")) {
                            ContentValues notesData = new ContentValues();
                            notesData.put("transaction_ID", autoIncrementId);
                            notesData.put("notes", notesText);
                            db.insert(SQLiteControllerHelper.getNotes(), null, notesData);
                        }

                        Toast.makeText(getContext(), "Transaction created!", Toast.LENGTH_SHORT).show();
                    }


                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, new HomeFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }
        });

        return submit;
    }

        private boolean allFieldsCorrect() {
        return isValidAmount()
                && isValidDate()
//                && isValidCategory()
                && isValidPayment();
    }

    private boolean isValidAmount() {
        String inputtedAmount = numberDecimal.getText().toString();

        try {
            Double.parseDouble(inputtedAmount);
            return true;
        } catch (NumberFormatException e) {
            showToast("Amount must either be a valid integer or floating point number!");
        }

        return false;
    }

    private boolean isValidDate() {
        String inputtedDate = dateHolder.getText().toString();
        if (inputtedDate.equals("__/__/____")) {
            showToast("You must add the transaction date!");
            return false;
        }

        return true;
    }

    private boolean isValidCategory() {

        if (displayCategoryClicked.getText().toString().equals("None")) {

            showToast("You must pick a category!");
            return false;
        }

        return true;
    }

    private boolean isValidPayment() {

        if (displayPaymentClicked.getText().toString().equals("None")) {

            showToast("You must pick a payment method!");
            return false;
        }

        return true;
    }

    private void showToast(String msg) {
        Toast.makeText(TransactionFragment.this.getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}