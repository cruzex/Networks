package com.example.android.wifidirect;


/**
 * Created by Deepak on 09-04-2017.
 */

        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.TextView;


public class CustomAdapter extends ArrayAdapter<String> {

    public CustomAdapter(Context context, String[] Subjects) {
        super(context, R.layout.list_view,Subjects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater customViewInflator = LayoutInflater.from(getContext());
        View customView = customViewInflator.inflate(R.layout.list_view, parent, false);
        String subject = getItem(position);
        TextView textView = (TextView)customView.findViewById(R.id.textView);
        textView.setText(subject);
        return customView;
    }
}
