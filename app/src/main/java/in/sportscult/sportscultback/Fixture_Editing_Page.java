package in.sportscult.sportscultback;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Fixture_Editing_Page extends AppCompatActivity {

    private static DatabaseReference databaseReference;
    private static DatabaseReference reference_for_deletion= FirebaseDatabase.getInstance().getReference();
    private Spinner age_group_fixture;
    private RecyclerView upcoming_matches_fixture;
    private static int selection_for_age_group = 1;
    private static final String[] age_group_codes = {"0","A","B","C","D"};
    private static String age_group;
    private static ArrayList<Fixture> list_of_fixtures;
    private static FixtureListAdapter fixtureListAdapter;
    private static ProgressDialog progressDialog;
    static Map<String,String> team_profile_pic_download_urls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixture__editing__page);

        age_group_fixture = (Spinner) findViewById(R.id.age_group_spinner_fixture_editor);
        upcoming_matches_fixture = (RecyclerView) findViewById(R.id.recycler_view_for_fixture_editor);
        list_of_fixtures = new ArrayList<Fixture>();
        team_profile_pic_download_urls = new HashMap<String, String>();

        final ArrayAdapter<String> age_group_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.age_groups));
        age_group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age_group_fixture.setAdapter(age_group_adapter);

        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            selection_for_age_group = Integer.parseInt(extras.getString("Age Group Selection"));
        }

        age_group_fixture.setSelection(selection_for_age_group);
        age_group = "Group - "+age_group_codes[selection_for_age_group];

        fixtureListAdapter = new FixtureListAdapter(Fixture_Editing_Page.this,list_of_fixtures,team_profile_pic_download_urls);
        upcoming_matches_fixture.setAdapter(fixtureListAdapter);
        upcoming_matches_fixture.setLayoutManager(new LinearLayoutManager(this));

        upcoming_matches_fixture.addOnItemTouchListener(new RecyclerItemClickListener(this, upcoming_matches_fixture, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(Fixture_Editing_Page.this,"To delete a match, press and hold the item",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Fixture_Editing_Page.this);
                alertDialog.setMessage("Proceeding with this action will lead to removal of the match from the fixtures page.\nThe change will be permanent.Are you sure you want to continue?");
                alertDialog.setPositiveButton("Confirm Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String clicked_ID;
                        Fixture temp = list_of_fixtures.get(position);
                        String date = temp.Date;
                        Log.d("CHECK",date);
                        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
                        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
                        Date date1 = null;
                        try {
                            date1 = parseFormat.parse(temp.Time);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        String Raw_Time = displayFormat.format(date1)+":00";

                        Log.d("CHECK",Raw_Time);
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
                        list_of_fixtures.remove(position);
                        fixtureListAdapter.notifyDataSetChanged();
                        reference_for_deletion.child(age_group).child("Fixtures").child(clicked_ID).removeValue();
                        dialog.cancel();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
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
                    //Also add selection_for_age_group to Shared Preferences
                    Fetching_Fixtures_From_Firebase();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void Fetching_Fixtures_From_Firebase(){

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Data....");
        progressDialog.setCancelable(false);

        databaseReference = FirebaseDatabase.getInstance().getReference().child(age_group);
        databaseReference.child("Fixtures").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //progressDialog.show();
                list_of_fixtures = new ArrayList<Fixture>();
                if(dataSnapshot.getValue()==null){
                    progressDialog.dismiss();
                    upcoming_matches_fixture.setAdapter(new FixtureListAdapter(Fixture_Editing_Page.this,list_of_fixtures,team_profile_pic_download_urls));
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
                        upcoming_matches_fixture.setAdapter(new FixtureListAdapter(Fixture_Editing_Page.this,list_of_fixtures,team_profile_pic_download_urls));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(Fixture_Editing_Page.this,"Some Error Occurred",Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(Fixture_Editing_Page.this,"Some Error Occurred",Toast.LENGTH_LONG).show();
            }
        });

    }
}

class Fixture{
    String TeamA,TeamB,Time,Venue,Referee,Date;
    Fixture(String TeamA,String TeamB,String Date,String Time,String Venue,String Referee){
        this.TeamA = TeamA;
        this.TeamB = TeamB;
        this.Time = Time;
        this.Venue = Venue;
        this.Referee = Referee;
        this.Date = Date;
    }
}

class FixtureListAdapter extends RecyclerView.Adapter<FixtureListAdapter.ViewHolder1>{
    ArrayList<Fixture> fixtureArrayList;
    Map<String,String> map_for_team_profile_pic_download_urls;
    LayoutInflater layoutInflater;
    Context context;
    public FixtureListAdapter(Context context,ArrayList<Fixture> fixtureArrayList,Map<String,String> map_for_team_profile_pic_download_urls){
        this.context = context;
        this.fixtureArrayList = fixtureArrayList;
        this.map_for_team_profile_pic_download_urls = map_for_team_profile_pic_download_urls;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder1 onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.fixture_card,parent,false);
        ViewHolder1 viewHolder1 = new ViewHolder1(view);
        return viewHolder1;
    }

    @Override
    public void onBindViewHolder(ViewHolder1 viewHolder, int position) {
        viewHolder.teamA_name.setText(fixtureArrayList.get(position).TeamA);
        viewHolder.teamB_name.setText(fixtureArrayList.get(position).TeamB);
        viewHolder.date.setText(fixtureArrayList.get(position).Date);
        viewHolder.time.setText(fixtureArrayList.get(position).Time);
        viewHolder.venue.setText(fixtureArrayList.get(position).Venue);
        viewHolder.referee.setText(fixtureArrayList.get(position).Referee);

        final ImageView tempImageViewA = viewHolder.teamA_image;
        final ImageView tempImageViewB = viewHolder.teamB_image;
        final String urlA = map_for_team_profile_pic_download_urls.get(fixtureArrayList.get(position).TeamA);
        final String urlB = map_for_team_profile_pic_download_urls.get(fixtureArrayList.get(position).TeamB);
        //Load profile pic thumbnails
        Picasso.with(context)
                .load(urlA)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(tempImageViewA, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(context)
                                .load(urlA)
                                //.error(R.drawable.common_full_open_on_phone)
                                .into(tempImageViewA, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                    }
                                });
                    }
                });
        Picasso.with(context)
                .load(urlB)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(tempImageViewB, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(context)
                                .load(urlB)
                                //.error(R.drawable.common_full_open_on_phone)
                                .into(tempImageViewB, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                    }
                                });
                    }
                });
    }

    @Override
    public int getItemCount() {
        return fixtureArrayList.size();
    }

    class ViewHolder1 extends RecyclerView.ViewHolder{
        TextView teamA_name, teamB_name, date, time, venue, referee;
        ImageView teamA_image,teamB_image;

        ViewHolder1(View v) {
            super(v);
            teamA_name = (TextView) v.findViewById(R.id.teamA_name);
            teamB_name = (TextView) v.findViewById(R.id.teamB_name);
            date = (TextView) v.findViewById(R.id.date);
            time = (TextView) v.findViewById(R.id.time);
            venue = (TextView) v.findViewById(R.id.venue);
            referee = (TextView) v.findViewById(R.id.referee);
            teamA_image = (ImageView)v.findViewById(R.id.teamA_image);
            teamB_image = (ImageView)v.findViewById(R.id.teamB_image);

        }
    }


}
