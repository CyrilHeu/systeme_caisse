package com.example.tablettegourmande.ui.gestion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tablettegourmande.R;

public class ColorSpinnerAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final String[] colors;
    private final int[] colorValues;

    public ColorSpinnerAdapter(Context context, String[] colors, int[] colorValues) {
        super(context, R.layout.custom_spinner_color_item, colors);
        this.context = context;
        this.colors = colors;
        this.colorValues = colorValues;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createColorView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createColorView(position, convertView, parent);
    }

    private View createColorView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.custom_spinner_color_item, parent, false);
        }

        View colorPreview = convertView.findViewById(R.id.colorPreview);
        TextView colorName = convertView.findViewById(R.id.colorName);

        colorName.setText(colors[position]);

        if (position == 0) { // "Défaut"
            colorPreview.setVisibility(View.GONE);
        } else {
            colorPreview.setVisibility(View.VISIBLE);
            colorPreview.setBackgroundColor(context.getResources().getColor(colorValues[position]));
        }

        return convertView;
    }
}
