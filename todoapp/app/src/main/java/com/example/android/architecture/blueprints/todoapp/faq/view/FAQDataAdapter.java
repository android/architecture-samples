package com.example.android.architecture.blueprints.todoapp.faq.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.faq.model.FAQModel;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Sa7r on 6/14/2017.
 */

public class FAQDataAdapter  extends BaseAdapter {

    private List<FAQModel> faqData;
    private FAQListItemListener mItemListener;

    public FAQDataAdapter(List<FAQModel> faqData, FAQListItemListener listner) {
        setList(faqData);
        this.mItemListener=listner;
    }

    public void replaceData(List<FAQModel> tasks) {
        setList(tasks);
        notifyDataSetChanged();
    }

    private void setList(List<FAQModel> tasks) {
        faqData = checkNotNull(tasks);
    }

    @Override
    public int getCount() {
        return faqData.size();
    }

    @Override
    public FAQModel getItem(int i) {
        return faqData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = view;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            rowView = inflater.inflate(R.layout.faq_item, viewGroup, false);
        }

        final FAQModel faqItem = getItem(i);

        TextView titleTV = (TextView) rowView.findViewById(R.id.tvQuestion);
        titleTV.setText(faqItem.getTitle());
        TextView answerTV = (TextView) rowView.findViewById(R.id.tvAnswer);
        answerTV.setText(faqItem.getBody());
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemListener.onFAQItemClick(faqItem);
            }
        });

        return rowView;
    }
}
