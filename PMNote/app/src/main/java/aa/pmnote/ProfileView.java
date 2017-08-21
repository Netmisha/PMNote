package aa.pmnote;
;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;

import android.content.DialogInterface;
import android.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import aa.pmnote.CustomAdapter;import aa.pmnote.R;

import java.util.Vector;

public class ProfileView extends AppCompatActivity
{
    private final static String NAME_TAG = "PERSON_NAME";
    private String mName;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                    mRootRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                }
            }
        };


        SetProjects();

        //vars
        slider_arch = new Vector<TextView>();
        mName = getIntent().getStringExtra(NAME_TAG);

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
                            EditText et = new EditText(ProfileView.this);
                            TextView note_name = new TextView(ProfileView.this);
                            note_name.setText(input.getText().toString());
                            et.setHint("Enter your note here... ");
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
                           SetCheckBox(input.getText().toString());
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


    void SetCheckBox(String input)
    {
        CheckBox cb = new CheckBox(ProfileView.this);
        cb.setText(input);

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
                         SetSeekBar(input.getText().toString(), Integer.parseInt(max_input.getText().toString()));
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
    void SetSeekBar(String name, int max)
    {
        SeekBar sbar = new SeekBar(ProfileView.this);
        slider_arch.add(new TextView(ProfileView.this));

        sbar.setMax(max);
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
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
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
                           SetCombobox(input.getText().toString(), final_list);
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

    void SetCombobox(String name, String[] list)
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
            }
        });
        CustomAdapter adapter = new CustomAdapter(ProfileView.this,
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

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
}



