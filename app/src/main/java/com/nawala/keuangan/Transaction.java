package com.nawala.keuangan;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String type; // "income" atau "expense"
    public double amount;
    public String category;
    public String description;
    public long transactionDate;
    public String source; // "Bank" atau "Dompet"
}
