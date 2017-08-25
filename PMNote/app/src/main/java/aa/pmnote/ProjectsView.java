package aa.pmnote;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.InputType;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ProjectsView extends AppCompatActivity {

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

    private String mProjectName;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mProjectName = getIntent().getStringExtra(Defines.INFO_TAG);
        setTitle(Defines.PROJECT_FRAGMENT_TITLE + "'" + mProjectName + "'");

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position)
                {
                    case Defines.PROJECT_FRAGMENT:
                        setTitle(Defines.PROJECT_FRAGMENT_TITLE + "'" + mProjectName + "'");
                        break;
                    case Defines.PROJECT_TASKS_FRAGMENT:
                        setTitle("'" + mProjectName + "'" + Defines.PROJECT_TASKS_FRAGMENT_TITLE);
                        break;
                }
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null)
                {
                    finish();
                }
                else
                {
                    String uid = firebaseAuth.getCurrentUser().getUid();
                    mRootRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Projects").child(mProjectName);
                }
            }
        };


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(mViewPager.getCurrentItem())
                {
                    case Defines.PROJECT_FRAGMENT:
                        AddNewPerson();
                        break;
                    case Defines.PROJECT_TASKS_FRAGMENT:
                        AddOrEditTask();
                        break;
                }
            }
        });
    }

    private void AddNewPerson() {
        //build dialog with request for name input
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectsView.this);
        builder.setTitle("Enter item info");

        final LinearLayout ll = new LinearLayout(ProjectsView.this);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);

        final EditText input = new EditText(ProjectsView.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Name");

        ll.addView(input);

        builder.setView(ll);

        //set on 'ok' listener
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                DatabaseReference child = mRootRef;
                child.child("People").child(name).setValue(true);

                child = child.getRoot().child("users").child(mAuth.getCurrentUser().getUid()).child("people").child(name);
                child.child("None").setValue(true);
                child.child("Projects").child(mProjectName).setValue(true);
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

    public void AddOrEditTask()
    {
        AddOrEditTask(null, null, null, null, false);
    }

    public void AddOrEditTask(final String name)
    {
        mRootRef.child("Tasks").child(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String date = null, time = null, descr = null;
                boolean status = (boolean)dataSnapshot.child("Status").getValue();
                if(dataSnapshot.child("Date").exists()) {
                    date = dataSnapshot.child("Date").getValue(String.class);
                }
                if(dataSnapshot.child("Time").exists()) {
                    time = dataSnapshot.child("Time").getValue(String.class);
                }
                if(dataSnapshot.child("Description").exists()) {
                    descr = dataSnapshot.child("Description").getValue(String.class);
                }

                AddOrEditTask(name, date, time, descr, status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void AddOrEditTask(final String name, final String date, final String time, final String description, final boolean status) {
        //build dialog with request for name input
        AlertDialog.Builder builder = new AlertDialog.Builder(ProjectsView.this);
        builder.setTitle("Edit task");

        final LinearLayout ll = new LinearLayout(ProjectsView.this);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);

        final EditText nameInput = new EditText(ProjectsView.this);
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        nameInput.setHint("Name");
        nameInput.setText(name != null ? name : "");
        ll.addView(nameInput);

        final EditText timeInput = new EditText(ProjectsView.this);
        timeInput.setKeyListener(null);
        timeInput.setHint("Expire Time");
        timeInput.setText(time != null ? time : "");
        if(date == null) {
            timeInput.setVisibility(View.GONE);
        }
        timeInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    Calendar c = Calendar.getInstance();
                    TimePickerDialog tpd = new TimePickerDialog(ProjectsView.this, new TimePickerDialog.OnTimeSetListener() {
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

        final EditText dateInput = new EditText(ProjectsView.this);
        dateInput.setKeyListener(null);
        dateInput.setHint("Expire Date");
        dateInput.setText(date != null ? date : "");
        dateInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    Calendar c = Calendar.getInstance();
                    DatePickerDialog dpd = new DatePickerDialog(ProjectsView.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            Calendar date = Calendar.getInstance();
                            date.set(year, month, day);
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
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

        final EditText descriptionInput = new EditText(ProjectsView.this);
        descriptionInput.setHint("Description");
        descriptionInput.setText(description != null ? description : "");
        ll.addView(descriptionInput);

        builder.setView(ll);

        //set on 'ok' listener
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredName = nameInput.getText().toString();
                if(!enteredName.isEmpty()) {
                    DatabaseReference child = mRootRef;
                    if (name != null && !enteredName.equals(name)) {
                        child.child("Tasks").child(name).removeValue();

                        child = mRootRef.getParent().getParent().child("tasks");
                        child.child(name).removeValue();
                        child = child.child(enteredName);
                    } else {
                        child = child.getParent().getParent().child("tasks").child(enteredName);
                    }

                    child.child("Status").setValue(status);
                    child.child("Date").setValue(dateInput.getText().toString());
                    child.child("Description").setValue(descriptionInput.getText().toString());
                    child.child("Time").setValue(timeInput.getText().toString());

                    mRootRef.child("Tasks").child(enteredName).setValue(true);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        switch(mViewPager.getCurrentItem())
        {
            case Defines.PROJECT_FRAGMENT:
                getMenuInflater().inflate(R.menu.menu_projects_view, menu);
                break;
            case Defines.PROJECT_TASKS_FRAGMENT:
                getMenuInflater().inflate(R.menu.menu_projects_tasks_view, menu);
                break;
        }
        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        String key = ((TextView)((LinearLayout)last_context_selected).getChildAt(Defines.TEXT_VIEW_POSITION)).getText().toString();;
        DatabaseReference child = null;
        switch(item.getItemId())
        {
            case 1:
                last_context_selected.callOnClick();
                break;
            case 2:
                switch ((Defines.LinearLayoutType)last_context_selected.getTag())
                {
                    case PERSON:
                        mRootRef.child("People").child(key).removeValue();
                        mRootRef.getRoot().child("Users").child(mAuth.getCurrentUser().getUid()).child("People").child(key).child("Projects").child(mProjectName).removeValue();
                        break;
                    case TASK:
                        mRootRef.child("Tasks").child(key).removeValue();
                        mRootRef.getRoot().child("Users").child(mAuth.getCurrentUser().getUid()).child("Tasks").child(key).child("Projects").child(mProjectName).removeValue();
                        break;
                }
                break;
            case 3:
                switch ((Defines.LinearLayoutType)last_context_selected.getTag())
                {
                    case PERSON:
                        mRootRef.child("People").child(key).removeValue();
                        child = mRootRef.getRoot().child("Users").child(mAuth.getCurrentUser().getUid()).child("People");
                        break;
                    case TASK:
                        mRootRef.child("users").child(key).removeValue();
                        child = mRootRef.getRoot().child("Users").child(mAuth.getCurrentUser().getUid()).child("Tasks");
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
                                    ContextMenu.ContextMenuInfo info)
    {
        last_context_selected = view;
        if(view instanceof LinearLayout) {
            menu.setHeaderTitle("Choose an action");
            menu.add(Menu.NONE, 1, Menu.NONE, "Open");
            menu.add(Menu.NONE, 2, Menu.NONE, "Remove from project");
            menu.add(Menu.NONE, 3, Menu.NONE, "Delete");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_signout:
                mAuth.signOut();
                return true;
            case R.id.action_addperson:
                AddPerson();
                return true;
            case R.id.action_addtask:
                AddTask();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void AddTask()
    {
        mRootRef.getRoot().child("Users").child(mAuth.getCurrentUser().getUid()).child("Tasks").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> nonAddedUsers = new ArrayList<String>();
                for(DataSnapshot dr : dataSnapshot.getChildren())
                {
                    if(!dr.child("Projects").child(mProjectName).exists())
                    {
                        nonAddedUsers.add(dr.getKey());
                    }
                }

                ListView lv = new ListView(ProjectsView.this);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ProjectsView.this, android.R.layout.simple_list_item_1, nonAddedUsers);
                lv.setAdapter(arrayAdapter);

                final AlertDialog.Builder builder = new AlertDialog.Builder(ProjectsView.this);
                builder.setTitle("Choose task to add");
                builder.setView(lv);

                final AlertDialog ad = builder.create();

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String taskToAdd = ((TextView)view).getText().toString();
                        mRootRef.child("Tasks").child(taskToAdd).setValue(true);
                        mRootRef.getRoot().child("Users").child(mAuth.getCurrentUser().getUid()).child("People").child(taskToAdd).child("Projects").child(mProjectName).setValue(true);
                        ad.cancel();
                    }
                });

                ad.show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void AddPerson()
    {
        mRootRef.getRoot().child("users").child(mAuth.getCurrentUser().getUid()).child("people").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> nonAddedUsers = new ArrayList<String>();
                for(DataSnapshot dr : dataSnapshot.getChildren())
                {
                    if(!dr.child("projects").child(mProjectName).exists())
                    {
                        nonAddedUsers.add(dr.getKey());
                    }
                }

                ListView lv = new ListView(ProjectsView.this);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ProjectsView.this, android.R.layout.simple_list_item_1, nonAddedUsers);
                lv.setAdapter(arrayAdapter);

                final AlertDialog.Builder builder = new AlertDialog.Builder(ProjectsView.this);
                builder.setTitle("Choose user to add");
                builder.setView(lv);

                final AlertDialog ad = builder.create();

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String userToAdd = ((TextView)view).getText().toString();
                        mRootRef.child("people").child(userToAdd).setValue(true);
                        mRootRef.getRoot().child("users").child(mAuth.getCurrentUser().getUid()).child("people").child(userToAdd).child("projects").child(mProjectName).setValue(true);
                        ad.cancel();
                    }
                });

                ad.show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public DatabaseReference GetRootRef()
    {
        return mRootRef;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ActivityFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private LinearLayout mLinearLayout;

        private FirebaseAuth mAuth;
        private FirebaseAuth.AuthStateListener mAuthStateListener;
        private DatabaseReference mRoot;

        private Spinner mSearchCategory;
        private ArrayAdapter<String> mArrayAdapter;
        private ArrayList<String> mSpinnerList = new ArrayList<>();

        public ActivityFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
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
            View rootView = inflater.inflate(R.layout.fragment_project_tasks_view, container, false);
            mLinearLayout = (LinearLayout)rootView.findViewById(R.id.projectTasksLayout);
            mAuth = FirebaseAuth.getInstance();
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if(firebaseAuth.getCurrentUser() != null)
                    {
                        mRoot = ((ProjectsView)getActivity()).GetRootRef();
                        SetUpTasksListener();
                    }
                }
            };

            if(getArguments().getInt(ARG_SECTION_NUMBER) - 1 == Defines.PROJECT_TASKS_FRAGMENT) {
                mSearchCategory = new Spinner(getActivity());
                mSearchCategory.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                ((LinearLayout)rootView.findViewById(R.id.mainLL)).addView(mSearchCategory, 0);

                mSpinnerList.add("Show all");
                mSpinnerList.add("Show open");
                mSpinnerList.add("Show completed");

                mArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.my_spinner_item, mSpinnerList);
                mSearchCategory.setAdapter(mArrayAdapter);
                mArrayAdapter.notifyDataSetChanged();

                mSearchCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                        HideItemsBySearchOptions(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        //do nothing, duh
                    }
                });
            }
            return rootView;
        }

        private void HideItemsBySearchOptions(int search_option) {
            LinearLayout ll = mLinearLayout;
            switch (search_option) {
                case 0:
                    for (int i = 0; i < ll.getChildCount(); ++i) {
                        ll.getChildAt(i).setVisibility(View.VISIBLE);
                    }
                    break;
                case 1:
                    for (int i = 0; i < ll.getChildCount(); i += 2) {
                        boolean isCompeted = ((CheckBox) ((LinearLayout) ll.getChildAt(i)).getChildAt(ViewFactory.LINEAR_LAYOUT_CHECKBOX_POSITION)).isChecked();
                        if (isCompeted) {
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
                        boolean isCompeted = ((CheckBox) ((LinearLayout) ll.getChildAt(i)).getChildAt(ViewFactory.LINEAR_LAYOUT_CHECKBOX_POSITION)).isChecked();
                        if (!isCompeted) {
                            ll.getChildAt(i).setVisibility(View.GONE);
                            ll.getChildAt(i + 1).setVisibility(View.GONE);
                        } else {
                            ll.getChildAt(i).setVisibility(View.VISIBLE);
                            ll.getChildAt(i + 1).setVisibility(View.VISIBLE);
                        }
                    }
                    break;
            }

        }

        private int FindLinearLayoutByText(String text)
        {
            int childCount = mLinearLayout.getChildCount();
            int i;
            for(i = 0; i < childCount; i += Defines.ITEM_SIZE_IN_VIEWS)
            {
                String llText =((TextView)((LinearLayout)mLinearLayout.getChildAt(i)).getChildAt(Defines.TEXT_VIEW_POSITION)).getText().toString();
                if(llText.equals(text)) {
                    break;
                }
            }
            return i;
        }

        private void RemoveItem(String name)
        {
            int i = FindLinearLayoutByText(name);
            mLinearLayout.removeViews(i, Defines.ITEM_SIZE_IN_VIEWS);
        }

        private ChildEventListener tasksCEV = null;
        private ChildEventListener peopleCEV = null;

        private CompoundButton.OnCheckedChangeListener ccl = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String taskName = (String)compoundButton.getTag();
                mRoot.getRoot().child("Users").child(mAuth.getCurrentUser().getUid()).child("Tasks").child(taskName).child("Status").setValue(b);
            }
        };

        private void SetUpTasksListener()
        {
            if(getArguments().getInt(ARG_SECTION_NUMBER) - 1 == Defines.PROJECT_TASKS_FRAGMENT) {
                tasksCEV = mRoot.child("Tasks").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        final String taskName = dataSnapshot.getKey();
                        mRoot.getRoot().child("Users").child(mAuth.getCurrentUser().getUid()).child("Tasks")
                                .child(taskName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                boolean taskStatus = (boolean) dataSnapshot.child("Status").getValue();
                                final LinearLayout ll = ViewFactory.linearLayoutFactory(getActivity(), taskName, taskStatus);

                                ll.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String name = ((TextView) ((LinearLayout) view).getChildAt(Defines.TEXT_VIEW_POSITION)).getText().toString();
                                        ((ProjectsView) getActivity()).AddOrEditTask(name);
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


                                ((CheckBox) ll.getChildAt(ViewFactory.LINEAR_LAYOUT_CHECKBOX_POSITION)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                        mRoot.getRoot().child("Users").child(mAuth.getCurrentUser().getUid()).child("Tasks")
                                                .child((String) ((CheckBox) ll.getChildAt(1)).getTag()).child("Status").setValue(b);
                                        ((ProjectsActivity) getActivity()).RefreshCurrentFragment();
                                    }
                                });

                                mLinearLayout.addView(ll);
                                mLinearLayout.addView(ViewFactory.horizontalDividerFactory(getActivity()));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        RemoveItem(dataSnapshot.getKey());
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            else
            {
                peopleCEV = mRoot.child("People").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        final String personName = dataSnapshot.getKey();

                        LinearLayout ll = ViewFactory.linearLayoutFactory(getActivity(), personName, Defines.LinearLayoutType.PERSON);

                        ll.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getActivity(), ((Defines.LinearLayoutType)view.getTag() == Defines.LinearLayoutType.PERSON ? ProfileView.class : ProjectsView.class ));
                                String extraInfo = ((TextView)((LinearLayout)view).getChildAt(Defines.TEXT_VIEW_POSITION)).getText().toString();
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

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        RemoveItem(dataSnapshot.getKey());
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
            if(peopleCEV != null)
                mRoot.child("People").removeEventListener(peopleCEV);
            if(tasksCEV != null)
                mRoot.child("Tasks").removeEventListener(tasksCEV);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final static int FRAGMENTS_NUM = 2;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //TODO: change it to produce another fragment if position == 0
            return ActivityFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return FRAGMENTS_NUM;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Project";
                case 1:
                    return "Tasks";
            }
            return null;
        }
    }
}
