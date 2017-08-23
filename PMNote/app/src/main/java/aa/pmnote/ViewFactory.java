package aa.pmnote;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by anton.gorielikov on 8/23/2017.
 */

public class ViewFactory {
    public final static int LINEAR_LAYOUT_CHECKBOX_POSITION = 0;

    static public LinearLayout linearLayoutFactory(Context context, String text, Defines.LinearLayoutType llt)
    {
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        ll.setGravity(Gravity.CENTER);
        ll.setLayoutParams(lp);
        ll.setTag(llt);

        ll.addView(imageViewFactory(context, llt));
        ll.addView(textViewFactory(context, text));

        return ll;
    }

    static public LinearLayout linearLayoutFactory(Context context, String text, boolean checkBoxStatus)
    {
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        ll.setGravity(Gravity.CENTER);
        ll.setLayoutParams(lp);

        ll.addView(checkBoxFactory(context, text, checkBoxStatus));
        ll.addView(textViewFactory(context, text));

        return ll;
    }

    static public CheckBox checkBoxFactory(Context context, String text, boolean checked)
    {
        final CheckBox cb = new CheckBox(context);
        cb.setLayoutParams(new ViewGroup.LayoutParams(150, 150));
        cb.setText("");
        cb.setTag(text);
        cb.setChecked(checked);

        return cb;
    }

    static public ImageView imageViewFactory(Context context, Defines.LinearLayoutType llt)
    {
        ImageView iv = new ImageView(context);
        iv.setLayoutParams(new ViewGroup.LayoutParams(150, 150));
        switch(llt)
        {
            case PERSON:
                iv.setImageResource(R.drawable.ic_person);
                break;
            case PROJECT:
                iv.setImageResource(R.drawable.ic_project);
                break;
        }

        iv.setTag(llt);
        iv.setPadding(10, 10, 0, 0);
        return iv;
    }

    static public TextView textViewFactory(Context context, String text)
    {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setGravity(Gravity.RIGHT);
        tv.setTextSize(20);
        tv.setText(text);

        return tv;
    }

    static public View horizontalDividerFactory(Context context)
    {
        View hd = new View(context);
        hd.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        hd.setBackgroundColor(Color.GRAY);
        return hd;
    }
}
