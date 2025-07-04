package com.nawala.keuangan;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
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

public class EditTransaksiActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSACTION_ID = "extra_transaction_id";
    private EditText etAmount, etDescription;
    private TextView tvDate;
    private Spinner spinnerCategory;
    private RadioGroup rgType, rgSource;
    private Button btnUpdate, btnDelete;
    private AppDatabase db;
    private int transactionId;
    private Transaction currentTransaction;

    private long selectedDateMillis;
    private final Calendar calendar = Calendar.getInstance();

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaksi);

        db = AppDatabase.getDatabase(getApplicationContext());
        transactionId = getIntent().getIntExtra(EXTRA_TRANSACTION_ID, -1);

        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        tvDate = findViewById(R.id.tvDate); // Inisialisasi TextView untuk tanggal
        spinnerCategory = findViewById(R.id.spinnerCategory);
        rgType = findViewById(R.id.rgType);
        rgSource = findViewById(R.id.rgSource);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        tvDate.setOnClickListener(v -> showDatePickerDialog());
        btnUpdate.setOnClickListener(v -> updateTransaction());
        btnDelete.setOnClickListener(v -> deleteTransaction());

        loadTransactionData();
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

    private void loadTransactionData() {
        databaseExecutor.execute(() -> {
            currentTransaction = db.transactionDao().getById(transactionId);
            mainThreadHandler.post(this::populateUI);
        });
    }

    private void populateUI() {
        if (currentTransaction != null) {
            double amount = currentTransaction.amount;
                // Cek apakah angka double ini sebenarnya adalah angka bulat
            if (amount == (long) amount) {
                // Jika ya, tampilkan sebagai angka bulat (long)
                etAmount.setText(String.format(Locale.US, "%d", (long) amount));
            } else {
                // Jika tidak (punya desimal), tampilkan apa adanya
                etAmount.setText(String.valueOf(amount));
            }
            etDescription.setText(currentTransaction.description);

            // Set tanggal transaksi
            selectedDateMillis = currentTransaction.transactionDate;
            updateDateEditText();

            // Set spinner selection
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.kategori_transaksi, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(adapter);
            int spinnerPosition = adapter.getPosition(currentTransaction.category);
            spinnerCategory.setSelection(spinnerPosition);

            if ("income".equals(currentTransaction.type)) {
                rgType.check(R.id.rbIncome);
            } else {
                rgType.check(R.id.rbExpense);
            }

            if ("Bank".equals(currentTransaction.source)) {
                rgSource.check(R.id.rbBank);
            } else {
                rgSource.check(R.id.rbDompet);
            }
        }
    }

    private void updateTransaction() {
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

        currentTransaction.amount = amount;
        currentTransaction.description = description;
        currentTransaction.category = category;
        currentTransaction.type = type;
        currentTransaction.source = source;
        currentTransaction.transactionDate = selectedDateMillis; // Gunakan tanggal yang dipilih

        databaseExecutor.execute(() -> {
            db.transactionDao().update(currentTransaction);
            mainThreadHandler.post(() -> {
                Toast.makeText(this, "Transaksi berhasil diperbarui", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void deleteTransaction() {
        databaseExecutor.execute(() -> {
            db.transactionDao().delete(currentTransaction);
            mainThreadHandler.post(() -> {
                Toast.makeText(this, "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
