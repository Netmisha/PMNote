package aa.pmnote;

import android.app.DatePickerDialog;
import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.DateFormat;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import aa.pmnote.Defines;
import aa.pmnote.OnSwipeTouchListener;

public class ProjectsActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mRootRef;

    private Spinner mSearchOptions;

    private ArrayAdapter<String> mArrayAdapter;
    private ArrayList<String> mCurrentSpinnerList = new ArrayList<>();
    private int mSavedSpinnerPosition = 0;

    public void RefreshCurrentFragment() {
        HideItemsBySearchOptions(mSearchOptions.getSelectedItemPosition());
    }

    private void HideItemsBySearchOptions(int search_option) {
        LinearLayout ll = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem()).GetLinearLayout();
        if (mViewPager.getCurrentItem() == 0) {
            switch (search_option) {
                case 0:
                    for (int i = 0; i < ll.getChildCount() - 1; ++i) {
                        ll.getChildAt(i).setVisibility(View.VISIBLE);
                    }
                    break;
                case 1:
                    for (int i = 0; i < ll.getChildCount() - 1; i += 2) {
                        boolean isProject = Defines.LinearLayoutType.PROJECT == ((ImageView) ((LinearLayout) ll.getChildAt(i)).getChildAt(0)).getTag();
                        if (!isProject) {
                            ll.getChildAt(i).setVisibility(View.GONE);
                            ll.getChildAt(i + 1).setVisibility(View.GONE);
                        } else {
                            ll.getChildAt(i).setVisibility(View.VISIBLE);
                            ll.getChildAt(i + 1).setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case 2:
                    for (int i = 0; i < ll.getChildCount() - 1; i += 2) {
                        boolean isProject = Defines.LinearLayoutType.PERSON == ((ImageView) ((LinearLayout) ll.getChildAt(i)).getChildAt(0)).getTag();
                        if (!isProject) {
                            ll.getChildAt(i).setVisibility(View.GONE);
                            ll.getChildAt(i + 1).setVisibility(View.GONE);
                        } else {
                            ll.getChildAt(i).setVisibility(View.VISIBLE);
                            ll.getChildAt(i + 1).setVisibility(View.VISIBLE);
                        }
                    }
                    break;
            }
        } else {
            switch (search_option) {
                case 0:
                    for (int j = 0; j < ll.getChildCount() - 1; ++j) {
                        LinearLayout mll = (LinearLayout) ll.getChildAt(j);
                        for (int i = 2; i < mll.getChildCount(); i += 2) {
                            boolean isCompeted = ((CheckBox) ((LinearLayout) mll.getChildAt(i)).getChildAt(0)).isChecked();
                            if (isCompeted) {
                                mll.getChildAt(i).setVisibility(View.GONE);
                                mll.getChildAt(i + 1).setVisibility(View.GONE);
                            } else {
                                mll.getChildAt(i).setVisibility(View.VISIBLE);
                                mll.getChildAt(i + 1).setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    break;
                case 1:
                    for (int j = 0; j < ll.getChildCount() - 1; ++j) {
                        LinearLayout mll = (LinearLayout) ll.getChildAt(j);
                        for (int i = 2; i < mll.getChildCount(); i += 2) {
                            boolean isCompeted = ((CheckBox) ((LinearLayout) mll.getChildAt(i)).getChildAt(0)).isChecked();
                            if (!isCompeted) {
                                mll.getChildAt(i).setVisibility(View.GONE);
                                mll.getChildAt(i + 1).setVisibility(View.GONE);
                            } else {
                                mll.getChildAt(i).setVisibility(View.VISIBLE);
                                mll.getChildAt(i + 1).setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        Defines.SetArrayList(mCurrentSpinnerList, Defines.LinearLayoutType.PROJECT);
        mSearchOptions = (Spinner) findViewById(R.id.searchOptions);
        mArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, mCurrentSpinnerList);
        mSearchOptions.setAdapter(mArrayAdapter);
        mSearchOptions.setSelection(0);

        mSearchOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                HideItemsBySearchOptions(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing, duh
            }
        });

        setTitle(mSectionsPagerAdapter.getPageTitle(mViewPager.getCurrentItem()));

        //who cares about depreciation anyway
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setTitle(mSectionsPagerAdapter.getPageTitle(position));
                if(position == Defines.PROJECTS_FRAGMENT) {
                    Defines.SetArrayList(mCurrentSpinnerList, Defines.LinearLayoutType.PROJECT);
                    mArrayAdapter.notifyDataSetChanged();
                    int temp = mSearchOptions.getSelectedItemPosition();
                    mSearchOptions.setSelection(mSavedSpinnerPosition);
                    mSavedSpinnerPosition = temp;
                }
                else {
                    mRootRef.child(Defines.LISTS_FOLDER).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Defines.SetArrayList(mCurrentSpinnerList, dataSnapshot);
                            mArrayAdapter.notifyDataSetChanged();
                            int temp = mSearchOptions.getSelectedItemPosition();
                            mSearchOptions.setSelection(mSavedSpinnerPosition);
                            mSavedSpinnerPosition = temp;
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    finish();
                } else {
                    String uid = firebaseAuth.getCurrentUser().getUid();
                    mRootRef = FirebaseDatabase.getInstance().getReference().child(Defines.USERS_FOLDER).child(uid);
                }
            }
        };

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mViewPager.getCurrentItem()) {
                    case Defines.PROJECTS_FRAGMENT:
                        AddNewProjectPerson();
                        break;
                    case Defines.TASKS_FRAGMENT:
                        AddOrEditTask();
                        break;
                }
            }
        });
    }

    public void AddOrEditTask() {
        AddOrEditTask(null, null, null, null, false, null);
    }

    public void AddOrEditTask(final String name) {
        mRootRef.child(Defines.TASKS_FOLDER).child(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String date = null, time = null, descr = null;
                boolean status = Boolean.parseBoolean((String) dataSnapshot.child(Defines.TASK_STATUS).getValue());
                ArrayList<String> attachedToList = new ArrayList<String>();
                if (dataSnapshot.child(Defines.TASK_DATE).exists()) {
                    date = dataSnapshot.child(Defines.TASK_DATE).getValue(String.class);
                }
                if (dataSnapshot.child(Defines.TASK_TIME).exists()) {
                    time = dataSnapshot.child(Defines.TASK_TIME).getValue(String.class);
                }
                if (dataSnapshot.child(Defines.TASK_DESCR).exists()) {
                    descr = dataSnapshot.child(Defines.TASK_DESCR).getValue(String.class);
                }
                if(dataSnapshot.child(Defines.TASK_ATTACHED_PROJECTS).exists())
                {
                    for(DataSnapshot ds : dataSnapshot.child(Defines.TASK_ATTACHED_PROJECTS).getChildren())
                        attachedToList.add("Project:" + ds.getKey());
                }
                if(dataSnapshot.child(Defines.TASK_ATTACHED_PEOPLE).exists())
                {
                    for(DataSnapshot ds : dataSnapshot.child(Defines.TASK_ATTACHED_PEOPLE).getChildren())
                        attachedToList.add("Person:" + ds.getKey());
                }

                AddOrEditTask(name, date, time, descr, status, attachedToList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void AddOrEditTask(final String name, final String date, final String time, final String description, final boolean status, final ArrayList<String> attachedToList) {
        //build dialog with request for name input
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectsActivity.this);
        builder.setTitle("Edit task");

        final LinearLayout ll = new LinearLayout(ProjectsActivity.this);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);

        InputFilter nameFilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start;i < end;i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i)) && !Character.toString(source.charAt(i)).equals("_") && !Character.toString(source.charAt(i)).equals("-")
                            && !Character.toString(source.charAt(i)).equals(" ")) {
                        return "";
                    }
                }
                return null;
            }
        };

        final EditText nameInput = new EditText(ProjectsActivity.this);
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        nameInput.setHint("Name");
        nameInput.setText(name != null ? name : "");
        ll.addView(nameInput);
        nameInput.setFilters(new InputFilter[] { nameFilter });

        final EditText timeInput = new EditText(ProjectsActivity.this);
        timeInput.setKeyListener(null);
        timeInput.setHint("Expire Time");
        timeInput.setText(time != null ? time : "");
        if (date == null) {
            timeInput.setVisibility(View.GONE);
        }

        timeInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    Calendar c = Calendar.getInstance();
                    TimePickerDialog tpd = new TimePickerDialog(ProjectsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                            timeInput.setText((hour < 10? "0" : "") + hour + ":" + (minute < 10? "0" : "") + minute);
                        }
                    }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
                    tpd.setTitle("Pick deadline time");
                    tpd.show();
                }
            }
        });

        final EditText dateInput = new EditText(ProjectsActivity.this);
        dateInput.setKeyListener(null);
        dateInput.setHint("Expire Date");
        dateInput.setText(date != null ? date : "");
        dateInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    Calendar c = Calendar.getInstance();
                    DatePickerDialog dpd = new DatePickerDialog(ProjectsActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            Calendar date = Calendar.getInstance();
                            date.set(year, month, day);
                            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
                            dateInput.setText(sdf.format(date.getTime()));
                            timeInput.setVisibility(View.VISIBLE);
                        }
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));
                    dpd.setTitle("Pick deadline date.");
                    dpd.show();
                }
            }
        });
        ll.addView(dateInput);
        ll.addView(timeInput);

        final EditText descriptionInput = new EditText(ProjectsActivity.this);
        descriptionInput.setHint("Description");
        descriptionInput.setText(description != null ? description : "");
        ll.addView(descriptionInput);

        final LinearLayout attachToLL = new LinearLayout(ProjectsActivity.this);
        attachToLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        attachToLL.setOrientation(LinearLayout.VERTICAL);
        attachToLL.setGravity(Gravity.CENTER);
        if(attachedToList != null)
        {
            for(String text : attachedToList)
            {
                attachToLL.addView(ViewFactory.attachToEditTextFactory(ProjectsActivity.this, mRootRef, attachToLL, text));
            }
        }
        attachToLL.addView(ViewFactory.attachToEditTextFactory(ProjectsActivity.this, mRootRef, attachToLL));
        ll.addView(attachToLL);

        builder.setView(ll);

        //set on 'ok' listener
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String enteredName = nameInput.getText().toString();
                if (!enteredName.isEmpty()) {
                    final DatabaseReference child = mRootRef.child(Defines.TASKS_FOLDER);
                    if (name != null && !enteredName.equals(name)) {
                        child.child(name).removeValue();
                    }

                    child.child(enteredName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot ds : dataSnapshot.child(Defines.TASK_ATTACHED_PEOPLE).getChildren())
                            {
                                child.getRoot().child(Defines.USERS_FOLDER).child(mAuth.getCurrentUser().getUid())
                                        .child(Defines.PEOPLE_FOLDER).child(ds.getKey()).child(Defines.PERSON_TASKS).child(enteredName).removeValue();
                            }

                            for(DataSnapshot ds : dataSnapshot.child(Defines.TASK_ATTACHED_PROJECTS).getChildren())
                            {
                                child.getRoot().child(Defines.USERS_FOLDER).child(mAuth.getCurrentUser().getUid())
                                        .child(Defines.PROJECTS_FOLDER).child(ds.getKey()).child(Defines.PROJECT_TASKS).child(enteredName).removeValue();
                            }

                            Map<String, String> data = new HashMap<String, String>();
                            data.put(Defines.TASK_STATUS, String.valueOf(status));
                            data.put(Defines.TASK_DATE, dateInput.getText().toString());
                            data.put(Defines.TASK_DESCR, descriptionInput.getText().toString());
                            data.put(Defines.TASK_TIME, timeInput.getText().toString());
                            child.child(enteredName).setValue(data);

                            for(int i = 0; i < attachToLL.getChildCount() - 1; ++i)
                            {
                                DatabaseReference taskRef = child.child(enteredName);
                                String text = ((EditText)((LinearLayout)attachToLL.getChildAt(i)).getChildAt(0)).getText().toString();
                                String parts[] = text.split(":");
                                if(parts[0].equals("Person")) {
                                    taskRef.child(Defines.TASK_ATTACHED_PEOPLE).child(parts[1]).setValue(true);
                                    taskRef = taskRef.getRoot().child(Defines.USERS_FOLDER).child(mAuth.getCurrentUser().getUid())
                                            .child(Defines.PEOPLE_FOLDER).child(parts[1]).child(Defines.PERSON_TASKS).child(enteredName);
                                    taskRef.setValue(true);
                                }
                                else {
                                    taskRef.child(Defines.TASK_ATTACHED_PROJECTS).child(parts[1]).setValue(true);
                                    taskRef.getRoot().child(Defines.USERS_FOLDER).child(mAuth.getCurrentUser().getUid())
                                            .child(Defines.PROJECTS_FOLDER).child(parts[1]).child(Defines.PROJECT_TASKS).child(enteredName).setValue(true);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }
        });

        //set on 'cancel' listener
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        //show dialog
        builder.show();
    }

    private void AddNewProjectPerson()
    {
        AddNewProjectPerson(null, null);
    }

    private void AddNewProjectPerson(String name, String email) {
        //build dialog with request for name input
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectsActivity.this);
        builder.setTitle("Enter item info");

        final LinearLayout ll = new LinearLayout(ProjectsActivity.this);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);

        InputFilter nameFilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start;i < end;i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i)) && !Character.toString(source.charAt(i)).equals("_") && !Character.toString(source.charAt(i)).equals("-")
                        && !Character.toString(source.charAt(i)).equals(" ")) {
                        return "";
                    }
                }
                return null;
            }
        };

        final EditText input = new EditText(ProjectsActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Name");
        if(name != null)
            input.setText(name);
        input.setFilters(new InputFilter[] { nameFilter });

        final Spinner spinner = new Spinner(ProjectsActivity.this);
        final TextView tv = new TextView(ProjectsActivity.this);
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setTextSize(20);
        tv.setText("Type");
        tv.setPadding(10, 0, 0, 0);
        ll.addView(tv);

        spinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        final ArrayList<String> list = new ArrayList<>();
        list.add("Person");
        list.add("Project");
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.my_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        ll.addView(spinner);
        ll.addView(input);

        final EditText emailInput = new EditText(ProjectsActivity.this);
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setHint("Email");
        if(email != null)
            emailInput.setText(email);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0)
                    emailInput.setVisibility(View.VISIBLE);
                else
                    emailInput.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ll.addView(emailInput);

        builder.setView(ll);

        //set on 'ok' listener
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                String email = emailInput.getText().toString();
                if(name.isEmpty()) {
                    Toast.makeText(ProjectsActivity.this, "Name is not valid", Toast.LENGTH_SHORT).show();
                    AddNewProjectPerson(name, email);
                }
                else {
                    DatabaseReference child = mRootRef;
                    switch (spinner.getSelectedItemPosition()) {
                        case 0:
                            if(email.isEmpty() || !Defines.isEmailValid(email)) {
                                Toast.makeText(ProjectsActivity.this, "Email is not valid", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                AddNewProjectPerson(name, email);
                            }
                            child.child(Defines.PEOPLE_FOLDER).child(name).child(Defines.PERSON_PLACEHOLDER).setValue(true);
                            child.child(Defines.PEOPLE_FOLDER).child(name).child(Defines.PERSON_INFO)
                                    .child(Defines.PERSON_INFO_EMAIL).setValue(Defines.hlinkFromEmail(email));
                            break;
                        case 1:
                            child.child(Defines.PROJECTS_FOLDER).child(name).child(Defines.PROJECT_PLACEHOLDER).setValue(true);
                    }
                }
            }
        });

        //set on 'cancel' listener
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        //show dialog
        builder.show();
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

    @Override
    protected void onRestart() {
        ((LinearLayout) findViewById(R.id.myLinearLayout)).removeAllViews();
        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //adds menu/menu_projects.xml here
        getMenuInflater().inflate(R.menu.menu_projects, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                HideAllNonMatches(query);
                return true;
            }
        });

        searchView.findViewById(R.id.search_close_btn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchView.setQuery("", false);
                        searchView.setIconified(true);

                        int position = mSearchOptions.getSelectedItemPosition();
                        HideItemsBySearchOptions(position);
                    }
                });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_signout) {
            mAuth.signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void HideAllNonMatches(String text) {
        LinearLayout ll = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem()).GetLinearLayout();
        switch (mViewPager.getCurrentItem())
        {
            case Defines.PROJECTS_FRAGMENT:
                for (int i = 0; i < ll.getChildCount() - 1; i += 2) {
                    String llText = ((TextView) ((LinearLayout) ll.getChildAt(i)).getChildAt(Defines.TEXT_VIEW_POSITION)).getText().toString();
                    if (text == null || llText.contains(text)) {
                        ll.getChildAt(i).setVisibility(View.VISIBLE);
                        ll.getChildAt(i + 1).setVisibility(View.VISIBLE);
                    } else {
                        ll.getChildAt(i).setVisibility(View.GONE);
                        ll.getChildAt(i + 1).setVisibility(View.GONE);
                    }
                }
                break;

            case Defines.TASKS_FRAGMENT:
                for(int j = 0; j < ll.getChildCount() - 1; ++j) {
                    LinearLayout minorLL = (LinearLayout)ll.getChildAt(j);
                    for (int i = 2; i < minorLL.getChildCount(); i += Defines.ITEM_SIZE_IN_VIEWS) {
                        String llText = ((TextView) ((LinearLayout) minorLL.getChildAt(i)).getChildAt(Defines.TEXT_VIEW_POSITION)).getText().toString();
                        if (text == null || llText.contains(text)) {
                            minorLL.getChildAt(i).setVisibility(View.VISIBLE);
                            minorLL.getChildAt(i + 1).setVisibility(View.VISIBLE);
                        } else {
                            minorLL.getChildAt(i).setVisibility(View.GONE);
                            minorLL.getChildAt(i + 1).setVisibility(View.GONE);
                        }
                    }
                }
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                last_context_selected.callOnClick();
                break;
            case 2:
                final String key = ((TextView) ((LinearLayout) last_context_selected).getChildAt(Defines.TEXT_VIEW_POSITION)).getText().toString();
                switch ((Defines.LinearLayoutType) last_context_selected.getTag()) {
                    case PERSON:
                        final DatabaseReference childPeople = mRootRef.child(Defines.PEOPLE_FOLDER).child(key);
                        childPeople.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.child(Defines.PERSON_PROJECTS).getChildren()) {
                                    childPeople.getRoot().child(Defines.USERS_FOLDER).child(mAuth.getCurrentUser().getUid())
                                            .child(Defines.PROJECTS_FOLDER).child(ds.getKey()).child(Defines.PROJECT_PEOPLE).child(key).removeValue();
                                }
                                for (DataSnapshot ds : dataSnapshot.child(Defines.PERSON_TASKS).getChildren())
                                {
                                    childPeople.getRoot().child(Defines.USERS_FOLDER).child(mAuth.getCurrentUser().getUid())
                                            .child(Defines.TASKS_FOLDER).child(ds.getKey()).child(Defines.TASK_ATTACHED_PEOPLE).child(key).removeValue();
                                }
                                childPeople.removeValue();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        break;
                    case PROJECT:
                        final DatabaseReference childProjects = mRootRef.child(Defines.PROJECTS_FOLDER).child(key);
                        childProjects.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.child(Defines.PROJECT_PEOPLE).getChildren()) {
                                    childProjects.getRoot().child(Defines.USERS_FOLDER).child(mAuth.getCurrentUser().getUid())
                                            .child(Defines.PEOPLE_FOLDER).child(ds.getKey()).child(Defines.PERSON_PROJECTS).child(key).removeValue();
                                }
                                for (DataSnapshot ds : dataSnapshot.child(Defines.PROJECT_TASKS).getChildren())
                                {
                                    childProjects.getRoot().child(Defines.USERS_FOLDER).child(mAuth.getCurrentUser().getUid())
                                            .child(Defines.TASKS_FOLDER).child(ds.getKey()).child(Defines.TASK_ATTACHED_PROJECTS).child(key).removeValue();
                                }
                                childProjects.removeValue();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        break;
                    case TASK:
                        final DatabaseReference childTasks = mRootRef.child(Defines.TASKS_FOLDER).child(key);
                        childTasks.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.child(Defines.TASK_ATTACHED_PEOPLE).getChildren()) {
                                    childTasks.getRoot().child(Defines.USERS_FOLDER).child(mAuth.getCurrentUser().getUid())
                                            .child(Defines.PEOPLE_FOLDER).child(ds.getKey()).child(Defines.PERSON_TASKS).child(key).removeValue();
                                }
                                for (DataSnapshot ds : dataSnapshot.child(Defines.TASK_ATTACHED_PROJECTS).getChildren())
                                {
                                    childTasks.getRoot().child(Defines.USERS_FOLDER).child(mAuth.getCurrentUser().getUid())
                                            .child(Defines.PROJECTS_FOLDER).child(ds.getKey()).child(Defines.PROJECT_TASKS).child(key).removeValue();
                                }
                                childTasks.removeValue();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                }
                break;
        }
        return super.onContextItemSelected(item);
    }

    private View last_context_selected;

    @Override
    public void onCreateContextMenu(ContextMenu menu,
                                    View view,
                                    ContextMenu.ContextMenuInfo info) {
        last_context_selected = view;
        if (view instanceof LinearLayout) {
            menu.setHeaderTitle("Choose an action");
            menu.add(Menu.NONE, 1, Menu.NONE, "Open");
            menu.add(Menu.NONE, 2, Menu.NONE, "Delete");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    public static class ActivityFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        private LinearLayout mLinearLayout;
        private LinearLayout mTodayTasks = null;
        private LinearLayout mWeekTasks = null;
        private LinearLayout mMonthTasks = null;
        private LinearLayout mOtherTasks = null;

        private FirebaseAuth mAuth;
        private FirebaseAuth.AuthStateListener mAuthStateListener;
        private DatabaseReference mRootRef;

        public LinearLayout GetLinearLayout() {
            return mLinearLayout;
        }

        public ActivityFragment() {
        }

        public static ActivityFragment newInstance(int sectionNumber) {
            ActivityFragment fragment = new ActivityFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_projects, container, false);
            mLinearLayout = (LinearLayout) rootView.findViewById(R.id.myLinearLayout);

            mAuth = FirebaseAuth.getInstance();
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() != null) {
                        String uid = firebaseAuth.getCurrentUser().getUid();
                        mRootRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                        switch (getArguments().getInt(ARG_SECTION_NUMBER) - 1) {
                            case Defines.PROJECTS_FRAGMENT:
                                mLinearLayout.addView(ViewFactory.placeholderFactory(getActivity()));
                                SetUpProjectsPersonList(mRootRef);
                                break;
                            case Defines.TASKS_FRAGMENT:
                                mLinearLayout.addView(mTodayTasks = ViewFactory.titledLinearLayoutFactory(getActivity(), "Less than a day"));
                                mLinearLayout.addView(mWeekTasks = ViewFactory.titledLinearLayoutFactory(getActivity(), "7 days"));
                                mLinearLayout.addView(mMonthTasks = ViewFactory.titledLinearLayoutFactory(getActivity(), "30 days"));
                                mLinearLayout.addView(mOtherTasks = ViewFactory.titledLinearLayoutFactory(getActivity(), "Ton of time"));
                                mLinearLayout.addView(ViewFactory.placeholderFactory(getActivity()));

                                SetUpTaskList(mRootRef);
                                break;
                        }
                    }
                }
            };

            return rootView;
        }

        private void Refresh()
        {
            mLinearLayout.removeAllViews();
            mLinearLayout.addView(ViewFactory.placeholderFactory(getActivity()));
            if (peopleCEV != null) {
                mRootRef.child(Defines.PEOPLE_FOLDER).removeEventListener(peopleCEV);
                mRootRef.child(Defines.PEOPLE_FOLDER).addChildEventListener(peopleCEV);
            }
            if (projectsCEV != null) {
                mRootRef.child(Defines.PROJECTS_FOLDER).removeEventListener(projectsCEV);
                mRootRef.child(Defines.PROJECTS_FOLDER).addChildEventListener(projectsCEV);
            }
        }

        @Override
        public void onStart() {
            super.onStart();
            mLinearLayout.removeAllViews();
            mAuth.addAuthStateListener(mAuthStateListener);
        }

        @Override
        public void onStop() {
            super.onStop();
            if (mAuthStateListener != null) {
                mAuth.removeAuthStateListener(mAuthStateListener);
            }
            if (peopleCEV != null)
                mRootRef.child(Defines.PEOPLE_FOLDER).removeEventListener(peopleCEV);
            if (projectsCEV != null)
                mRootRef.child(Defines.PROJECTS_FOLDER).removeEventListener(projectsCEV);
            if (tasksCEV != null)
                mRootRef.child(Defines.TASKS_FOLDER).removeEventListener(tasksCEV);
        }

        private ChildEventListener tasksCEV = null;
        private ChildEventListener peopleCEV = null;
        private ChildEventListener projectsCEV = null;

        private void SetUpProjectsPersonList(DatabaseReference dr) {
            projectsCEV = dr.child(Defines.PROJECTS_FOLDER).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    AddItem(dataSnapshot.getKey(), Defines.LinearLayoutType.PROJECT);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Refresh();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            peopleCEV = dr.child(Defines.PEOPLE_FOLDER).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if(!dataSnapshot.child(Defines.PERSON_PROJECTS).exists())
                        AddItem(dataSnapshot.getKey(), Defines.LinearLayoutType.PERSON);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    RemovePersonOrProject(dataSnapshot.getKey());
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        private void SetUpTaskList(DatabaseReference dr) {
            tasksCEV = dr.child(Defines.TASKS_FOLDER).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String name = dataSnapshot.getKey();
                    boolean status = Boolean.parseBoolean((String) dataSnapshot.child(Defines.TASK_STATUS).getValue());
                    String date = (String) dataSnapshot.child(Defines.TASK_DATE).getValue();
                    String time = (String) dataSnapshot.child(Defines.TASK_TIME).getValue();
                    PrepareToAddTask(name, status, date, time);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    RemoveTaskByText(dataSnapshot.getKey());
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        void ChangeCheckBoxStatus(String name, boolean newStatus) {
            int i = FindLinearLayoutByText(name);
            ((CheckBox) ((LinearLayout) mLinearLayout.getChildAt(i)).getChildAt(0)).setChecked(newStatus);
        }

        private void PrepareToAddTask(String name, boolean status, String date, String time) {
            Defines.TaskType tt = Defines.TaskType.OTHER;

            if (!date.isEmpty()) {
                String[] parts = date.split("\\.");

                String s = parts[0];
                Calendar taskDate = Calendar.getInstance();
                if(time.isEmpty()) {
                    taskDate.set((int) Integer.parseInt(parts[2]), (int) Integer.parseInt(parts[1]) - 1, (int) Integer.parseInt(parts[0]));
                }
                else {
                    String[] time_parts = time.split(":");
                    taskDate.set((int) Integer.parseInt(parts[2]), (int) Integer.parseInt(parts[1]) - 1, (int) Integer.parseInt(parts[0]),
                            (int)Integer.parseInt(time_parts[0]), (int)Integer.parseInt(time_parts[1]));
                }

                Calendar currDate = Calendar.getInstance();

                long diffInMS = taskDate.getTime().getTime() - currDate.getTime().getTime();
                TimeUnit tu = TimeUnit.DAYS;
                long diffInDays = tu.convert(diffInMS, TimeUnit.MILLISECONDS);

                if (diffInDays <= 1)
                    tt = Defines.TaskType.TODAY;
                else if (diffInDays < 7)
                    tt = Defines.TaskType.WEEK;
                else if (diffInDays < 30)
                    tt = Defines.TaskType.MONTH;
            }

            AddItem(name, status, tt);
        }

        private void AddItem(String name, boolean checkBoxStatus, Defines.TaskType tt) {
            final LinearLayout ll = ViewFactory.linearLayoutFactory(getActivity(), name, checkBoxStatus);

            ll.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    registerForContextMenu(view);
                    getActivity().openContextMenu(view);
                    return true;
                }
            });

            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name = ((TextView) ((LinearLayout) view).getChildAt(Defines.TEXT_VIEW_POSITION)).getText().toString();
                    ((ProjectsActivity) getActivity()).AddOrEditTask(name);
                }
            });


            ((CheckBox) ll.getChildAt(ViewFactory.LINEAR_LAYOUT_CHECKBOX_POSITION)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    String name = (String) ((CheckBox) ll.getChildAt(ViewFactory.LINEAR_LAYOUT_CHECKBOX_POSITION)).getTag();
                    mRootRef.child(Defines.TASKS_FOLDER).child(name).child(Defines.TASK_STATUS).setValue(String.valueOf(b));
                    ((ProjectsActivity) getActivity()).RefreshCurrentFragment();
                }
            });

            switch (tt) {
                case TODAY:
                    AddTaskToLL(ll, mTodayTasks);
                    break;
                case WEEK:
                    AddTaskToLL(ll, mWeekTasks);
                    break;
                case MONTH:
                    AddTaskToLL(ll, mMonthTasks);
                    break;
                case OTHER:
                    AddTaskToLL(ll, mOtherTasks);
                    break;
            }
        }

        private void CheckIfLLIsEmpty(LinearLayout ll) {
            if (ll.getChildCount() == 2)
                ll.setVisibility(View.GONE);
        }

        private void AddTaskToLL(LinearLayout task, LinearLayout whereAdd) {
            whereAdd.addView(task);
            whereAdd.addView(ViewFactory.horizontalDividerFactory(getActivity()));
            whereAdd.setVisibility(View.VISIBLE);
        }

        private void AddItem(String name, Defines.LinearLayoutType llt) {
            LinearLayout ll = ViewFactory.linearLayoutFactory(getActivity(), name, llt);

            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), ((Defines.LinearLayoutType) view.getTag() == Defines.LinearLayoutType.PERSON ? ProfileView.class : ProjectsView.class));
                    String extraInfo = ((TextView) ((LinearLayout) view).getChildAt(Defines.TEXT_VIEW_POSITION)).getText().toString();
                    intent.putExtra(Defines.INFO_TAG, extraInfo);
                    startActivity(intent);
                }
            });

            ll.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    registerForContextMenu(view);
                    getActivity().openContextMenu(view);
                    return true;
                }
            });

            mLinearLayout.addView(ll, mLinearLayout.getChildCount() - 1);
            mLinearLayout.addView(ViewFactory.horizontalDividerFactory(getActivity()), mLinearLayout.getChildCount() - 1);
        }

        private void RemoveTaskByText(String text) {
            for (int i = 0; i < mLinearLayout.getChildCount(); ++i) {
                LinearLayout mll = (LinearLayout) mLinearLayout.getChildAt(i);
                for (int j = 2; j < mll.getChildCount(); j += Defines.ITEM_SIZE_IN_VIEWS) {
                    String textViewText = ((TextView) ((LinearLayout) mll.getChildAt(j)).getChildAt(Defines.TEXT_VIEW_POSITION)).getText().toString();
                    if (textViewText.equals(text)) {
                        mll.removeViews(j, 2);
                        CheckIfLLIsEmpty(mll);
                        return;
                    }
                }
            }
        }

        private void RemovePersonOrProject(String name) {
            int position = FindLinearLayoutByText(name);
            mLinearLayout.removeViews(position, Defines.ITEM_SIZE_IN_VIEWS);
        }

        private int FindLinearLayoutByText(String text) {
            int childCount = mLinearLayout.getChildCount();
            int i;
            for (i = 0; i < childCount - 1; i += Defines.ITEM_SIZE_IN_VIEWS) {
                String llText = ((TextView) ((LinearLayout) mLinearLayout.getChildAt(i)).getChildAt(Defines.TEXT_VIEW_POSITION)).getText().toString();
                if (llText.equals(text)) {
                    break;
                }
            }
            return i;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final static int PAGE_COUNT = 2;
        private SparseArray<ActivityFragment> mFragments = new SparseArray<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return ActivityFragment.newInstance(position + 1);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ActivityFragment fragment = (ActivityFragment) super.instantiateItem(container, position);
            mFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public ActivityFragment getFragment(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            //projects and tasks
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Projects and People";
                case 1:
                    return "Tasks";
            }
            return null;
        }
    }
}