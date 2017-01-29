package com.example.android.architecture.blueprints.todoapp.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.R;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapter for the list of tasks.
 */
final class TasksAdapter extends BaseAdapter {

    private List<TaskItem> mTasks;

    public TasksAdapter(List<TaskItem> tasks) {
        setList(tasks);
    }

    public void replaceData(List<TaskItem> tasks) {
        setList(tasks);
        notifyDataSetChanged();
    }

    private void setList(List<TaskItem> tasks) {
        mTasks = checkNotNull(tasks);
    }

    @Override
    public int getCount() {
        return mTasks.size();
    }

    @Override
    public TaskItem getItem(int i) {
        return mTasks.get(i);
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
            rowView = inflater.inflate(R.layout.task_item, viewGroup, false);
        }

        final TaskItem taskItem = getItem(i);

        TextView titleTV = (TextView) rowView.findViewById(R.id.title);
        titleTV.setText(taskItem.getTask().getTitleForList());

        CheckBox completeCB = (CheckBox) rowView.findViewById(R.id.complete);
        // Active/completed task UI
        completeCB.setChecked(taskItem.getTask().isCompleted());
        completeCB.setOnCheckedChangeListener(
                (buttonView, isChecked) -> taskItem.getOnCheckAction().call(isChecked));

        rowView.setBackgroundResource(taskItem.getBackground());
        rowView.setOnClickListener(__ -> taskItem.getOnClickAction().call());

        return rowView;
    }
}