package aa.pmnote;
;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;

import android.content.DialogInterface;
import android.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Toast;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.LinearLayout;

//for notifications

import 	android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import aa.pmnote.CustomAdapter;import aa.pmnote.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class ProfileView extends AppCompatActivity
{
    private final static String NAME_TAG = "PERSON_NAME";
    private String mName;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mRootRef;

    //how widgets will be saved on firebase
    private final static int NOTE = 1;
    private final static int CHECKBOX = 2;
    private final static int SLIDER = 3;
    private final static int COMBOBOX = 4;
    private final static int SPINBOX = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //vars
        slider_arch = new Vector<TextView>();
        mName = getIntent().getStringExtra(NAME_TAG);

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null) {
                    finish();
                }
                else
                {
                    String uid = firebaseAuth.getCurrentUser().getUid();
                    mRootRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("people").child(mName);
                    SetUpWidgetList();
                }
            }
        };


        SetProjects();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {


                AlertDialog.Builder builder;
                final String[] mItemsName = {"Note", "Checkbox", "Slider",  "Combobox", "Spinbox", "Add Task", "Assign Task"};

                builder = new AlertDialog.Builder(ProfileView.this);
                builder.setTitle("Add ..."); // tite

                builder.setItems(mItemsName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        switch (mItemsName[item]) {
                            case "Note":
                                addNote();
                                break;
                            case "Checkbox":
                                addCheckbox();
                                break;
                            case "Slider":
                                addSeekBar();
                                break;
                            case "Combobox":
                                addCombobox();
                                break;
                            case "Spinbox":
                                break;
                        }
                    }
                });
                builder.setCancelable(true);
                builder.show();

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    private void addNote()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileView.this);
        alertDialog.setTitle("NOTE");
        alertDialog.setMessage("Note name:");

        final EditText input = new EditText(ProfileView.this);
        TextView enter_s = new TextView(ProfileView.this);

        enter_s.setText("Enter note name:");
        LinearLayout slider_set_layout = new LinearLayout(ProfileView.this);
        slider_set_layout.setOrientation(LinearLayout.VERTICAL);
        slider_set_layout.addView(enter_s);
        slider_set_layout.addView(input);

        LinearLayout.LayoutParams set_lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(set_lp);
        alertDialog.setView(slider_set_layout);

        alertDialog.setPositiveButton("SET",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which) {
                        if(!input.getText().toString().isEmpty())
                        {
                            mRootRef.child("Widgets").child(input.getText().toString()).child("Type").setValue(NOTE);
                            mRootRef.child("Widgets").child(input.getText().toString()).child("Text").setValue("");
                        }
                         else
                        {
                            Toast.makeText(getApplicationContext(), "One of dialog fields is empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });

        alertDialog.show();


    }

    void SetNote(final String n_name, String inner_text)
    {
        final EditText et = new EditText(ProfileView.this);
        et.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {}

            @Override
            public void afterTextChanged(Editable s)
            {
                mRootRef.child("Widgets").child(n_name).child("Text").setValue(et.getText().toString());
            }

        });
        TextView note_name = new TextView(ProfileView.this);
        note_name.setText(n_name);
        if(inner_text.isEmpty())
            et.setHint("Enter your note here... ");
        else
            et.setText(inner_text);
        LinearLayout note_layout = new LinearLayout(ProfileView.this);
        note_layout.setOrientation(LinearLayout.VERTICAL);
        note_layout.addView(note_name);
        note_layout.addView(et);


        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT); //height enough
        ll.addView(note_layout, lp);

    }


    private void addCheckbox()
    {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileView.this);
        alertDialog.setTitle("Checkbox");
        alertDialog.setMessage("Checkbox info:");

        final EditText input = new EditText(ProfileView.this);
        TextView enter_n = new TextView(ProfileView.this);

        enter_n.setText("Enter checkbox label:");
        LinearLayout slider_set_layout = new LinearLayout(ProfileView.this);
        slider_set_layout.setOrientation(LinearLayout.VERTICAL);
        slider_set_layout.addView(enter_n);
        slider_set_layout.addView(input);

        LinearLayout.LayoutParams set_lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(set_lp);
        alertDialog.setView(slider_set_layout);

        alertDialog.setPositiveButton("SET",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(!input.getText().toString().isEmpty())
                        {
                            mRootRef.child("Widgets").child(input.getText().toString()).child("Type").setValue(CHECKBOX);
                            mRootRef.child("Widgets").child(input.getText().toString()).child("isChecked").setValue(false);
                        }
                         else
                        {
                            Toast.makeText(getApplicationContext(), "One of dialog fields is empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });

        alertDialog.show();

    }


    void SetCheckBox(final String input, final Boolean checked)
    {
        CheckBox cb = new CheckBox(ProfileView.this);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                {
                    mRootRef.child("Widgets").child(input).child("isChecked").setValue(true);
                }
                else
                {
                    mRootRef.child("Widgets").child(input).child("isChecked").setValue(false);
                }
            }
        });
        cb.setText(input);
        cb.setChecked(checked);

        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.addView(cb, lp);
    }


    private void addSeekBar()
    {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileView.this);
        alertDialog.setTitle("SLIDER");
        alertDialog.setMessage("Slider info:");

        final EditText input = new EditText(ProfileView.this);
        final EditText max_input = new EditText(ProfileView.this);
        TextView enter_s = new TextView(ProfileView.this);
        TextView enter_m = new TextView(ProfileView.this);

        max_input.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_SIGNED);
        enter_s.setText("Enter slider name:");
        enter_m.setText("Enter max value:");
        LinearLayout slider_set_layout = new LinearLayout(ProfileView.this);
        slider_set_layout.setOrientation(LinearLayout.VERTICAL);
        slider_set_layout.addView(enter_s);
        slider_set_layout.addView(input);
        slider_set_layout.addView(enter_m);
        slider_set_layout.addView(max_input);

        LinearLayout.LayoutParams set_lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(set_lp);
        alertDialog.setView(slider_set_layout);

        alertDialog.setPositiveButton("SET",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {


                        if(!input.getText().toString().isEmpty() && !max_input.getText().toString().isEmpty())
                        {
                            mRootRef.child("Widgets").child(input.getText().toString()).child("Type").setValue(SLIDER);
                            mRootRef.child("Widgets").child(input.getText().toString()).child("Max_Num").setValue(max_input.getText().toString());
                            mRootRef.child("Widgets").child(input.getText().toString()).child("Cur_Num").setValue(0);

                        }
                         else
                        {
                            Toast.makeText(getApplicationContext(), "One of dialog fields is empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });

        alertDialog.show();

    }

    private Vector<TextView> slider_arch;
    void SetSeekBar(final String name, final int max, final int current_num)
    {
        SeekBar sbar = new SeekBar(ProfileView.this);
        slider_arch.add(new TextView(ProfileView.this));

        sbar.setMax(max);
        sbar.setProgress(current_num);
        final String UserText = name;
        slider_arch.get(slider_arch.size() - 1).setText(UserText + ": " + sbar.getProgress() + "/" + sbar.getMax());
        final TextView slider_txt = slider_arch.get(slider_arch.size() - 1);
        sbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                TextView slider_tmp = slider_txt;
                slider_tmp.setText(UserText + ": " + progress + "/" + seekBar.getMax());

                mRootRef.child("Widgets").child(name).child("Cur_Num").setValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                TextView slider_tmp = slider_txt;
                slider_tmp.setText(UserText + ": " + progress + "/" + seekBar.getMax());
            }
        });

        LinearLayout slider_layout = new LinearLayout(ProfileView.this);
        slider_layout.setOrientation(LinearLayout.VERTICAL);
        slider_layout.addView(slider_arch.get(slider_arch.size() - 1));
        slider_layout.addView(sbar);

        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.addView(slider_layout, lp);
    }


    private void addCombobox() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileView.this);
        alertDialog.setTitle("COMBOBOX");
        alertDialog.setMessage("Combobox info:");

        final EditText input = new EditText(ProfileView.this);
        final EditText item1 = new EditText(ProfileView.this);
        final EditText item2 = new EditText(ProfileView.this);
        final EditText item3 = new EditText(ProfileView.this);
        final EditText item4 = new EditText(ProfileView.this);
        final EditText item5 = new EditText(ProfileView.this);
        TextView enter_s = new TextView(ProfileView.this);
        TextView enter_m = new TextView(ProfileView.this);

        enter_s.setText("Enter combobox name:");
        enter_m.setText("Enter items:");
        LinearLayout slider_set_layout = new LinearLayout(ProfileView.this);
        slider_set_layout.setOrientation(LinearLayout.VERTICAL);
        slider_set_layout.addView(enter_s);
        slider_set_layout.addView(input);
        slider_set_layout.addView(enter_m);
        slider_set_layout.addView(item1);
        slider_set_layout.addView(item2);
        slider_set_layout.addView(item3);
        slider_set_layout.addView(item4);
        slider_set_layout.addView(item5);

        LinearLayout.LayoutParams set_lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(set_lp);
        alertDialog.setView(slider_set_layout);

        alertDialog.setPositiveButton("SET",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        //set up list of items

                        String[] list = new String[5];
                        Integer active_item = new Integer(0);
                       if (!item1.getText().toString().isEmpty()) {
                            list[active_item] = item1.getText().toString();
                            active_item++;
                        }
                        if (!item2.getText().toString().isEmpty()) {
                            list[active_item] = item2.getText().toString();
                            active_item++;
                        }
                        if (!item3.getText().toString().isEmpty()) {
                            list[active_item] = item3.getText().toString();
                            active_item++;
                        }
                        if (!item4.getText().toString().isEmpty()) {
                            list[active_item] = item4.getText().toString();
                            active_item++;
                        }
                        if (!item5.getText().toString().isEmpty()) {
                            list[active_item] = item5.getText().toString();
                            active_item++;
                        }

                        String[] final_list = new String[active_item];

                        for(int i =0; i<active_item; i++)
                        {
                            final_list[i] = list[i];
                        }//set up spinner
                        if( !input.getText().toString().isEmpty() && active_item!=0 )
                        {
                            mRootRef.child("Widgets").child(input.getText().toString()).child("Type").setValue(COMBOBOX);
                            mRootRef.child("Widgets").child(input.getText().toString()).child("Items_Num").setValue(active_item);
                            List items_list = new ArrayList<String>(Arrays.asList(final_list));
                            mRootRef.child("Widgets").child(input.getText().toString()).child("Items").setValue(items_list);
                            mRootRef.child("Widgets").child(input.getText().toString()).child("Chosen_One").setValue(final_list[0]);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "One of dialog fields is empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    void SetCombobox(final String name, final String[] list, String chosen)
    {
        TextView s_name = new TextView(ProfileView.this);
        s_name.setText(name);
        Spinner spinner = new Spinner(ProfileView.this);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                mRootRef.child("Widgets").child(name).child("Chosen_One").setValue(list[pos]);
            }
        });
        CustomAdapter adapter = new CustomAdapter(ProfileView.this,
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if(!chosen.isEmpty())
        spinner.setSelection(adapter.getPosition(chosen));

        LinearLayout s_layout = new LinearLayout(ProfileView.this);
        s_layout.setOrientation(LinearLayout.HORIZONTAL);
        s_layout.addView(s_name);
        s_layout.addView(spinner);


        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT); //height enough
        ll.addView(s_layout, lp);
    }


    void SetProjects()
    {
        if (true)
        {
            TextView no_proj = new TextView(ProfileView.this);
            no_proj.setText("None");
            LinearLayout ll = (LinearLayout) findViewById(R.id.projects_layout);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.addView(no_proj, lp);
            ll.addView(horizontalDividerFactory());
        }
        else
        {
            //TODO
        }
    }

    private View horizontalDividerFactory()
    {
        View hd = new View(ProfileView.this);
        hd.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        hd.setBackgroundColor(Color.GRAY);
        return hd;
    }

    //get widget list from database
    private void SetUpWidgetList()
    {
        mRootRef.child("Widgets").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String widget_name = dataSnapshot.getKey();

                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(ProfileView.this);
                dlgAlert.setMessage(widget_name);
                dlgAlert.setTitle("App Title");
                dlgAlert.setPositiveButton("OK", null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
                //int type = (int)dataSnapshot.child("Type").getValue();
                switch(1)//(type)
                {
                    case NOTE:
                       //SetNote(widget_name, dataSnapshot.child("Text").getValue(String.class) );
                        break;

                    case CHECKBOX:
                       // SetCheckBox(widget_name, dataSnapshot.child("isChecked").getValue(Boolean.class));
                        break;

                    case SLIDER:
                        //SetSeekBar(widget_name, dataSnapshot.child("Max_Num").getValue(Integer.class), dataSnapshot.child("Cur_Num").getValue(Integer.class));
                        break;

                    case COMBOBOX:
                       // SetCombobox(widget_name, dataSnapshot.child("Items").getValue(String[].class),dataSnapshot.child("Chosen_One").getValue(String.class));
                        break;

                    case SPINBOX:
                        break;
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
               // RemovePersonOrProject(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}



