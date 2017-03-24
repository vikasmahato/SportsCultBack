package in.sportscult.sportscultback;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Add_Matches_To_Fixtures extends AppCompatActivity {

    private EditText TeamA_Name_Fixture,TeamB_Name_Fixture,Venue_Fixture,Referee_Fixture;
    private TextView Date_Fixture,Time_Fixture;
    private Spinner age_group_spinner_fixture;
    private static final String[] age_group_texts = {"Group - 0","Group - A","Group -B","Group - C","Group - D"};
    private static Fixture_Description input_fixture_description;
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private String raw_time;
    static boolean verification_status;
    private Button go_to_fixture_editing_page;


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
        go_to_fixture_editing_page = (Button)findViewById(R.id.go_to_fixture_editing_page);

        final ArrayAdapter<String> age_group_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.age_groups));
        age_group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age_group_spinner_fixture.setAdapter(age_group_adapter);
        age_group_spinner_fixture.setSelection(0);

        go_to_fixture_editing_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoToFixtureEditingPage();
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
                        Time_Fixture.setText(new StringBuilder().append(hourOfDay).append(":").append(minute)
                                .append(" ").append(format));
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    public void add_match_to_fixture(View view){



        boolean verification = get_verified_data();
        if(!verification)
            return;

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setMessage("This newly scheduled match will be added to the Fixtures and will be visible to all users.\nDo you want to add it?");
        alertDialog.setPositiveButton("Yes,Do It", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String id_to_push = null;
                try {
                    id_to_push = pushId(input_fixture_description.Date,input_fixture_description.Raw_Time)+input_fixture_description.TeamA+input_fixture_description.TeamB;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                DatabaseReference databaseReference1 = databaseReference.child(input_fixture_description.AgeGroup).child("Fixtures").child(id_to_push);
                databaseReference1.child("Date").setValue(input_fixture_description.Date);
                databaseReference1.child("Referee").setValue(input_fixture_description.Referee);
                databaseReference1.child("Team A").setValue(input_fixture_description.TeamA);
                databaseReference1.child("Team B").setValue(input_fixture_description.TeamB);
                databaseReference1.child("Time").setValue(input_fixture_description.Time);
                databaseReference1.child("Venue").setValue(input_fixture_description.Venue);

                GoToFixtureEditingPage();
                dialog.cancel();
            }
        }).setNegativeButton("No,Let Me Recheck", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();

        //Verified Data Is Now Available

        //Generate a unique key for each match which will be sort the data according to time


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
        if(!verification_status && focusView!=null)
            focusView.requestFocus();
        else
            input_fixture_description = new Fixture_Description(TeamA,TeamB,Date,Time,Venue,Referee,AgeGroup,raw_time);
        return  verification_status;
    }


    public void GoToFixtureEditingPage(){
        Intent intent = new Intent(this,Fixture_Editing_Page.class);
        intent.putExtra("Age Group Selection",""+age_group_spinner_fixture.getSelectedItemPosition());
        startActivity(intent);
    }

    //Helper functions

    public String properly_format_input(String s){
        if(s.length()==0)
            return "";
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


