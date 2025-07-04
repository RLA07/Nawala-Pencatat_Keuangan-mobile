package com.nawala.keuangan;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC")
    List<Transaction> getAll();

    @Query("SELECT * FROM transactions WHERE id = :id")
    Transaction getById(int id);

    @Insert
    void insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);
}
