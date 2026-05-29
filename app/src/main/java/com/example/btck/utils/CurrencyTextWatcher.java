package com.example.btck.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyTextWatcher implements TextWatcher {
    private final EditText editText;
    private String current = "";

    public CurrencyTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        if (!s.toString().equals(current)) {
            editText.removeTextChangedListener(this);

            // Strip out dots from the input string
            String cleanString = s.toString().replaceAll("[.]", "");

            if (!cleanString.isEmpty()) {
                try {
                    long parsed = Long.parseLong(cleanString);
                    // Use a DecimalFormat with dot as group separator
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                    symbols.setGroupingSeparator('.');
                    DecimalFormat formatter = new DecimalFormat("#,###", symbols);
                    String formatted = formatter.format(parsed);

                    current = formatted;
                    editText.setText(formatted);
                    editText.setSelection(formatted.length());
                } catch (NumberFormatException e) {
                    // Ignore
                }
            } else {
                current = "";
                editText.setText("");
            }

            editText.addTextChangedListener(this);
        }
    }
}
