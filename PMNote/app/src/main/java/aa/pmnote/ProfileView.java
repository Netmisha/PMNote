package aa.pmnote;
;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.content.DialogInterface;
import android.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.Toast;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.LinearLayout;

//for notifications

import 	android.widget.Spinner;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import aa.pmnote.CustomAdapter;import aa.pmnote.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.StrictMath.toIntExact;

public class ProfileView extends AppCompatActivity
{
    private final static String NAME_TAG = "PERSON_NAME";
    private String mName;
    public View for_alertdialog;
    public Boolean changingDir;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mRootRef;
    private StorageReference mStorageRef;
    public String uid;
    public static Boolean isThereAnyProjects;

    //how widgets will be saved on firebase
    private final static int NOTE = 1;
    private final static int CHECKBOX = 2;
    private final static int SLIDER = 3;
    private final static int COMBOBOX = 4;
    private final static int SPINBOX = 5;

    final static int IMPORT_PICTURE = 123;
    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //adding
        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
        ll.addView(horizontalDividerFactory());
        //vars
        slider_arch = new Vector<TextView>();
        changingDir = false;
        //set up name
        mName = getIntent().getStringExtra(NAME_TAG);
        ((TextView) findViewById(R.id.name)).setText(mName);

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null) {
                    finish();
                }
                else
                {
                    uid = firebaseAuth.getCurrentUser().getUid();
                    mRootRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("people").child(mName);
                    mStorageRef = FirebaseStorage.getInstance().getReference();
                    SetUpWidgetList();
                }
            }
        };


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
        if(widgetsCEL != null)
            mRootRef.child("Widgets").removeEventListener(widgetsCEL);
        if(projectsCEL != null)
            mRootRef.child("Projects").removeEventListener(projectsCEL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMPORT_PICTURE && resultCode==RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file


            StorageReference ProfileImgRef = mStorageRef.child("/"+uid + "/" + mName+"/ProfileImage/");
            ProfileImgRef.delete();

            ProfileImgRef.putFile(selectedfile)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Toast.makeText(ProfileView.this, "Upload faild",
                                    Toast.LENGTH_LONG).show();

                        }
                    });


            ImageView imageView =  (ImageView) findViewById(R.id.avatar);
            imageView.setImageURI(null);
            imageView.setImageURI(selectedfile);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.set_img) {

            Intent intent = new Intent()
                    .setType("image/*")
                    .setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent, "Select a picture"), IMPORT_PICTURE);

            return true;
        }

        if (id == R.id.set_info) {
            OpenSetInfoDialog();
            return true;
        }

        if (id == R.id.del_usr) {
            AlertDialog.Builder ad;
            String title = "Warning !";
            String message = "Do you really want to delete this profile?";
            String button1String = "Yes";
            String button2String = "Cencel";

            ad = new AlertDialog.Builder(ProfileView.this);
            ad.setTitle(title);  // заголовок
            ad.setMessage(message); // сообщение
            ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    mRootRef.removeValue();
                    finish();
                }
            });
            ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    Toast.makeText(ProfileView.this, "Cenceled",
                            Toast.LENGTH_LONG).show();
                }
            });
            ad.setCancelable(true);
            ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    Toast.makeText(ProfileView.this, "Cenceled",
                            Toast.LENGTH_LONG).show();
                }}
            );

            ad.show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void OpenSetInfoDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileView.this);
        alertDialog.setTitle("Profile Info");
        alertDialog.setMessage("Enter Person Info: ");

        final EditText name_input = new EditText(ProfileView.this);
        name_input.setText(mName);
        final EditText mail_input = new EditText(ProfileView.this);
        mail_input.setText( ((TextView)findViewById(R.id.mail)).getText().toString() );
        mail_input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS | InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT);
        final EditText skype_input = new EditText(ProfileView.this);
        skype_input.setText( ((TextView)findViewById(R.id.skype)).getText().toString() );
        final EditText number_input = new EditText(ProfileView.this);
        number_input.setText( ((TextView)findViewById(R.id.number)).getText().toString() );
        number_input.setInputType(InputType.TYPE_CLASS_PHONE);

        final TextView enter_n = new TextView(ProfileView.this);
        enter_n.setText("Enter name:");
        TextView enter_m = new TextView(ProfileView.this);
        enter_m.setText("Enter e-mail:");
        TextView enter_s = new TextView(ProfileView.this);
        enter_s.setText("Enter skype:");
        TextView enter_number = new TextView(ProfileView.this);
        enter_number.setText("Enter phone number:");

        LinearLayout info_set_layout = new LinearLayout(ProfileView.this);
        info_set_layout.setOrientation(LinearLayout.VERTICAL);
        info_set_layout.addView(enter_n);
        info_set_layout.addView(name_input);
        info_set_layout.addView(enter_m);
        info_set_layout.addView(mail_input);
        info_set_layout.addView(enter_s);
        info_set_layout.addView(skype_input);
        info_set_layout.addView(enter_number);
        info_set_layout.addView(number_input);

        LinearLayout.LayoutParams set_lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        name_input.setLayoutParams(set_lp);
        mail_input.setLayoutParams(set_lp);
        skype_input.setLayoutParams(set_lp);
        alertDialog.setView(info_set_layout);

        alertDialog.setPositiveButton("SET",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(!name_input.getText().toString().isEmpty())
                        {
                            String mNewName =  name_input.getText().toString();
                            //if we changing the name --> we changing the brunch
                            if( mNewName.compareTo(mName) != 0 ) {

                                mName = mNewName;
                                ((TextView) findViewById(R.id.name)).setText(mName);

                                changingDir = true;

                                moveFirebaseRecord(mRootRef, FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("people").child(mName));
                            }
                            ((TextView) findViewById(R.id.skype)).setText(skype_input.getText().toString());
                            mRootRef.child("Info").child("skype").setValue(skype_input.getText().toString());


                            String email = mail_input.getText().toString();
                            if(isEmailValid(email))
                            {
                                String hyper_email = "<a href=\""+email+"\">" + email+"</a>";
                                ((TextView) findViewById(R.id.mail)).setText(Html.fromHtml(hyper_email));
                                ((TextView) findViewById(R.id.mail)).setMovementMethod(LinkMovementMethod.getInstance());
                                mRootRef.child("Info").child("mail").setValue(hyper_email);
                            }
                            else
                                {
                                    Toast.makeText(ProfileView.this, "Not valid e-mail!",
                                            Toast.LENGTH_LONG).show();
                                }
                            ((TextView) findViewById(R.id.number)).setText(number_input.getText().toString());
                            mRootRef.child("Info").child("number").setValue(number_input.getText().toString());
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Name is empty", Toast.LENGTH_SHORT).show();
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

    public boolean isEmailValid(String email)
    {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        if(matcher.matches())
            return true;
        else
            return false;
    }

    public void moveFirebaseRecord(DatabaseReference fromPath, final DatabaseReference toPath)
    {
        fromPath.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               toPath.setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {
                   @Override
                   public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                       if(changingDir) {
                           mRootRef.removeValue();
                           finish();
                       }
                   }
               });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private View horizontalDividerFactory()
    {
        View hd = new View(ProfileView.this);
        hd.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        hd.setBackgroundColor(Color.GRAY);
        return hd;
    }

    private void addNote()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileView.this);
        alertDialog.setTitle("NOTE");
        alertDialog.setMessage("Note name:");

        final EditText input = new EditText(ProfileView.this);
        TextView enter_s = new TextView(ProfileView.this);

        enter_s.setText("Enter note name:");
        enter_s.setTextSize(15);
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
                            SetNote(input.getText().toString(), "");
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
        note_name.setTextSize(20);
        note_name.setTextColor(Color.BLACK);
        if(inner_text.isEmpty())
            et.setHint("Enter your note here... ");
        else
            et.setText(inner_text);
        LinearLayout note_layout = new LinearLayout(ProfileView.this);
        note_layout.setOrientation(LinearLayout.VERTICAL);
        note_layout.addView(note_name);
        note_layout.addView(et);
        note_layout.addView(horizontalDividerFactory());
        Space space = new Space(ProfileView.this );
        note_layout.addView(space);

        note_layout.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v)
        {
            AlertDialog.Builder ad;
            String title = "Warning !";
            String message = "Do you really want to delete this widget?";
            String button1String = "Yes";
            String button2String = "Cencel";

            ad = new AlertDialog.Builder(ProfileView.this);
            ad.setTitle(title);  // заголовок
            ad.setMessage(message); // сообщение
            ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
                    LinearLayout dead_layout = ((LinearLayout) for_alertdialog);
                    ll.removeView(dead_layout);
                    mRootRef.child("Widgets").child(n_name).removeValue();
                }
            });
            ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    Toast.makeText(ProfileView.this, "Cenceled",
                            Toast.LENGTH_LONG).show();
                }
            });
            ad.setCancelable(true);
            ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    Toast.makeText(ProfileView.this, "Cenceled",
                            Toast.LENGTH_LONG).show();
                }}
            );

            for_alertdialog = v;
            ad.show();

            return true;
        }});



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
                            SetCheckBox(input.getText().toString(), false);
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
        cb.setTextSize(20);
        cb.setTextColor(Color.BLACK);
        cb.setChecked(checked);

        final LinearLayout cb_layout = new LinearLayout(ProfileView.this);
        cb_layout.setOrientation(LinearLayout.VERTICAL);
        cb_layout.addView(cb);
        cb_layout.addView(horizontalDividerFactory());
        Space space = new Space(ProfileView.this );
        cb_layout.addView(space);
        cb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                AlertDialog.Builder ad;
                String title = "Warning !";
                String message = "Do you really want to delete this widget?";
                String button1String = "Yes";
                String button2String = "Cencel";

                ad = new AlertDialog.Builder(ProfileView.this);
                ad.setTitle(title);  // заголовок
                ad.setMessage(message); // сообщение
                ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
                        ll.removeView(cb_layout);
                        mRootRef.child("Widgets").child(input).removeValue();
                    }
                });
                ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        Toast.makeText(ProfileView.this, "Cenceled",
                                Toast.LENGTH_LONG).show();
                    }
                });
                ad.setCancelable(true);
                ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        Toast.makeText(ProfileView.this, "Cenceled",
                                Toast.LENGTH_LONG).show();
                    }}
                );

                ad.show();

                return true;
            }});


        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.addView(cb_layout, lp);
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
                            mRootRef.child("Widgets").child(input.getText().toString()).child("Max_Num").setValue(Integer.parseInt(max_input.getText().toString()));
                            mRootRef.child("Widgets").child(input.getText().toString()).child("Cur_Num").setValue(0);
                            SetSeekBar(input.getText().toString(), Integer.parseInt(max_input.getText().toString()), 0);

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
        slider_txt.setTextSize(20);
        slider_txt.setTextColor(Color.BLACK);

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
        slider_layout.addView(horizontalDividerFactory());
        Space space = new Space(ProfileView.this );
        slider_layout.addView(space);

        slider_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                AlertDialog.Builder ad;
                String title = "Warning !";
                String message = "Do you really want to delete this widget?";
                String button1String = "Yes";
                String button2String = "Cencel";

                ad = new AlertDialog.Builder(ProfileView.this);
                ad.setTitle(title);  // заголовок
                ad.setMessage(message); // сообщение
                ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
                        LinearLayout dead_layout = ((LinearLayout) for_alertdialog);
                        ll.removeView(dead_layout);
                        mRootRef.child("Widgets").child(name).removeValue();
                    }
                });
                ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        Toast.makeText(ProfileView.this, "Cenceled",
                                Toast.LENGTH_LONG).show();
                    }
                });
                ad.setCancelable(true);
                ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        Toast.makeText(ProfileView.this, "Cenceled",
                                Toast.LENGTH_LONG).show();
                    }}
                );

                for_alertdialog = v;
                ad.show();

                return true;
            }});


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
                            SetCombobox(input.getText().toString(),final_list, final_list[0] );
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
        s_name.setTextSize(20);
        s_name.setTextColor(Color.BLACK);
        Spinner spinner = new Spinner(ProfileView.this);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                ((TextView) parent.getChildAt(0)).setTextSize(20);
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

        LinearLayout s2_layout = new LinearLayout(ProfileView.this);
        s2_layout.setOrientation(LinearLayout.VERTICAL);
        s2_layout.addView(s_layout);
        s2_layout.addView(horizontalDividerFactory());
        Space space = new Space(ProfileView.this );
        s2_layout.addView(space);

        s2_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)
            {
                AlertDialog.Builder ad;
                String title = "Warning !";
                String message = "Do you really want to delete this widget?";
                String button1String = "Yes";
                String button2String = "Cencel";

                ad = new AlertDialog.Builder(ProfileView.this);
                ad.setTitle(title);  // заголовок
                ad.setMessage(message); // сообщение
                ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
                        LinearLayout dead_layout = ((LinearLayout) for_alertdialog);
                        ll.removeView(dead_layout);
                        mRootRef.child("Widgets").child(name).removeValue();
                    }
                });
                ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        Toast.makeText(ProfileView.this, "Cenceled",
                                Toast.LENGTH_LONG).show();
                    }
                });
                ad.setCancelable(true);
                ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        Toast.makeText(ProfileView.this, "Cenceled",
                                Toast.LENGTH_LONG).show();
                    }}
                    );

                    for_alertdialog = v;
                        ad.show();

                        return true;
                }});

        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT); //height enough
        ll.addView(s2_layout, lp);
    }

    private ChildEventListener widgetsCEL = null;
    private ChildEventListener projectsCEL = null;
    //get widget list from database
    private void SetUpWidgetList()
    {
        widgetsCEL = mRootRef.child("Widgets").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String widget_name = dataSnapshot.getKey();



                Long type=0L;
                if(dataSnapshot.child("Type").exists())
                {
                    type = (long)dataSnapshot.child("Type").getValue();

                }

                int i_type = type.intValue();
                switch(i_type)
                {
                    case NOTE:
                        if(dataSnapshot.child("Text").exists()) {
                            type = (long)dataSnapshot.child("Type").getValue();

                            SetNote(widget_name, dataSnapshot.child("Text").getValue(String.class));
                        }
                        break;

                    case CHECKBOX:
                        if(dataSnapshot.child("isChecked").exists()) {
                            SetCheckBox(widget_name, dataSnapshot.child("isChecked").getValue(Boolean.class));
                        }
                        break;

                    case SLIDER:
                        if(dataSnapshot.child("Max_Num").exists()) {
                            Long max_num = 0L;
                            max_num = (long) dataSnapshot.child("Max_Num").getValue();
                            Long cur_num = 0L;
                            cur_num = (long) dataSnapshot.child("Cur_Num").getValue();
                            int i_max_num = max_num.intValue();
                            int i_cur_num = cur_num.intValue();

                            SetSeekBar(widget_name, i_max_num, i_cur_num);
                        }
                        break;

                    case COMBOBOX:
                        if(dataSnapshot.child("Items_Num").exists()) {
                            Long items_num = 0L;
                            items_num = (long) dataSnapshot.child("Items_Num").getValue();
                            int i_items_num = items_num.intValue();
                            String[] list = new String[i_items_num];

                            for (Integer i = 0; i < i_items_num; i++) {
                                list[i] = dataSnapshot.child("Items").child(i.toString()).getValue(String.class);
                            }

                            SetCombobox(widget_name, list, dataSnapshot.child("Chosen_One").getValue(String.class));
                        }
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

        projectsCEL = mRootRef.child("Projects").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                TextView proj = new TextView(ProfileView.this);
                proj.setText(dataSnapshot.getKey());
                proj.setTextColor(Color.BLACK);
                proj.setTextSize(20);
                LinearLayout ll = (LinearLayout) findViewById(R.id.projects_layout);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                ll.addView(proj, lp);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
            }
        );
        mRootRef.child("Info").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String key = dataSnapshot.getKey();

                if (key.compareTo("mail") == 0 )
                {
                    ((TextView) findViewById(R.id.mail)).setText(Html.fromHtml(dataSnapshot.getValue(String.class)));
                    ((TextView) findViewById(R.id.mail)).setMovementMethod(LinkMovementMethod.getInstance());
                }
                else if (key.compareTo("skype") == 0 )
                {

                    ((TextView) findViewById(R.id.skype)).setText(dataSnapshot.getValue(String.class));
                }
                else if (key.compareTo("number") == 0 )
                {
                    ((TextView) findViewById(R.id.number)).setText(dataSnapshot.getValue(String.class));
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SetUpImage()
    {
        final File file = new File("main");
       // file = File.createTempFile(Context.getFilesDir(), null, this.getCacheDir());

        StorageReference ProfileImgRef = mStorageRef.child("/"+uid + "/" + mName+"/ProfileImage/");
        ProfileImgRef.getFile(file)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                       // ImageView imageView =  (ImageView) findViewById(R.id.avatar);
                      //  imageView.setImageURI(null);
                       // imageView.setImageURI(file);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                // ...
            }
        });
    }
}



