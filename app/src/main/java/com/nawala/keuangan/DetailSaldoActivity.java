package com.nawala.keuangan;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailSaldoActivity extends AppCompatActivity {

    private AppDatabase db;
    private TextView tvDetailSaldo, tvMonthlyIncome, tvMonthlyExpense, tvWeeklyIncome, tvWeeklyExpense, tvBankBalance, tvWalletBalance;

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_saldo);

        db = AppDatabase.getDatabase(getApplicationContext());

        // Inisialisasi semua TextView
        tvDetailSaldo = findViewById(R.id.tvDetailSaldo);
        tvMonthlyIncome = findViewById(R.id.tvMonthlyIncome);
        tvMonthlyExpense = findViewById(R.id.tvMonthlyExpense);
        tvWeeklyIncome = findViewById(R.id.tvWeeklyIncome);
        tvWeeklyExpense = findViewById(R.id.tvWeeklyExpense);
        tvBankBalance = findViewById(R.id.tvBankBalance);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);

        loadAndCalculateData();
    }

    private void loadAndCalculateData() {
        databaseExecutor.execute(() -> {
            List<Transaction> allTransactions = db.transactionDao().getAll();
            runOnUiThread(() -> calculateAndDisplaySummaries(allTransactions));
        });
    }

    private void calculateAndDisplaySummaries(List<Transaction> transactions) {
        double totalIncome = 0, totalExpense = 0;
        double monthlyIncome = 0, monthlyExpense = 0;
        double weeklyIncome = 0, weeklyExpense = 0;
        double bankIncome = 0, bankExpense = 0;
        double walletIncome = 0, walletExpense = 0;

        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH);
        int currentYear = now.get(Calendar.YEAR);
        int currentWeek = now.get(Calendar.WEEK_OF_YEAR);

        for (Transaction t : transactions) {
            Calendar transCal = Calendar.getInstance();
            transCal.setTimeInMillis(t.transactionDate);
            int transMonth = transCal.get(Calendar.MONTH);
            int transYear = transCal.get(Calendar.YEAR);
            int transWeek = transCal.get(Calendar.WEEK_OF_YEAR);

            boolean isIncome = "income".equals(t.type);
            double amount = t.amount;

            if (isIncome) {
                totalIncome += amount;
                if (transYear == currentYear && transMonth == currentMonth) monthlyIncome += amount;
                if (transYear == currentYear && transWeek == currentWeek) weeklyIncome += amount;
                if ("Bank".equals(t.source)) bankIncome += amount;
                if ("Dompet".equals(t.source)) walletIncome += amount;
            } else {
                totalExpense += amount;
                if (transYear == currentYear && transMonth == currentMonth) monthlyExpense += amount;
                if (transYear == currentYear && transWeek == currentWeek) weeklyExpense += amount;
                if ("Bank".equals(t.source)) bankExpense += amount;
                if ("Dompet".equals(t.source)) walletExpense += amount;
            }
        }

        double totalBalance = totalIncome - totalExpense;
        double bankBalance = bankIncome - bankExpense;
        double walletBalance = walletIncome - walletExpense;

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        format.setMaximumFractionDigits(0);

        tvDetailSaldo.setText(format.format(totalBalance));
        tvMonthlyIncome.setText(format.format(monthlyIncome));
        tvMonthlyExpense.setText(format.format(monthlyExpense));
        tvWeeklyIncome.setText(format.format(weeklyIncome));
        tvWeeklyExpense.setText(format.format(weeklyExpense));
        tvBankBalance.setText(format.format(bankBalance));
        tvWalletBalance.setText(format.format(walletBalance));
    }
}
