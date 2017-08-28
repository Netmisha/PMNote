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
import android.text.InputType;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.Gravity;
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
                    for (int i = 0; i < ll.getChildCount(); ++i) {
                        ll.getChildAt(i).setVisibility(View.VISIBLE);
                    }
                    break;
                case 1:
                    for (int i = 0; i < ll.getChildCount(); i += 2) {
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
                    for (int i = 0; i < ll.getChildCount(); i += 2) {
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
                    for (int j = 0; j < ll.getChildCount(); ++j) {
                        LinearLayout mll = (LinearLayout) ll.getChildAt(j);
                        for (int i = 0; i < mll.getChildCount(); ++i) {
                            mll.getChildAt(i).setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case 1:
                    for (int j = 0; j < ll.getChildCount(); ++j) {
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
                case 2:
                    for (int j = 0; j < ll.getChildCount(); ++j) {
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
                Defines.SetArrayList(mCurrentSpinnerList,
                        (position == Defines.PROJECTS_FRAGMENT ? Defines.LinearLayoutType.PROJECT : Defines.LinearLayoutType.TASK));
                mArrayAdapter.notifyDataSetChanged();

                int temp = mSearchOptions.getSelectedItemPosition();
                mSearchOptions.setSelection(mSavedSpinnerPosition);
                mSavedSpinnerPosition = temp;
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
                    mRootRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
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
        mRootRef.child("Tasks").child(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String date = null, time = null, descr = null;
                boolean status = Boolean.parseBoolean((String) dataSnapshot.child("Status").getValue());
                ArrayList<String> attachedToList = new ArrayList<String>();
                if (dataSnapshot.child("Date").exists()) {
                    date = dataSnapshot.child("Date").getValue(String.class);
                }
                if (dataSnapshot.child("Time").exists()) {
                    time = dataSnapshot.child("Time").getValue(String.class);
                }
                if (dataSnapshot.child("Description").exists()) {
                    descr = dataSnapshot.child("Description").getValue(String.class);
                }
                if(dataSnapshot.child("Projects").exists())
                {
                    for(DataSnapshot ds : dataSnapshot.child("Projects").getChildren())
                        attachedToList.add("Project:" + ds.getKey());
                }
                if(dataSnapshot.child("People").exists())
                {
                    for(DataSnapshot ds : dataSnapshot.child("People").getChildren())
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
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);

        final EditText nameInput = new EditText(ProjectsActivity.this);
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        nameInput.setHint("Name");
        nameInput.setText(name != null ? name : "");
        ll.addView(nameInput);

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
                            timeInput.setText(hour + ":" + minute);
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
        attachToLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
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
                String enteredName = nameInput.getText().toString();
                if (!enteredName.isEmpty()) {
                    DatabaseReference child = mRootRef.child("Tasks");
                    if (name != null && !enteredName.equals(name)) {
                        child.child(name).removeValue();
                    }
                    child = child.child(enteredName);
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("Status", String.valueOf(status));
                    data.put("Date", dateInput.getText().toString());
                    data.put("Description", descriptionInput.getText().toString());
                    data.put("Time", timeInput.getText().toString());
                    child.setValue(data);

                    for(int i = 0; i < attachToLL.getChildCount() - 1; ++i)
                    {
                        String text = ((EditText)attachToLL.getChildAt(i)).getText().toString();
                        String parts[] = text.split(":");
                        if(parts[0].equals("Person")) {
                            child.child("People").child(parts[1]).setValue(true);
                            child = child.getRoot().child("Users").child(mAuth.getCurrentUser().getUid()).child("People").child(parts[1]).child("Tasks").child(enteredName);
                            child.setValue(true);
                        }
                        else {
                            child.child("Projects").child(parts[1]).setValue(true);
                            child.getRoot().child("Users").child(mAuth.getCurrentUser().getUid()).child("Projects").child(parts[1]).child("Tasks").child(enteredName).setValue(true);
                        }
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

    private void AddNewProjectPerson() {
        //build dialog with request for name input
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectsActivity.this);
        builder.setTitle("Enter item info");

        final LinearLayout ll = new LinearLayout(ProjectsActivity.this);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);

        final EditText input = new EditText(ProjectsActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Name");

        ll.addView(input);

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

        builder.setView(ll);

        //set on 'ok' listener
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                DatabaseReference child = mRootRef;
                switch (spinner.getSelectedItemPosition()) {
                    case 0:
                        child.child("People").child(name).child("None").setValue(true);
                        break;
                    case 1:
                        child.child("Projects").child(name).child("Status").setValue(String.valueOf(true));
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

                        HideItemsBySearchOptions(mSearchOptions.getSelectedItemPosition());
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
        for (int i = 0; i < ll.getChildCount(); i += 2) {
            String llText = ((TextView) ((LinearLayout) ll.getChildAt(i)).getChildAt(Defines.TEXT_VIEW_POSITION)).getText().toString();
            if (text == null || llText.contains(text)) {
                ll.getChildAt(i).setVisibility(View.VISIBLE);
                ll.getChildAt(i + 1).setVisibility(View.VISIBLE);
            } else {
                ll.getChildAt(i).setVisibility(View.GONE);
                ll.getChildAt(i + 1).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                last_context_selected.callOnClick();
                break;
            case 2:
                String key = ((TextView) ((LinearLayout) last_context_selected).getChildAt(Defines.TEXT_VIEW_POSITION)).getText().toString();
                DatabaseReference child = null;
                switch ((Defines.LinearLayoutType) last_context_selected.getTag()) {
                    case PERSON:
                        child = mRootRef.child("People");
                        break;
                    case PROJECT:
                        child = mRootRef.child("Projects");
                        break;
                    case TASK:
                        child = mRootRef.child("Tasks");
                        break;
                }
                child.child(key).removeValue();
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
                                SetUpProjectsPersonList(mRootRef);
                                break;
                            case Defines.TASKS_FRAGMENT:
                                mLinearLayout.addView(mTodayTasks = ViewFactory.titledLinearLayoutFactory(getActivity(), "Less than a day"));
                                mLinearLayout.addView(mWeekTasks = ViewFactory.titledLinearLayoutFactory(getActivity(), "7 days"));
                                mLinearLayout.addView(mMonthTasks = ViewFactory.titledLinearLayoutFactory(getActivity(), "30 days"));
                                mLinearLayout.addView(mOtherTasks = ViewFactory.titledLinearLayoutFactory(getActivity(), "Ton of time"));

                                SetUpTaskList(mRootRef);
                                break;
                        }
                    }
                }
            };

            return rootView;
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
                mRootRef.child("People").removeEventListener(peopleCEV);
            if (projectsCEV != null)
                mRootRef.child("Projects").removeEventListener(projectsCEV);
            if (tasksCEV != null)
                mRootRef.child("Tasks").removeEventListener(tasksCEV);
        }

        private ChildEventListener tasksCEV = null;
        private ChildEventListener peopleCEV = null;
        private ChildEventListener projectsCEV = null;

        private void SetUpProjectsPersonList(DatabaseReference dr) {
            projectsCEV = dr.child("Projects").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    AddItem(dataSnapshot.getKey(), Defines.LinearLayoutType.PROJECT);
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

            peopleCEV = dr.child("People").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
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
            tasksCEV = dr.child("Tasks").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String name = dataSnapshot.getKey();
                    boolean status = Boolean.parseBoolean((String) dataSnapshot.child("Status").getValue());
                    String date = (String) dataSnapshot.child("Date").getValue();
                    PrepareToAddTask(name, status, date);
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

        private void PrepareToAddTask(String name, boolean status, String date) {
            Defines.TaskType tt = Defines.TaskType.OTHER;

            if (!date.isEmpty()) {
                String[] parts = date.split("\\.");

                String s = parts[0];
                Calendar taskDate = Calendar.getInstance();
                taskDate.set((int) Integer.parseInt(parts[2]), (int) Integer.parseInt(parts[1]) - 1, (int) Integer.parseInt(parts[0]));
                Calendar currDate = Calendar.getInstance();

                long diffInMS = taskDate.getTime().getTime() - currDate.getTime().getTime();
                TimeUnit tu = TimeUnit.DAYS;
                long diffInDays = tu.convert(diffInMS, TimeUnit.MILLISECONDS);

                if (diffInDays < 1)
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
                    mRootRef.child("Tasks").child(name).child("Status").setValue(String.valueOf(b));
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

            mLinearLayout.addView(ll);
            mLinearLayout.addView(ViewFactory.horizontalDividerFactory(getActivity()));
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
            for (i = 0; i < childCount; i += Defines.ITEM_SIZE_IN_VIEWS) {
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