package com.example.mahmoud.newtask.adapter;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mahmoud.newtask.R;

import static com.example.mahmoud.newtask.data.MessageContract.MessageEntry.COLUMN_MESSAGE;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.GuestViewHolder>
{
    private Cursor mCursor;
    private Context mContext;

   public MessageAdapter(Context context, Cursor cursor)
    {
        this.mContext = context;
        this.mCursor = cursor;
    }

    @Override
    public GuestViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_list, parent, false);
        return new GuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GuestViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position))
            return;

        String message = mCursor.getString(mCursor.getColumnIndex(COLUMN_MESSAGE));
        holder.tvMessage.setText(message);
    }

    @Override
    public int getItemCount() {

        return mCursor.getCount();
    }


    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) mCursor.close();
        mCursor = newCursor;
        if (newCursor != null) {
            this.notifyDataSetChanged();
        }
    }


    class GuestViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;


        public GuestViewHolder(View itemView) {
            super(itemView);
            tvMessage = (TextView) itemView.findViewById(R.id.tv_message_item);
        }

    }
}