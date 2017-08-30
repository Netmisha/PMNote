package aa.pmnote;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;

import aa.pmnote.OnSwipeTouchListener;

/**
 * Created by anton.gorielikov on 8/23/2017.
 */

public class ViewFactory {
    final static int LINEAR_LAYOUT_CHECKBOX_POSITION = 0;

    static LinearLayout linearLayoutFactory(Context context, String text, Defines.LinearLayoutType llt) {
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

    static LinearLayout linearLayoutFactory(Context context, String text, boolean checkBoxStatus) {
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

    static CheckBox checkBoxFactory(Context context, String text, boolean checked) {
        final CheckBox cb = new CheckBox(context);
        cb.setLayoutParams(new ViewGroup.LayoutParams(120, 120));
        cb.setText("");
        cb.setTag(text);
        cb.setChecked(checked);

        return cb;
    }

    static ImageView imageViewFactory(Context context, Defines.LinearLayoutType llt) {
        ImageView iv = new ImageView(context);
        iv.setLayoutParams(new ViewGroup.LayoutParams(120, 120));
        switch (llt) {
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

    static TextView textViewFactory(Context context, String text) {
        return textViewFactory(context, text, null);
    }

    static TextView textViewFactory(Context context, String text, View.OnClickListener onClickListener) {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setGravity(Gravity.RIGHT);
        tv.setTextSize(20);
        tv.setText(text);
        if (onClickListener != null)
            tv.setOnClickListener(onClickListener);

        return tv;
    }

    static View horizontalDividerFactory(Context context) {
        return horizontalDividerFactory(context, Color.GRAY);
    }

    static View horizontalDividerFactory(Context context, int color) {
        View hd = new View(context);
        hd.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        hd.setBackgroundColor(color);
        return hd;
    }

    static LinearLayout titledLinearLayoutFactory(Context context, String text) {
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.setGravity(Gravity.CENTER);
        ll.setLayoutParams(lp);
        ll.setPadding(0, 0, 0, 5);

        TextView tv = new TextView(context);
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120));
        tv.setGravity(Gravity.START | Gravity.CENTER_HORIZONTAL);
        tv.setTextSize(24);
        tv.setText(text);
        tv.setVisibility(View.VISIBLE);
        tv.setTextColor(Color.BLACK);

        ll.addView(tv);
        ll.addView(horizontalDividerFactory(context, Color.BLACK));

        return ll;
    }

    static LinearLayout attachToEditTextFactory(final Context context, final DatabaseReference root, final LinearLayout linearLayout)
    {
        return attachToEditTextFactory(context, root, linearLayout, null);
    }

    static LinearLayout attachToEditTextFactory(final Context context, final DatabaseReference root, final LinearLayout linearLayout, String text) {
        final LinearLayout ll = new LinearLayout(context);
        ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setVerticalGravity(Gravity.CENTER_VERTICAL);

        final EditText et = new EditText(context);
        et.setHint("Attach to");
        et.setKeyListener(null);
        if(text != null) {
            et.setText(text);
        }
        et.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b)
                {
                    root.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Attach to");

                            LinearLayout buildLL = new LinearLayout(context);
                            buildLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            buildLL.setOrientation(LinearLayout.VERTICAL);

                            final Spinner spinner = new Spinner(context);
                            spinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            final ArrayList<String> list = new ArrayList<>();
                            list.add("Person");
                            list.add("Project");
                            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                                    R.layout.my_spinner_item, list);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            buildLL.addView(spinner);

                            final ListView peopleView = listViewFactory(context, dataSnapshot.child("People"));
                            peopleView.setVisibility(View.GONE);
                            final ListView projectsView = listViewFactory(context, dataSnapshot.child("Projects"));
                            projectsView.setVisibility(View.GONE);

                            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                                    switch(position)
                                    {
                                        case 0:
                                            projectsView.setVisibility(View.GONE);
                                            peopleView.setVisibility(View.VISIBLE);
                                            break;
                                        case 1:
                                            peopleView.setVisibility(View.GONE);
                                            projectsView.setVisibility(View.VISIBLE);
                                            break;
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });

                            buildLL.addView(peopleView);
                            buildLL.addView(projectsView);

                            builder.setView(buildLL);
                            final AlertDialog ad = builder.create();

                            peopleView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    String name = ((TextView)view).getText().toString();
                                    et.setText("Person:" + name);
                                    linearLayout.addView(attachToEditTextFactory(context, root, linearLayout));
                                    ad.dismiss();
                                }
                            });

                            projectsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    String name = ((TextView)view).getText().toString();
                                    et.setText("Project:" + name);
                                    linearLayout.addView(attachToEditTextFactory(context, root, linearLayout));
                                    ad.dismiss();
                                }
                            });

                            ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {
                                }
                            });

                            ad.show();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        ll.addView(et);

        ImageView iv = new ImageView(context);
        iv.setLayoutParams(new ViewGroup.LayoutParams(120, 120));
        iv.setImageResource(R.drawable.ic_remove);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(linearLayout.getChildCount() > 1 && !et.getText().toString().isEmpty())
                    linearLayout.removeView(ll);
            }
        });
        iv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 10.0f));

        ll.addView(iv);

        return ll;
    }

    static ListView listViewFactory(Context context, DataSnapshot ds){

        ArrayList<String> arrayList = new ArrayList<String>();
        for(DataSnapshot dataSnapshot : ds.getChildren())
        {
            arrayList.add(dataSnapshot.getKey());
        }

        ListView lv = new ListView(context);
        lv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, arrayList);
        lv.setAdapter(arrayAdapter);

        return lv;
    }

    static View placeholderFactory(Context context)
    {
        View view = new View(context);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120));
        return view;
    }
}