package in.sportscult.sportscultback;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Add_Live_Matches extends AppCompatActivity {

    private static DatabaseReference databaseReference;
    private static final DatabaseReference rootReference= FirebaseDatabase.getInstance().getReference();
    private static final DatabaseReference LiveMatchReference= FirebaseDatabase.getInstance().getReference().child("Live Matches");
    private Spinner age_group_fixture;
    private RecyclerView upcoming_matches_fixture;
    private TextView Page_Title;
    private static int selection_for_age_group = 1;
    private static final String[] age_group_codes = {"0","A","B","C","D"};
    private static String age_group;
    private static ArrayList<Fixture> list_of_fixtures;
    private static FixtureListAdapter fixtureListAdapter;
    private static ProgressDialog progressDialog;
    static Map<String,String> team_profile_pic_download_urls;
    private static Button specific_for_live_match;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixture__editing__page);

        age_group_fixture = (Spinner) findViewById(R.id.age_group_spinner_fixture_editor);
        upcoming_matches_fixture = (RecyclerView) findViewById(R.id.recycler_view_for_fixture_editor);
        list_of_fixtures = new ArrayList<Fixture>();
        team_profile_pic_download_urls = new HashMap<String, String>();
        Page_Title = (TextView)findViewById(R.id.Page_Title);
        Page_Title.setText("Configure Scheduled Matches");
        specific_for_live_match = (Button)findViewById(R.id.specific_for_live_match);
        specific_for_live_match.setVisibility(View.VISIBLE);

        final ArrayAdapter<String> age_group_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.age_groups));
        age_group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age_group_fixture.setAdapter(age_group_adapter);

        age_group_fixture.setSelection(selection_for_age_group);
        age_group = "Group - "+age_group_codes[selection_for_age_group];

        fixtureListAdapter = new FixtureListAdapter(Add_Live_Matches.this,list_of_fixtures,team_profile_pic_download_urls);
        upcoming_matches_fixture.setAdapter(fixtureListAdapter);
        upcoming_matches_fixture.setLayoutManager(new LinearLayoutManager(this));

        upcoming_matches_fixture.addOnItemTouchListener(new RecyclerItemClickListener(this, upcoming_matches_fixture, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(Add_Live_Matches.this,"Long Press To Configure Match",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Add_Live_Matches.this);
                alertDialog.setMessage("Choose the appropriate match status.\nThe change will be permanent.Make sure before continuing.");
                alertDialog.setNeutralButton("Match Postponed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String clicked_ID = get_Push_Id(position);
                        //Delete from Fixtures
                        list_of_fixtures.remove(position);
                        fixtureListAdapter.notifyDataSetChanged();
                        rootReference.child(age_group).child("Fixtures").child(clicked_ID).removeValue();
                        dialog.cancel();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setPositiveButton("Match Is Live", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final DialogInterface dialog1 = dialog;
                        //Get the data of the match to be made Live
                        Fixture live_match = list_of_fixtures.get(position);
//                        Log.d("CHECK",live_match.TeamA);
//                        Log.d("CHECK",live_match.TeamB);
//                        Log.d("CHECK",live_match.Venue);
//                        Log.d("CHECK",live_match.Time);
//                        Log.d("CHECK",age_group);
//                        Log.d("CHECK",position+"");

                        String clicked_ID = get_Push_Id(position);
                        //Delete from Fixtures
                        rootReference.child(age_group).child("Fixtures").child(clicked_ID).removeValue();

                        //Add The Match To The Live Match Page
                        Map<String,String> uploadData = new HashMap<String, String>();
                        uploadData.put("Team A",live_match.TeamA);
                        uploadData.put("Team B",live_match.TeamB);
                        uploadData.put("Team A Goals","0");
                        uploadData.put("Team B Goals","0");
                        uploadData.put("Venue", live_match.Venue);
                        uploadData.put("Start Time",live_match.Time);
                        uploadData.put("Age Group",age_group);
//                        LiveMatchReference.child(clicked_ID).child("Team A").setValue(live_match.TeamA);
//                        LiveMatchReference.child(clicked_ID).child("Team B").setValue(live_match.TeamB);
//                        LiveMatchReference.child(clicked_ID).child("Team A Goals").setValue("0");
//                        LiveMatchReference.child(clicked_ID).child("Team B Goals").setValue("0");
//                        LiveMatchReference.child(clicked_ID).child("Venue").setValue(live_match.Venue);
//                        LiveMatchReference.child(clicked_ID).child("Start Time").setValue(live_match.Time);
//                        LiveMatchReference.child(clicked_ID).child("Age Group").setValue(age_group);
                        LiveMatchReference.child(clicked_ID).setValue(uploadData);

                        //Intent intent = new Intent(Add_Live_Matches.this,LiveMatches.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_NEW_TASK);
                        //startActivity(intent);
                    }
                });
                alertDialog.show();
            }
        }));

        Fetching_Fixtures_From_Firebase();

        //Listening for change in age groups
        age_group_fixture.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0 && position!=selection_for_age_group){
                    selection_for_age_group = position;
                    age_group = "Group - "+age_group_codes[position];
                    Fetching_Fixtures_From_Firebase();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public String get_Push_Id(int position){
        String clicked_ID;
        Fixture temp = list_of_fixtures.get(position);
        String date = temp.Date;
        //Log.d("CHECK",date);
        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
        Date date1 = null;
        try {
            date1 = parseFormat.parse(temp.Time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String Raw_Time = displayFormat.format(date1)+":00";

        //Log.d("CHECK",Raw_Time);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String dateinString = date+ " " + Raw_Time;
        Date d = null;
        try {
            d = sdf.parse(dateinString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.setTime(d);
        clicked_ID = (c.getTimeInMillis())+temp.TeamA+temp.TeamB;
        return clicked_ID;
    }

    public void go_to_live_match_page(View view){
        Intent intent = new Intent(Add_Live_Matches.this,LiveMatches.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void Fetching_Fixtures_From_Firebase(){

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Data....");
        progressDialog.setCancelable(false);
        //progressDialog.show();

        databaseReference = FirebaseDatabase.getInstance().getReference().child(age_group);
        databaseReference.child("Fixtures").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                list_of_fixtures = new ArrayList<Fixture>();
                if(dataSnapshot.getValue()==null){
                    progressDialog.dismiss();
                    upcoming_matches_fixture.setAdapter(new FixtureListAdapter(Add_Live_Matches.this,list_of_fixtures,team_profile_pic_download_urls));
                    return;
                }
                for(DataSnapshot ChildSnapshot : dataSnapshot.getChildren()){
                    Map<String,String> fixture_description = (Map<String,String>)ChildSnapshot.getValue();
                    Fixture fixture = new Fixture(fixture_description.get("Team A"),fixture_description.get("Team B"),fixture_description.get("Date"),
                            fixture_description.get("Time"),fixture_description.get("Venue"),fixture_description.get("Referee"));
                    list_of_fixtures.add(fixture);
                }
                databaseReference.child("Team Names").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        team_profile_pic_download_urls = new HashMap<String, String>();
                        for(DataSnapshot ChildSnapshot : dataSnapshot.getChildren()) {
                            Map<String, String> urlmap = (Map<String, String>) ChildSnapshot.getValue();
                            team_profile_pic_download_urls.put(ChildSnapshot.getKey(), urlmap.get("Team Profile Pic Thumbnail Url"));
                        }
                        progressDialog.dismiss();
                        //Configure Adapter for ListView
                        upcoming_matches_fixture.setAdapter(new FixtureListAdapter(Add_Live_Matches.this,list_of_fixtures,team_profile_pic_download_urls));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(Add_Live_Matches.this,"Some Error Occurred",Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(Add_Live_Matches.this,"Some Error Occurred",Toast.LENGTH_LONG).show();
            }
        });

    }
}
