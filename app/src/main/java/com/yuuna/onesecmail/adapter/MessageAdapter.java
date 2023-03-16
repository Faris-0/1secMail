package com.yuuna.onesecmail.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.yuuna.onesecmail.model.MessageModel;
import com.yuuna.onesecmail.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.Holder> {

    private ArrayList<MessageModel> messageDataArrayList;
    private Context mContext;
    private ItemClickListener clickListener;

    public MessageAdapter(ArrayList<MessageModel> messageModelArrayList, Context context) {
        this.messageDataArrayList = messageModelArrayList;
        this.mContext = context;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mail, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        MessageModel messageModel = messageDataArrayList.get(position);

        char firstCharacter = messageModel.getFrom().charAt(0);
        holder.first.setText(String.valueOf(firstCharacter));
        holder.from.setText(messageModel.getFrom());
        try {
            holder.date.setText(new SimpleDateFormat("dd/MM/yy")
                    .format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(messageModel.getDate())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.subject.setText(messageModel.getSubject());
    }

    @Override
    public int getItemCount() {
        return messageDataArrayList.size();
    }

    public interface ItemClickListener {
        void onClick(MessageModel messageModel, View view, int position);
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public class Holder extends RecyclerView.ViewHolder {
        private TextView first, from, date, subject;

        public Holder(View itemView) {
            super(itemView);
            first = itemView.findViewById(R.id.itemFirstText);
            from = itemView.findViewById(R.id.itemFrom);
            date = itemView.findViewById(R.id.itemDate);
            subject = itemView.findViewById(R.id.itemSubject);
            itemView.setOnClickListener(view -> {
                if (clickListener != null) clickListener.onClick(messageDataArrayList.get(getAdapterPosition()), view, getAdapterPosition());
            });
        }
    }
}
