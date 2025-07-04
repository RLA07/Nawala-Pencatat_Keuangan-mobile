package com.nawala.keuangan;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TambahTransaksiActivity extends AppCompatActivity {

    private EditText etAmount, etDescription;
    private TextView tvDate;
    private Spinner spinnerCategory;
    private RadioGroup rgType, rgSource;
    private Button btnSimpan, btnBatal;
    private AppDatabase db;

    private long selectedDateMillis;
    private final Calendar calendar = Calendar.getInstance();

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_transaksi);

        db = AppDatabase.getDatabase(getApplicationContext());

        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        tvDate = findViewById(R.id.tvDate); // Inisialisasi TextView untuk tanggal
        spinnerCategory = findViewById(R.id.spinnerCategory);
        rgType = findViewById(R.id.rgType);
        rgSource = findViewById(R.id.rgSource);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBatal = findViewById(R.id.btnBatal); // Inisialisasi tombol Batal

        // Set tanggal saat ini sebagai default
        selectedDateMillis = System.currentTimeMillis();
        updateDateEditText();

        tvDate.setOnClickListener(v -> showDatePickerDialog());
        btnSimpan.setOnClickListener(v -> saveTransaction());
        btnBatal.setOnClickListener(v -> finish()); // Menambahkan listener untuk tombol Batal
    }

    private void showDatePickerDialog() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            selectedDateMillis = calendar.getTimeInMillis();
            updateDateEditText();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateEditText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvDate.setText(sdf.format(selectedDateMillis));
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString();
        String description = etDescription.getText().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        int selectedTypeId = rgType.getCheckedRadioButtonId();
        int selectedSourceId = rgSource.getCheckedRadioButtonId();

        if (amountStr.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Harap isi jumlah dan deskripsi", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String type = selectedTypeId == R.id.rbIncome ? "income" : "expense";
        String source = selectedSourceId == R.id.rbBank ? "Bank" : "Dompet";

        Transaction transaction = new Transaction();
        transaction.amount = amount;
        transaction.description = description;
        transaction.category = category;
        transaction.type = type;
        transaction.source = source;
        transaction.transactionDate = selectedDateMillis; // Gunakan tanggal yang dipilih

        databaseExecutor.execute(() -> {
            db.transactionDao().insert(transaction);
            mainThreadHandler.post(() -> {
                Toast.makeText(this, "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show();
                finish(); // Kembali ke MainActivity
            });
        });
    }
}
