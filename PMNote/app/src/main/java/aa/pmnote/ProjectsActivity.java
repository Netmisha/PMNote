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
import java.util.Locale;

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

    public void RefreshCurrentFragment()
    {
        HideItemsBySearchOptions(mSearchOptions.getSelectedItemPosition());
    }

    private void SetArrayList(ArrayList<String> list, Defines.LinearLayoutType llt)
    {
        list.clear();
        if(llt == Defines.LinearLayoutType.TASK)
        {
            list.add("Show All");
            list.add("Show Open");
            list.add("Show Completed");
        }
        else
        {
            list.add("Show All");
            list.add("Show Projects");
            list.add("Show People");
        }
    }

    private void HideItemsBySearchOptions(int search_option)
    {
        LinearLayout ll = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem()).GetLinearLayout();
        if(mViewPager.getCurrentItem() == 0) {
            switch (search_option) {
                case 0:
                    for (int i = 0; i < ll.getChildCount(); ++i) {
                        ll.getChildAt(i).setVisibility(View.VISIBLE);
                    }
                    break;
                case 1:
                    for (int i = 0; i < ll.getChildCount(); i += 2) {
                        boolean isProject = Defines.LinearLayoutType.PROJECT ==((ImageView)((LinearLayout) ll.getChildAt(i)).getChildAt(0)).getTag();
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
                        boolean isProject = Defines.LinearLayoutType.PERSON ==((ImageView)((LinearLayout) ll.getChildAt(i)).getChildAt(0)).getTag();
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
        }
        else {
            switch (search_option) {
                case 0:
                    for (int i = 0; i < ll.getChildCount(); ++i) {
                        ll.getChildAt(i).setVisibility(View.VISIBLE);
                    }
                    break;
                case 1:
                    for (int i = 0; i < ll.getChildCount(); i += 2) {
                        boolean isCompeted = ((CheckBox) ((LinearLayout) ll.getChildAt(i)).getChildAt(0)).isChecked();
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
                        boolean isCompeted = ((CheckBox) ((LinearLayout) ll.getChildAt(i)).getChildAt(0)).isChecked();
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

        SetArrayList(mCurrentSpinnerList, Defines.LinearLayoutType.PROJECT);
        mSearchOptions = (Spinner)findViewById(R.id.searchOptions);
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
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                setTitle(mSectionsPagerAdapter.getPageTitle(position));
                SetArrayList(mCurrentSpinnerList,
                        (position == Defines.PROJECTS_FRAGMENT ? Defines.LinearLayoutType.PROJECT : Defines.LinearLayoutType.TASK));
                mArrayAdapter.notifyDataSetChanged();

                int temp = mSearchOptions.getSelectedItemPosition();
                mSearchOptions.setSelection(mSavedSpinnerPosition);
                mSavedSpinnerPosition = temp;
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

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

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(mViewPager.getCurrentItem())
                {
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

    public void AddOrEditTask()
    {
        AddOrEditTask(null, null, null, null, false);
    }

    public void AddOrEditTask(final String name)
    {
        mRootRef.child("tasks").child(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String date = null, time = null, descr = null;
                boolean status = (boolean)dataSnapshot.child("status").getValue();
                if(dataSnapshot.child("date").exists()) {
                    date = dataSnapshot.child("date").getValue(String.class);
                }
                if(dataSnapshot.child("time").exists()) {
                    time = dataSnapshot.child("time").getValue(String.class);
                }
                if(dataSnapshot.child("description").exists()) {
                    descr = dataSnapshot.child("description").getValue(String.class);
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
        if(date == null) {
            timeInput.setVisibility(View.GONE);
        }
        timeInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
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
                if(b) {
                    Calendar c = Calendar.getInstance();
                    DatePickerDialog dpd = new DatePickerDialog(ProjectsActivity.this, new DatePickerDialog.OnDateSetListener() {
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

        final EditText descriptionInput = new EditText(ProjectsActivity.this);
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
                    DatabaseReference child = mRootRef.child("tasks");
                    if(name != null && !enteredName.equals(name))
                    {
                        child.child(name).removeValue();
                    }
                    child = child.child(enteredName);
                    child.child("status").setValue(status);
                    child.child("date").setValue(dateInput.getText().toString());
                    child.child("description").setValue(descriptionInput.getText().toString());
                    child.child("time").setValue(timeInput.getText().toString());
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

        spinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120));
        final ArrayList<String> list = new ArrayList<>();
        list.add("Person");
        list.add("Project");
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, list);
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
                        child.child("people").child(name).child("None").setValue(true);
                        break;
                    case 1:
                        child.child("projects").child(name).child("status").setValue(true);
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
        ((LinearLayout)findViewById(R.id.myLinearLayout)).removeAllViews();
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
                        searchView.setQuery("",false);
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

    private void HideAllNonMatches(String text)
    {
        LinearLayout ll = mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem()).GetLinearLayout();
        for(int i = 0; i < ll.getChildCount(); i+=2)
        {
            String llText = ((TextView)((LinearLayout)ll.getChildAt(i)).getChildAt(Defines.TEXT_VIEW_POSITION)).getText().toString();
            if(text == null || llText.contains(text))
            {
                ll.getChildAt(i).setVisibility(View.VISIBLE);
                ll.getChildAt(i+1).setVisibility(View.VISIBLE);
            }
            else
            {
                ll.getChildAt(i).setVisibility(View.GONE);
                ll.getChildAt(i+1).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case 1:
                last_context_selected.callOnClick();
                break;
            case 2:
                String key = ((TextView)((LinearLayout)last_context_selected).getChildAt(Defines.TEXT_VIEW_POSITION)).getText().toString();
                DatabaseReference child = null;
                switch ((Defines.LinearLayoutType)last_context_selected.getTag())
                {
                    case PERSON:
                        child = mRootRef.child("people");
                        break;
                    case PROJECT:
                        child = mRootRef.child("projects");
                        break;
                    case TASK:
                        child = mRootRef.child("tasks");
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
            menu.add(Menu.NONE, 2, Menu.NONE, "Delete");
        }
    }

    public static class ActivityFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        private LinearLayout mLinearLayout;

        private FirebaseAuth mAuth;
        private FirebaseAuth.AuthStateListener mAuthStateListener;
        private DatabaseReference mRootRef;

        public LinearLayout GetLinearLayout()
        {
            return mLinearLayout;
        }

        public ActivityFragment() {}

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
            mLinearLayout = (LinearLayout)rootView.findViewById(R.id.myLinearLayout);

            mAuth = FirebaseAuth.getInstance();
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if(firebaseAuth.getCurrentUser() != null) {
                        String uid = firebaseAuth.getCurrentUser().getUid();
                        mRootRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                        switch (getArguments().getInt(ARG_SECTION_NUMBER) - 1) {
                            case Defines.PROJECTS_FRAGMENT:
                                SetUpProjectsPersonList(mRootRef);
                                break;
                            case Defines.TASKS_FRAGMENT:
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
        }



        private LinearLayout linearLayoutFactory(String text, Defines.LinearLayoutType llt)
        {
            LinearLayout ll = new LinearLayout(getActivity());
            ll.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            ll.setGravity(Gravity.CENTER);
            ll.setLayoutParams(lp);

            ll.addView(imageViewFactory(llt));
            ll.addView(textViewFactory(text));

            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO: set switch here
                    Intent intent = new Intent(getActivity(), ((Defines.LinearLayoutType)view.getTag() == Defines.LinearLayoutType.PERSON ? ProfileView.class : ProfileView.class ));
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

            return ll;
        }

        private LinearLayout linearLayoutFactory(String text, boolean checkBoxStatus)
        {
            LinearLayout ll = new LinearLayout(getActivity());
            ll.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            ll.setGravity(Gravity.CENTER);
            ll.setLayoutParams(lp);

            ll.addView(checkBoxFactory(text, checkBoxStatus));
            ll.addView(textViewFactory(text));

            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name = ((TextView)((LinearLayout)view).getChildAt(Defines.TEXT_VIEW_POSITION)).getText().toString();
                    ((ProjectsActivity)getActivity()).AddOrEditTask(name);
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

            return ll;
        }

        private CheckBox checkBoxFactory(String text, boolean checked)
        {
            final CheckBox cb = new CheckBox(getActivity());
            cb.setLayoutParams(new ViewGroup.LayoutParams(150, 150));
            cb.setText("");
            cb.setTag(text);
            cb.setChecked(checked);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mRootRef.child("tasks").child((String)cb.getTag()).child("status").setValue(b);
                    ((ProjectsActivity)getActivity()).RefreshCurrentFragment();
                }
            });

            return cb;
        }

        private ImageView imageViewFactory(Defines.LinearLayoutType llt)
        {
            ImageView iv = new ImageView(getActivity());
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

        private TextView textViewFactory(String text)
        {
            TextView tv = new TextView(getActivity());
            tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tv.setGravity(Gravity.RIGHT);
            tv.setTextSize(20);
            tv.setText(text);

            return tv;
        }

        private View horizontalDividerFactory()
        {
            View hd = new View(getActivity());
            hd.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            hd.setBackgroundColor(Color.GRAY);
            return hd;
        }

        private void SetUpProjectsPersonList(DatabaseReference dr)
        {
            dr.child("projects").addChildEventListener(new ChildEventListener() {
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

            dr.child("people").addChildEventListener(new ChildEventListener() {
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

        private void SetUpTaskList(DatabaseReference dr)
        {
            dr.child("tasks").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String name = dataSnapshot.getKey();
                    boolean status = (boolean)dataSnapshot.child("status").getValue();
                    AddItem(name, status);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    ChangeCheckBoxStatus(dataSnapshot.getKey(), (boolean)dataSnapshot.child("status").getValue());
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

        void ChangeCheckBoxStatus(String name, boolean newStatus)
        {
            int i = FindLinearLayoutByText(name);
            ((CheckBox)((LinearLayout)mLinearLayout.getChildAt(i)).getChildAt(0)).setChecked(newStatus);
        }

        private void AddItem(String name, boolean checkBoxStatus)
        {
            LinearLayout ll = linearLayoutFactory(name, checkBoxStatus);
            mLinearLayout.addView(ll);
            mLinearLayout.addView(horizontalDividerFactory());
        }

        private void AddItem(String name, Defines.LinearLayoutType llt)
        {
            LinearLayout ll = linearLayoutFactory(name, llt);
            mLinearLayout.addView(ll);
            mLinearLayout.addView(horizontalDividerFactory());
        }

        private void RemovePersonOrProject(String name)
        {
            int position = FindLinearLayoutByText(name);
            mLinearLayout.removeViews(position, Defines.ITEM_SIZE_IN_VIEWS);
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
            ActivityFragment fragment = (ActivityFragment)super.instantiateItem(container, position);
            mFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public ActivityFragment getFragment(int position)
        {
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
