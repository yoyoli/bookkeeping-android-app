package edu.northeastern.finalproject_group12;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SQLiteControllerHelper extends SQLiteOpenHelper {

    private static final String transactionInfo = "transactionInfo";
    private static final String location = "location";
    private static final String notes = "notes";
    private static final String user = "user";
    private static final String expenseCategories = "expenseCategories";
    private static final String paymentMethods = "paymentMethods";

    public SQLiteControllerHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {

        super(context, name, factory, version+1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(

                "CREATE TABLE " + transactionInfo + " (" +

                        "transaction_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "amount REAL NOT NULL, " +
                        "date TEXT NOT NULL, " +
                        "category TEXT NOT NULL, " +
                        "payment TEXT NOT NULL" +
                        ")"
        );

        db.execSQL(

                "CREATE TABLE " + location + " (" +

                        "transaction_ID INTEGER NOT NULL, " +
                        "latitude INTEGER NOT NULL, " +
                        "longitude INTEGER NOT NULL" +
                        ")"
        );

        db.execSQL(

                "CREATE TABLE " + notes + " (" +

                        "transaction_ID INTEGER NOT NULL, " +
                        "notes TEXT NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + user + " (" +
                        "user_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT, " +
                        "email TEXT, " +
                        "monthlyBudget REAL" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + expenseCategories + " (" +
                        "category TEXT PRIMARY KEY NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + paymentMethods + " (" +
                        "payment TEXT PRIMARY KEY NOT NULL" +
                        ")"
        );

        // Insert default values into expenseCategories
        db.execSQL("INSERT INTO " + expenseCategories + " (category) VALUES ('Rent')");
        db.execSQL("INSERT INTO " + expenseCategories + " (category) VALUES ('Food')");
        db.execSQL("INSERT INTO " + expenseCategories + " (category) VALUES ('Travel')");
        db.execSQL("INSERT INTO " + expenseCategories + " (category) VALUES ('Entertainment')");
        // Insert default values into paymentMethods
        db.execSQL("INSERT INTO " + paymentMethods + " (payment) VALUES ('Cash')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + transactionInfo);
        db.execSQL("DROP TABLE IF EXISTS " + location);
        db.execSQL("DROP TABLE IF EXISTS " + notes);
        db.execSQL("DROP TABLE IF EXISTS " + user);
        db.execSQL("DROP TABLE IF EXISTS " + expenseCategories);
        db.execSQL("DROP TABLE IF EXISTS " + paymentMethods);

        onCreate(db);
    }

    public double getTotalExpensesForMonth(int month, int year) {
        double totalExpenses = 0.0;
        SQLiteDatabase db = this.getReadableDatabase();

        String monthString = String.format("%02d", month + 1);
        String dateFilter = monthString + "/%";

        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM " + transactionInfo + " WHERE date LIKE ?", new String[]{dateFilter});
        if (cursor.moveToFirst()) {
            totalExpenses = cursor.getDouble(0);
        }
        cursor.close();
        return totalExpenses;
    }

    public double getMonthlyBudgetForHome() {
        double monthlyBudget = 0.0;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT monthlyBudget FROM " + user, null);
        if (cursor.moveToFirst()) {
            monthlyBudget = cursor.getDouble(0);
        }
        cursor.close();
        return monthlyBudget;
    }

    public void deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(getTransactionInfo(), "transaction_ID = ?", new String[]{String.valueOf(transactionId)});
        db.close();
    }


    public static String getTransactionInfo() {
        return transactionInfo;
    }

    public static String getLocation() {
        return location;
    }

    public static String getNotes() {
        return notes;
    }

    public static String getUser() {
        return user;
    }

    public static String getExpenseCategories() {
        return expenseCategories;
    }

    public static String getPaymentMethods() {
        return paymentMethods;
    }

    public boolean isTableExists(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{tableName}
        );
        boolean tableExists = cursor.getCount() > 0;
        cursor.close();
        return tableExists;
    }

    // calculate for chosen year
    public double getTotalExpenseForYear(String year) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM transactionInfo WHERE substr(date, 7, 4) = ?",
                new String[]{year}
        );
        double total = 0;
        if (cursor.moveToFirst() && cursor.getDouble(0) != 0) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public double getAverageExpenseForYear(String year) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM transactionInfo WHERE substr(date, 7, 4) = ?",
                new String[]{year}
        );
        double total = 0;
        if (cursor.moveToFirst() && cursor.getDouble(0) != 0) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total / 12;
    }

    public double getMaxExpenseForYear(String year) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT substr(date, 1, 2) AS month, SUM(amount) AS monthly_total FROM transactionInfo WHERE substr(date, 7, 4) = ? GROUP BY substr(date, 1, 2)",
                new String[]{year}
        );
        double maxExpense = 0;
        while (cursor.moveToNext()) {
            double monthlyTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("monthly_total"));
            if (monthlyTotal > maxExpense) {
                maxExpense = monthlyTotal;
            }
        }
        cursor.close();
        return maxExpense;
    }

    // Calculate for chosen Month
    public double getTotalExpenseForMonth(String year, String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM transactionInfo WHERE substr(date, 7, 4) = ? AND substr(date, 1, 2) = ?",
                new String[]{year, month}
        );
        double total = 0;
        if (cursor.moveToFirst() && cursor.getDouble(0) != 0) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public double getAverageExpenseForMonth(String year, String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM transactionInfo WHERE substr(date, 7, 4) = ? AND substr(date, 1, 2) = ?",
                new String[]{year, month}
        );
        double total = 0;
        if (cursor.moveToFirst() && cursor.getDouble(0) != 0) {
            total = cursor.getDouble(0);
        }
        cursor.close();

        // calculate days per month
        int yearInt = Integer.parseInt(year);
        int monthInt = Integer.parseInt(month);
        Calendar calendar = Calendar.getInstance();
        calendar.set(yearInt, monthInt - 1, 1);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        return total / daysInMonth;
    }

    public double getMaxDailyExpenseForMonth(String year, String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT substr(date, 4, 2) AS day, SUM(amount) AS daily_total FROM transactionInfo WHERE substr(date, 7, 4) = ? AND substr(date, 1, 2) = ? GROUP BY substr(date, 4, 2)",
                new String[]{year, month}
        );
        double maxExpense = 0;
        while (cursor.moveToNext()) {
            double dailyTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("daily_total"));
            if (dailyTotal > maxExpense) {
                maxExpense = dailyTotal;
            }
        }
        cursor.close();
        return maxExpense;
    }

    // Calculate daily expense
    public double getDailyExpenseForMonth(String year, String month, String day) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM transactionInfo WHERE substr(date, 7, 4) = ? AND substr(date, 1, 2) = ? AND substr(date, 4, 2) = ?",
                new String[]{year, month, day}
        );
        double dailyTotal = 0;
        if (cursor.moveToFirst() && cursor.getDouble(0) != 0) {
            dailyTotal = cursor.getDouble(0);
        }
        cursor.close();
        return dailyTotal;
    }

    // Category
    public List<CategoryExpense> getCategoryExpensesForYear(String year) {
        List<CategoryExpense> categoryExpenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT category, SUM(amount) as total FROM transactionInfo WHERE substr(date, 7, 4) = ? GROUP BY category";
        Cursor cursor = db.rawQuery(query, new String[]{year});

        double totalAmount = getTotalExpenseForYear(year);

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                double percentage = (total / totalAmount) * 100;
                categoryExpenses.add(new CategoryExpense(category, total, percentage));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categoryExpenses;
    }

    public List<CategoryExpense> getCategoryExpensesForMonth(String year, String month) {
        List<CategoryExpense> categoryExpenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT category, SUM(amount) as total FROM transactionInfo WHERE substr(date, 7, 4) = ? AND substr(date, 1, 2) = ? GROUP BY category";
        Cursor cursor = db.rawQuery(query, new String[]{year, month});

        double totalAmount = getTotalExpenseForMonth(year, month);

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                double percentage = (total / totalAmount) * 100;
                categoryExpenses.add(new CategoryExpense(category, total, percentage));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categoryExpenses;
    }

    public double getMonthlyBudget() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT monthlyBudget FROM user LIMIT 1", null);

        double monthlyBudget = 0;
        if (cursor.moveToFirst()) {
            monthlyBudget = cursor.getDouble(cursor.getColumnIndexOrThrow("monthlyBudget"));
        }
        cursor.close();
        return monthlyBudget;
    }

    public List<Location> getFrequentLocations() {
        List<Location> locations = new ArrayList<>();
        String selectQuery = "SELECT latitude, longitude FROM location";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));
                Location location = new Location("");
                location.setLatitude(longitude);
                location.setLongitude(latitude);
                locations.add(location);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return locations;
    }
}

