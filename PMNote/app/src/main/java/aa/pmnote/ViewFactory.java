package aa.pmnote;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

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
        ll.setTag(Defines.LinearLayoutType.TASK);

        ll.addView(checkBoxFactory(context, text, checkBoxStatus));
        ll.addView(textViewFactory(context, text));

        return ll;
    }

    static public CheckBox checkBoxFactory(Context context, String text, boolean checked)
    {
        final CheckBox cb = new CheckBox(context);
        cb.setLayoutParams(new ViewGroup.LayoutParams(120, 120));
        cb.setText("");
        cb.setTag(text);
        cb.setChecked(checked);

        return cb;
    }

    static public ImageView imageViewFactory(Context context, Defines.LinearLayoutType llt)
    {
        ImageView iv = new ImageView(context);
        iv.setLayoutParams(new ViewGroup.LayoutParams(120, 120));
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
        return textViewFactory(context, text, null);
    }

    static public TextView textViewFactory(Context context, String text, View.OnClickListener onClickListener)
    {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setGravity(Gravity.RIGHT);
        tv.setTextSize(20);
        tv.setText(text);
        if(onClickListener != null)
            tv.setOnClickListener(onClickListener);

        return tv;
    }

    static public View horizontalDividerFactory(Context context)
    {
        return horizontalDividerFactory(context, Color.GRAY);
    }

    static public View horizontalDividerFactory(Context context, int color)
    {
        View hd = new View(context);
        hd.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        hd.setBackgroundColor(color);
        return hd;
    }

    static public LinearLayout titledLinearLayoutFactory(Context context, String text)
    {
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        ll.setGravity(Gravity.CENTER);
        ll.setLayoutParams(lp);
        ll.setVisibility(View.GONE);
        ll.setPadding(0, 0, 0, 5);

        TextView tv = new TextView(context);
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120));
        tv.setGravity(Gravity.LEFT);
        tv.setTextSize(24);
        tv.setText(text);
        tv.setVisibility(View.VISIBLE);
        tv.setTextColor(Color.BLACK);

        ll.addView(tv);
        ll.addView(horizontalDividerFactory(context, Color.BLACK));

        return ll;
    }
}
