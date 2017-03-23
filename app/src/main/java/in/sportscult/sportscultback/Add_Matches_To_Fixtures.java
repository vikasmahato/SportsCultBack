package in.sportscult.sportscultback;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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

public class Add_Matches_To_Fixtures extends AppCompatActivity {

    private EditText TeamA_Name_Fixture,TeamB_Name_Fixture,Venue_Fixture,Referee_Fixture;
    private TextView Date_Fixture,Time_Fixture;
    private Spinner age_group_spinner_fixture;
    private RecyclerView recycler_view_fixtures;
    private static final String[] age_group_texts = {"Group - 0","Group - A","Group -B","Group - C","Group - D"};
    private static Fixture_Description input_fixture_description;
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private String raw_time;
    static boolean verification_status;
    private static int selection_for_age_group;
    private static String age_group;
    private static ArrayList<Fixture> list_of_fixtures;
    private static FixtureListAdapter fixtureListAdapter;
    static Map<String,String> team_profile_pic_download_urls;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add__matches__to__fixtures);

        TeamA_Name_Fixture = (EditText) findViewById(R.id.TeamA_Name_Fixture);
        TeamB_Name_Fixture = (EditText) findViewById(R.id.TeamB_Name_Fixture);
        Date_Fixture = (TextView) findViewById(R.id.Date_Fixture);
        Time_Fixture = (TextView) findViewById(R.id.Time_Fixture);
        Venue_Fixture = (EditText) findViewById(R.id.Venue_Fixture);
        Referee_Fixture = (EditText) findViewById(R.id.Referee_Fixture);
        age_group_spinner_fixture = (Spinner)findViewById(R.id.age_group_spinner_fixture);
        recycler_view_fixtures = (RecyclerView)findViewById(R.id.recycler_view_fixtures);

        list_of_fixtures = new ArrayList<Fixture>();
        team_profile_pic_download_urls = new HashMap<String, String>();

        final ArrayAdapter<String> age_group_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.age_groups));
        age_group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age_group_spinner_fixture.setAdapter(age_group_adapter);

        age_group_spinner_fixture.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0 && position!=selection_for_age_group){
                    selection_for_age_group = position;
                    age_group = age_group_texts[position];
                    //Also add selection_for_age_group to Shared Preferences
                    Fetching_Fixtures_From_Firebase();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    public void pick_a_date(View view){
        int mYear,mMonth,mDay;
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        Date_Fixture.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }


    public void pick_a_time(View view){
        int mHour,mMinute;
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        raw_time = hourOfDay + ":" + minute+":00";
                        String format;
                        if (hourOfDay == 0) {
                            hourOfDay += 12;
                            format = "AM";
                        } else if (hourOfDay == 12) {
                            format = "PM";
                        } else if (hourOfDay > 12) {
                            hourOfDay -= 12;
                            format = "PM";
                        } else {
                            format = "AM";
                        }
                        Time_Fixture.setText(new StringBuilder().append(hourOfDay).append(" : ").append(minute)
                                .append(" ").append(format));
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    public void add_match_to_fixture(View view) throws ParseException {

        boolean verification = get_confirmation();
        if(!verification)
            return;

        verification = get_verified_data();
        if(!verification)
            return;

        //Verified Data Is Now Available

        //Generate a unique key for each match which will be sorted according to time
        String id_to_push = pushId(input_fixture_description.Date,input_fixture_description.Raw_Time)+input_fixture_description.TeamA+input_fixture_description.TeamB;
        DatabaseReference databaseReference1 = databaseReference.child(input_fixture_description.AgeGroup).child("Fixtures").child(id_to_push);
        databaseReference1.child("Date").setValue(input_fixture_description.Date);
        databaseReference1.child("Referee").setValue(input_fixture_description.Referee);
        databaseReference1.child("Team A").setValue(input_fixture_description.TeamA);
        databaseReference1.child("Team B").setValue(input_fixture_description.TeamB);
        databaseReference1.child("Time").setValue(input_fixture_description.Time);
        databaseReference1.child("Venue").setValue(input_fixture_description.Venue);

    }

    public boolean get_verified_data(){

        String TeamA,TeamB,Date,Time,Venue,Referee,AgeGroup;

        //Do the appropriate capitalization
        TeamA = properly_format_input(TeamA_Name_Fixture.getText().toString());
        TeamB = properly_format_input(TeamB_Name_Fixture.getText().toString());
        Date = Date_Fixture.getText().toString();
        Time = Time_Fixture.getText().toString();
        Venue = properly_format_input(Venue_Fixture.getText().toString());
        Referee = properly_format_input(Referee_Fixture.getText().toString());
        int index = age_group_spinner_fixture.getSelectedItemPosition();
        AgeGroup = age_group_texts[index];
        verification_status = true;

        //Verify The Data
        View focusView = null;
        if(Referee.length()<3){
            focusView = Referee_Fixture;
            Referee_Fixture.setError(getString(R.string.input_too_short));
            verification_status = false;
        }
        if(Venue.length()<6){
            focusView = Venue_Fixture;
            Venue_Fixture.setError(getString(R.string.input_too_short));
            verification_status = false;
        }
        if(TeamB.length()<2){
            focusView = TeamB_Name_Fixture;
            TeamB_Name_Fixture.setError(getString(R.string.input_too_short));
            verification_status = false;
        }
        else if(TeamA.equals(TeamB)){
            focusView = TeamB_Name_Fixture;
            TeamB_Name_Fixture.setError(getString(R.string.input_too_short));
            verification_status = false;
        }
        if(TeamA.length()<2){
            focusView = TeamA_Name_Fixture;
            TeamA_Name_Fixture.setError(getString(R.string.input_too_short));
            verification_status = false;
        }
        if(index==0){
            Toast.makeText(this,"Please Select An Age Group",Toast.LENGTH_LONG).show();
            verification_status = false;
        }
        else if(Time.length()<8 || Date.length()<8){
            Toast.makeText(this,"Please Select Proper Date And Time",Toast.LENGTH_LONG).show();
            verification_status = false;
        }
        if(!verification_status)
            focusView.requestFocus();
        else
            input_fixture_description = new Fixture_Description(TeamA,TeamB,Date,Time,Venue,Referee,AgeGroup,raw_time);
        return  verification_status;
    }

    public boolean get_confirmation(){
        verification_status = true;
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setMessage("This newly scheduled match will be added to the Fixtures and will be visible to all users.\nDo you want to add it?");
        alertDialog.setPositiveButton("Yes,Do It", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                verification_status = true;
                dialog.cancel();
            }
        }).setNegativeButton("No,Let Me Recheck", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                verification_status = false;
                dialog.cancel();
            }
        });
        alertDialog.show();
        return verification_status;
    }

    public void Fetching_Fixtures_From_Firebase(){

        databaseReference.child(age_group).child("Fixtures").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list_of_fixtures = new ArrayList<Fixture>();
                if(dataSnapshot.getValue()==null){
                    recycler_view_fixtures.setAdapter(new FixtureListAdapter(Add_Matches_To_Fixtures.this,list_of_fixtures,team_profile_pic_download_urls));
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
                        //Configure Adapter for ListView
                        recycler_view_fixtures.setAdapter(new FixtureListAdapter(Add_Matches_To_Fixtures.this,list_of_fixtures,team_profile_pic_download_urls));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(Add_Matches_To_Fixtures.this,"Some Error Occurred",Toast.LENGTH_LONG).show();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Add_Matches_To_Fixtures.this,"Some Error Occurred",Toast.LENGTH_LONG).show();
            }
        });

    }

    //Helper functions

    public String properly_format_input(String s){
        String array[] = s.split(" ");
        StringBuilder stringBuilder = new StringBuilder("");
        for(String a:array) {
            stringBuilder.append(a.substring(0, 1).toUpperCase());
            stringBuilder.append(a.substring(1).toLowerCase()).append(" ");
        }
        return stringBuilder.toString().trim();
    }

    public String pushId(String date,String Raw_Time) throws ParseException{

        String Push_ID;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        String dateinString = date+ " " + Raw_Time;
        Date d = sdf.parse(dateinString);
        c.setTime(d);
        Push_ID = (c.getTimeInMillis())+"";
        return Push_ID;
    }

    class Fixture_Description{
        String TeamA,TeamB,Date,Time,Venue,Referee,AgeGroup;
        String Raw_Time;
        Fixture_Description(String TeamA,String TeamB,String Date,String Time,String Venue,String Referee,String AgeGroup,String Raw_Time){
            this.TeamA = TeamA;
            this.TeamB = TeamB;
            this.Date = Date;
            this.Time = Time;
            this.Venue = Venue;
            this.Referee = Referee;
            this.AgeGroup = AgeGroup;
            this.Raw_Time = Raw_Time;
        }
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

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context,"Press longer to delete the match",Toast.LENGTH_LONG).show();
            }
            });
        }
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
