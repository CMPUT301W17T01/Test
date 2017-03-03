package com.example.mac.bugfree;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;

    // Test CardView
    private ArrayList<MoodEvent> moodEventArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Intent intent = new Intent(MainActivity.this, CreateEditMoodActivity.class);
        //startActivity(intent);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        //navigationView.setCheckedItem(R.id.drawer_filter);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.drawer_filter:
                        mDrawerLayout.closeDrawers();
                        break;

                    case R.id.drawer_friend:
                        mDrawerLayout.closeDrawers();
                        break;

                    case R.id.drawer_sign_out:
                        mDrawerLayout.closeDrawers();
                        break;
                }
                return true;
            }
        });

        // Test CardView
        try{
            TestCardView(moodEventArrayList);
        }catch(MoodStateNotAvailableException e){
            Toast.makeText(getApplicationContext(),
                    "Invalid mood state tested.",
                    Toast.LENGTH_SHORT).show();
        };


        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        RecyclerView.Adapter mAdapter = new MoodEventAdapter(moodEventArrayList);
        mRecyclerView.setAdapter(mAdapter);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_mainactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;

            case R.id.add_follow:
                Toast.makeText(this, "You clicked add_follow", Toast.LENGTH_SHORT).show();
                break;

            default:
        }
        return true;
    }

    public void TestCardView(ArrayList<MoodEvent> List) throws MoodStateNotAvailableException{
        MoodEvent event1 = new MoodEvent("Happy",2);
        MoodEvent event2 = new MoodEvent("Happy",2);
        List.add(event1);
        List.add(event2);

    }

    public void loadList(ArrayList<MoodEvent> moodEventArrayList) {
        // load List to UI
    }

    public void followDialogue(String username) {
        //
    }

    public void onitemDialogue() {
        //
    }


}
