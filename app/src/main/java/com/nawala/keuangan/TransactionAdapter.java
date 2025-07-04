package com.nawala.keuangan;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TRANSACTION = 0;
    private static final int VIEW_TYPE_HEADER = 1;

    private List<Object> itemList;
    private Context context;

    public TransactionAdapter(List<Object> itemList) {
        this.itemList = itemList;
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position) instanceof Transaction) {
            return VIEW_TYPE_TRANSACTION;
        } else {
            return VIEW_TYPE_HEADER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == VIEW_TYPE_HEADER) {
            View view = inflater.inflate(R.layout.list_item_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.list_item_transaction, parent, false);
            return new TransactionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
            DateHeaderViewHolder headerHolder = (DateHeaderViewHolder) holder;
            headerHolder.tvDateHeader.setText((String) itemList.get(position));
        } else {
            TransactionViewHolder transactionHolder = (TransactionViewHolder) holder;
            Transaction transaction = (Transaction) itemList.get(position);

            transactionHolder.tvCategory.setText(transaction.category);
            transactionHolder.tvDescription.setText(transaction.description);

            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
            format.setMaximumFractionDigits(0);
            String formattedAmount = format.format(transaction.amount);

            if ("income".equals(transaction.type)) {
                transactionHolder.tvAmount.setText("+ " + formattedAmount);
                transactionHolder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.color_income));
            } else {
                transactionHolder.tvAmount.setText("- " + formattedAmount);
                transactionHolder.tvAmount.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            }

            transactionHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditTransaksiActivity.class);
                intent.putExtra(EditTransaksiActivity.EXTRA_TRANSACTION_ID, transaction.id);
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void setItemList(List<Object> newItemList) {
        this.itemList = newItemList;
        notifyDataSetChanged();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDescription, tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }

    public static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateHeader;

        public DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
        }
    }
}
