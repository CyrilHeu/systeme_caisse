package Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tablettegourmande.R;

public class CustomToast {

    public static void show(Context context, String message, int iconResId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        ImageView toastIcon = layout.findViewById(R.id.toast_icon);
        TextView toastMessage = layout.findViewById(R.id.toast_message);

        if(iconResId != 0){
            toastIcon.setImageResource(iconResId);
        }else{
            toastIcon.setVisibility(View.GONE);
        }

        toastMessage.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
