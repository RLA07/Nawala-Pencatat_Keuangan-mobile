package com.nawala.keuangan;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailSaldoActivity extends AppCompatActivity {

    private AppDatabase db;
    private TextView tvDetailSaldo, tvMonthlyIncome, tvMonthlyExpense, tvWeeklyIncome, tvWeeklyExpense, tvBankBalance, tvWalletBalance;

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    // Variabel untuk menyimpan tanggal pilihan untuk ekspor PDF
    private long selectedStartDate;
    private long selectedEndDate;

    // --- DEKLARASI SEMUA ACTIVITY RESULT LAUNCHER ---

    // Launcher untuk menyimpan file PDF
    private final ActivityResultLauncher<Intent> exportPdfLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        generatePdf(uri, selectedStartDate, selectedEndDate);
                    }
                }
            }
    );

    // Launcher untuk menyimpan file CSV
    private final ActivityResultLauncher<Intent> exportCsvLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        writeDataToFile(uri);
                    }
                }
            }
    );

    // Launcher untuk membuka file CSV
    private final ActivityResultLauncher<Intent> importCsvLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        readDataFromFile(uri);
                    }
                }
            }
    );


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

        // Inisialisasi semua listener tombol
        Button btnExportCsv = findViewById(R.id.btnExportCsv);
        btnExportCsv.setOnClickListener(v -> launchExportCsvPicker());

        Button btnImportCsv = findViewById(R.id.btnImportCsv);
        btnImportCsv.setOnClickListener(v -> launchImportCsvPicker());

        Button btnDeleteAll = findViewById(R.id.btnDeleteAll);
        btnDeleteAll.setOnClickListener(v -> showDeleteConfirmationDialog());

        Button btnExportPdf = findViewById(R.id.btnExportPdf);
        btnExportPdf.setOnClickListener(v -> showDateRangePickerDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Muat ulang data setiap kali kembali ke activity ini
        loadAndCalculateData();
    }

    // --- BAGIAN KALKULASI & TAMPILAN SALDO ---

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

        tvDetailSaldo.setText(formatCurrency(totalBalance));
        tvMonthlyIncome.setText(formatCurrency(monthlyIncome));
        tvMonthlyExpense.setText(formatCurrency(monthlyExpense));
        tvWeeklyIncome.setText(formatCurrency(weeklyIncome));
        tvWeeklyExpense.setText(formatCurrency(weeklyExpense));
        tvBankBalance.setText(formatCurrency(bankBalance));
        tvWalletBalance.setText(formatCurrency(walletBalance));
    }

    // --- BAGIAN EKSPOR CSV ---

    private void launchExportCsvPicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "nawala_backup_" + System.currentTimeMillis() + ".csv");
        exportCsvLauncher.launch(intent);
    }

    private void writeDataToFile(Uri uri) {
        databaseExecutor.execute(() -> {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri);
                 OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                List<Transaction> transactions = db.transactionDao().getAll();
                StringBuilder csvBuilder = new StringBuilder();
                csvBuilder.append("id,type,amount,category,description,transactionDate,source\n");
                for (Transaction t : transactions) {
                    String safeDescription = "\"" + t.description.replace("\"", "\"\"") + "\"";
                    csvBuilder.append(t.id).append(",");
                    csvBuilder.append(t.type).append(",");
                    csvBuilder.append(t.amount).append(",");
                    csvBuilder.append(t.category).append(",");
                    csvBuilder.append(safeDescription).append(",");
                    csvBuilder.append(t.transactionDate).append(",");
                    csvBuilder.append(t.source).append("\n");
                }
                writer.write(csvBuilder.toString());
                runOnUiThread(() -> Toast.makeText(this, "Ekspor data berhasil!", Toast.LENGTH_LONG).show());
            } catch (IOException e) {
                Log.e("ExportCSV", "Gagal menulis file CSV", e);
                runOnUiThread(() -> Toast.makeText(this, "Ekspor data gagal!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // --- BAGIAN IMPOR CSV ---

    private void launchImportCsvPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"text/csv", "text/comma-separated-values"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        importCsvLauncher.launch(intent);
    }

    private void readDataFromFile(Uri uri) {
        databaseExecutor.execute(() -> {
            List<Transaction> importedTransactions = new ArrayList<>();
            try (InputStream inputStream = getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                reader.readLine(); // Lewati header
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.split(",");
                    if (tokens.length >= 7) {
                        try {
                            Transaction transaction = new Transaction();
                            transaction.type = tokens[1].trim();
                            transaction.amount = Double.parseDouble(tokens[2].trim());
                            transaction.category = tokens[3].trim();
                            transaction.description = tokens[4].trim().replaceAll("^\"|\"$", "");
                            transaction.transactionDate = Long.parseLong(tokens[5].trim());
                            transaction.source = tokens[6].trim();
                            importedTransactions.add(transaction);
                        } catch (Exception e) {
                            Log.e("ImportCSV", "Format baris salah: " + line, e);
                        }
                    }
                }
                if (!importedTransactions.isEmpty()) {
                    db.runInTransaction(() -> {
                        for (Transaction t : importedTransactions) {
                            db.transactionDao().insert(t);
                        }
                    });
                    runOnUiThread(() -> Toast.makeText(this, importedTransactions.size() + " data berhasil diimpor!", Toast.LENGTH_LONG).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Tidak ada data untuk diimpor atau format file salah.", Toast.LENGTH_LONG).show());
                }
            } catch (IOException e) {
                Log.e("ImportCSV", "Gagal membaca file CSV", e);
                runOnUiThread(() -> Toast.makeText(this, "Impor data gagal!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // --- BAGIAN HAPUS SEMUA DATA ---

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Hapus")
                .setMessage("Apakah Anda yakin ingin menghapus SEMUA data transaksi? Aksi ini tidak dapat dibatalkan.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Ya, Hapus Semua", (dialog, whichButton) -> deleteAllData())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteAllData() {
        databaseExecutor.execute(() -> {
            db.transactionDao().deleteAll();
            runOnUiThread(() -> {
                Toast.makeText(DetailSaldoActivity.this, "Semua data berhasil dihapus.", Toast.LENGTH_LONG).show();
                finish();
            });
        });
    }

    // --- BAGIAN EKSPOR PDF ---

    private void showDateRangePickerDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_date_range_picker, null);
        TextView tvStartDate = dialogView.findViewById(R.id.tvStartDate);
        TextView tvEndDate = dialogView.findViewById(R.id.tvEndDate);

        Calendar cal = Calendar.getInstance();
        selectedEndDate = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, -1);
        selectedStartDate = cal.getTimeInMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvStartDate.setText(sdf.format(new Date(selectedStartDate)));
        tvEndDate.setText(sdf.format(new Date(selectedEndDate)));

        tvStartDate.setOnClickListener(v -> showDatePicker(true, tvStartDate));
        tvEndDate.setOnClickListener(v -> showDatePicker(false, tvEndDate));

        new AlertDialog.Builder(this)
                .setTitle("Pilih Rentang Tanggal Laporan")
                .setView(dialogView)
                .setPositiveButton("Buat PDF", (dialog, which) -> launchExportPdfPicker())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showDatePicker(boolean isStartDate, TextView dateTextView) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(isStartDate ? selectedStartDate : selectedEndDate);

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, dayOfMonth);
            if (isStartDate) {
                selectedStartDate = newDate.getTimeInMillis();
            } else {
                selectedEndDate = newDate.getTimeInMillis();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateTextView.setText(sdf.format(newDate.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void launchExportPdfPicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "laporan_keuangan_" + System.currentTimeMillis() + ".pdf");
        exportPdfLauncher.launch(intent);
    }

    private void generatePdf(Uri uri, long startDate, long endDate) {
        databaseExecutor.execute(() -> {
            List<Transaction> transactions = db.transactionDao().getTransactionsBetweenDates(startDate, endDate);
            try {
                PdfWriter writer = new PdfWriter(getContentResolver().openOutputStream(uri));
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Konten PDF
                document.add(new Paragraph("Laporan Keuangan Nawala").setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));

                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                String dateRange = sdf.format(new Date(startDate)) + " - " + sdf.format(new Date(endDate));
                document.add(new Paragraph(dateRange).setTextAlignment(TextAlignment.CENTER));

                double totalIncome = transactions.stream().filter(t -> t.type.equals("income")).mapToDouble(t -> t.amount).sum();
                double totalExpense = transactions.stream().filter(t -> t.type.equals("expense")).mapToDouble(t -> t.amount).sum();
                document.add(new Paragraph("\nRingkasan:").setBold());
                document.add(new Paragraph("Total Pemasukan: " + formatCurrency(totalIncome)));
                document.add(new Paragraph("Total Pengeluaran: " + formatCurrency(totalExpense)));
                document.add(new Paragraph("Selisih: " + formatCurrency(totalIncome - totalExpense)));

                document.add(new Paragraph("\nDetail Transaksi:").setBold());
                Table table = new Table(UnitValue.createPercentArray(new float[]{3, 3, 5, 3, 3}));
                table.setWidth(UnitValue.createPercentValue(100));

                table.addHeaderCell(new Cell().add(new Paragraph("Tanggal").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Kategori").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Deskripsi").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Pemasukan").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Pengeluaran").setBold()));

                for (Transaction t : transactions) {
                    table.addCell(sdf.format(new Date(t.transactionDate)));
                    table.addCell(t.category);
                    table.addCell(t.description);
                    if (t.type.equals("income")) {
                        table.addCell(formatCurrency(t.amount));
                        table.addCell("");
                    } else {
                        table.addCell("");
                        table.addCell(formatCurrency(t.amount));
                    }
                }
                document.add(table);

                document.close();
                runOnUiThread(() -> Toast.makeText(this, "PDF berhasil dibuat!", Toast.LENGTH_LONG).show());

            } catch (FileNotFoundException e) {
                Log.e("GeneratePDF", "File tidak ditemukan", e);
                runOnUiThread(() -> Toast.makeText(this, "Gagal membuat PDF!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // --- METODE HELPER ---

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        format.setMaximumFractionDigits(0);
        return format.format(amount);
    }
}
