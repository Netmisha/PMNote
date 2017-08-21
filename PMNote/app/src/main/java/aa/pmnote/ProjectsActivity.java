package aa.pmnote;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ProjectsActivity extends AppCompatActivity {

    private final static int PROJECTS = 0;
    private final static int TASKS = 1;

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

        //who cares about depreciation anyway
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                switch (position)
                {
                    case 0:
                        setTitle("Projects and People");
                        break;
                    case 1:
                        setTitle("Tasks");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        setTitle("Projects and People");

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddNewItem();
            }
        });
    }

    private void AddNewItem()
    {
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
        if(mViewPager.getCurrentItem() == PROJECTS) {
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
        }

        builder.setView(ll);

        //set on 'ok' listener
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                DatabaseReference child = mRootRef;
                switch (mViewPager.getCurrentItem())
                {
                    case PROJECTS:
                        int i = spinner.getSelectedItemPosition();
                        switch (spinner.getSelectedItemPosition())
                        {
                            case 0:
                                child.child("people").child(name).child("ph").setValue(true);
                                break;
                            case 1:
                                child.child("projects").child(name).child("ph").setValue(true);
                        }
                        break;
                    case TASKS:
                        child.child("tasks").child(name).child("ph").setValue(true);
                        break;
                }
            }
        });

        //set on 'cancer' listener
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

                        HideAllNonMatches(null);
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
        LinearLayout ll = (LinearLayout)findViewById(R.id.myLinearLayout);
        for(int i = 0; i < ll.getChildCount(); i+=2)
        {
            String llText = ((TextView)((LinearLayout)ll.getChildAt(i)).getChildAt(1)).getText().toString();
            if(text == null || llText.equals(text))
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
    private final static int PROJECT = 0;
    private final static int PERSON = 1;
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case 1:
                last_context_selected.callOnClick();
                break;
            case 2:
                String key = ((TextView)((LinearLayout)last_context_selected).getChildAt(1)).getText().toString();
                mRootRef.child(((int)last_context_selected.getTag() == PROJECT ? "projects" : "people")).child(key).removeValue();
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
            menu.setHeaderTitle("Whadda you do?");
            menu.add(Menu.NONE, 1, Menu.NONE, "Open");
            menu.add(Menu.NONE, 2, Menu.NONE, "Delete");
        }
    }

    public static class ActivityFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final int PROJECTS = 1;
        private static final int TASKS = 2;

        private LinearLayout mLinearLayout;

        private FirebaseAuth mAuth;
        private FirebaseAuth.AuthStateListener mAuthStateListener;
        private DatabaseReference mRootRef;

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
                        switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                            case PROJECTS:
                                SetUpProjectsPersonList(mRootRef);
                                break;
                            case TASKS:
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
            mAuth.addAuthStateListener(mAuthStateListener);
        }

        @Override
        public void onStop() {
            super.onStop();
            if (mAuthStateListener != null) {
                mAuth.removeAuthStateListener(mAuthStateListener);
            }
        }

        private enum LinearLayoutType
        {
            PROJECT,
            PERSON
        }

        private final static String INFO_TAG = "PERSON_NAME";

        private LinearLayout linearLayoutFactory(String text, LinearLayoutType llt)
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
                    Intent intent = new Intent(getActivity(), ((int)view.getTag() == PERSON ? ProfileView.class :ProfileView.class ));
                    String extraInfo = ((TextView)((LinearLayout)view).getChildAt(1)).getText().toString();
                    intent.putExtra(INFO_TAG, extraInfo);
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

        private ImageView imageViewFactory(LinearLayoutType llt)
        {
            ImageView iv = new ImageView(getActivity());
            iv.setLayoutParams(new ViewGroup.LayoutParams(150, 150));
            iv.setImageResource((llt == LinearLayoutType.PROJECT ? R.drawable.ic_project : R.drawable.ic_person));
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

        private final static int PROJECT = 0;
        private final static int PERSON = 1;

        private void SetUpProjectsPersonList(DatabaseReference dr)
        {
            dr.child("projects").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    AddPersonOrProject(dataSnapshot.getKey(), LinearLayoutType.PROJECT);
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
                    AddPersonOrProject(dataSnapshot.getKey(), LinearLayoutType.PERSON);
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
                    //TODO: on task add
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    //TODO on task remove
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        private void AddPersonOrProject(String name, LinearLayoutType llt)
        {
            LinearLayout ll = linearLayoutFactory(name, llt);
            ll.setTag((llt == LinearLayoutType.PERSON ? PERSON : PROJECT));
            mLinearLayout.addView(ll);
            mLinearLayout.addView(horizontalDividerFactory());
        }

        private void RemovePersonOrProject(String name)
        {
            int position = FindLinearLayoutByText(name);
            mLinearLayout.removeView(mLinearLayout.getChildAt(position));
            mLinearLayout.removeView(mLinearLayout.getChildAt(position));
        }

        private int FindLinearLayoutByText(String text)
        {
            int i;
            int childCount = mLinearLayout.getChildCount();
            for(i = 0; i < childCount; i+=2)
            {
                String llText =((TextView)((LinearLayout)mLinearLayout.getChildAt(i)).getChildAt(1)).getText().toString();
                if(llText == text) {
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
        public int getCount() {
            // Show 2 total pages.
            //projects and tasks
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Projects";
                case 1:
                    return "Tasks";
            }
            return null;
        }
    }

    public class DetailOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

        private int currentPage;

        @Override
        public void onPageSelected(int position) {
            currentPage = position;
        }

        public final int getCurrentPage() {
            return currentPage;
        }
    }
}
