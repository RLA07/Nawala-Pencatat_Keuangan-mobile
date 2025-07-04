package com.nawala.keuangan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout; // << GANTI DARI CARDVIEW
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private TransactionAdapter adapter;
    private TextView tvSaldo, tvIncome, tvExpense;
    private CardView cvSaldo;
    // --- PERBAIKAN 1: Ubah tipe data dari CardView menjadi LinearLayout
    private LinearLayout llMonthlySummary;

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    // --- PERBAIKAN 2: Hapus anotasi @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getDatabase(getApplicationContext());

        tvSaldo = findViewById(R.id.tvSaldo);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        cvSaldo = findViewById(R.id.cvSaldo);
        llMonthlySummary = findViewById(R.id.llMonthlySummary); // Sekarang tipenya sudah benar
        RecyclerView recyclerView = findViewById(R.id.recyclerViewTransactions);
        FloatingActionButton fab = findViewById(R.id.fabTambah);

        adapter = new TransactionAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, TambahTransaksiActivity.class);
            startActivity(intent);
        });

        // Kedua listener ini sekarang aman dan menuju ke tujuan yang sama
        cvSaldo.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, DetailSaldoActivity.class);
            startActivity(intent);
        });

        llMonthlySummary.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, DetailSaldoActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions();
    }

    private void loadTransactions() {
        databaseExecutor.execute(() -> {
            final List<Transaction> results = db.transactionDao().getAll();

            // Pengurutan tetap di sini
            results.sort((t1, t2) -> Long.compare(t2.transactionDate, t1.transactionDate));

            List<Object> groupedList = new ArrayList<>();
            String lastDateHeader = "";
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("in", "ID"));

            for (Transaction transaction : results) {
                String currentDateHeader = sdf.format(transaction.transactionDate);
                if (!currentDateHeader.equals(lastDateHeader)) {
                    groupedList.add(currentDateHeader);
                    lastDateHeader = currentDateHeader;
                }
                groupedList.add(transaction);
            }

            mainThreadHandler.post(() -> {
                adapter.setItemList(groupedList);
                adapter.notifyDataSetChanged();
                // --- PERBAIKAN 3: Kirim data langsung sebagai parameter
                updateSummary(results);
            });
        });
    }

    // --- PERBAIKAN 4: Metode ini sekarang menerima List sebagai parameter
    private void updateSummary(List<Transaction> transactionList) {
        double totalIncome = 0;
        double totalExpense = 0;
        double monthlyIncome = 0;
        double monthlyExpense = 0;

        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        for (Transaction t : transactionList) { // Gunakan parameter, bukan variabel kelas
            cal.setTimeInMillis(t.transactionDate);
            int transactionMonth = cal.get(Calendar.MONTH);
            int transactionYear = cal.get(Calendar.YEAR);

            if ("income".equals(t.type)) {
                totalIncome += t.amount;
                if (transactionMonth == currentMonth && transactionYear == currentYear) {
                    monthlyIncome += t.amount;
                }
            } else {
                totalExpense += t.amount;
                if (transactionMonth == currentMonth && transactionYear == currentYear) {
                    monthlyExpense += t.amount;
                }
            }
        }

        double balance = totalIncome - totalExpense;

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        format.setMaximumFractionDigits(0);

        tvSaldo.setText(format.format(balance));
        tvIncome.setText(format.format(monthlyIncome));
        tvExpense.setText(format.format(monthlyExpense));
    }
}