package com.example.schoolish;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteTvAdapter extends ArrayAdapter<SearchItem> {
    private List<SearchItem> schoolListFull;
    public AutoCompleteTvAdapter(@NonNull Context context, @NonNull List<SearchItem> schoolItemList) {
        super(context, 0, schoolItemList);
        schoolListFull = new ArrayList(schoolItemList);

    }

    @NonNull
    @Override
    public Filter getFilter() {
        return schoolFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView==null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.school_search_item,parent,false
            );
        }
        TextView textViewSchoolName = convertView.findViewById(R.id.searchSchoolName);
        SearchItem searchItem = getItem(position);
        if (searchItem!=null)
        {
            textViewSchoolName.setText(searchItem.getSchoolName());
        }
        return convertView;
    }

    private Filter schoolFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            List<SearchItem> suggestions  = new ArrayList<>();
            if (constraint == null || constraint.length()==0)
            {
                suggestions.addAll(schoolListFull);

            } else
            {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (SearchItem item : schoolListFull)
                    {
                        if (item.getSchoolName().toLowerCase().contains(filterPattern))
                        {
                            suggestions.add(item);
                        }

                    }
            }
            filterResults.values = suggestions ;
            filterResults.count = suggestions.size();
            return  filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((SearchItem)resultValue).getSchoolName();
        }
    };
}
