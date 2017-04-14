package in.sportscult.sportscultback;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


public class LiveMatchInformationFeeder extends AppCompatActivity {

    private static String UniqueMatchID,AgeGroup;
    private static DatabaseReference LiveMatchReference,TeamProfileReference;
    private static final DatabaseReference RootReference = FirebaseDatabase.getInstance().getReference();
    private TextView teamA_name,teamB_name,live_match_teama_goals,live_match_teamb_goals,live_match_start_time,live_match_age_group;
    private ImageView teamA_image,teamB_image;
    private static AlertDialog.Builder alertDialog;
    private static ArrayList<LeaderboardInformation> leaderboardInformationsArrayList;
    private static Map<String,Integer> PositionOfTeamsInArrayList;
    private static int RedCardsA,RedCardsB;
    private static Button add_the_playing7_button;
    private static final String ZERO = "0";
    private static final int WIN_POINTS = 5;
    private static final int LOOSE_POINTS = 1;
    private static final int DRAW_POINTS = 3;

    //Configuring Different Input Fields
    private static View view1,view2,view3;
    private static RadioGroup GoalsRadioGroup,SubstitutionRadioGroup,RedCardsRadioGroup;
    private static EditText GoalsJerseyNumber,RedCardsJerseyNumber,SubstitutionJerseyNumberOut,SubstitutionJerseyNumberIn;
    private static EditText GoalsPlayerName,RedCardsPlayerName,SubstitutionPlayerNameOut,SubstitutionPlayerNameIn;
    private static RadioButton GoalsTeamA,GoalsTeamB,RedCardsTeamA,RedCardsTeamB,SubstitutionTeamA,SubstitutionTeamB;
    private static Spinner GoalsTimeSpinner,RedCardsTimeSpinner,SubstitutionTimeSpinner;
    private static Button GoalsFile,SubstitutionFile,RedCardsFile;
    private static TextView GoalsCancel,SubstitutionCancel,RedCardsCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_match_information_feeder);

        teamA_name = (TextView)findViewById(R.id.teamA_name);
        teamB_name = (TextView)findViewById(R.id.teamB_name);
        live_match_teama_goals = (TextView)findViewById(R.id.live_match_teama_goals);
        live_match_teamb_goals = (TextView)findViewById(R.id.live_match_teamb_goals);
        live_match_start_time = (TextView)findViewById(R.id.live_match_start_time);
        live_match_age_group = (TextView)findViewById(R.id.live_match_age_group);
        teamA_image = (ImageView)findViewById(R.id.teamA_image);
        teamB_image = (ImageView)findViewById(R.id.teamB_image);
        add_the_playing7_button = (Button)findViewById(R.id.add_the_playing7_button);

        view1 = findViewById(R.id.goal_information_fields);
        GoalsRadioGroup = (RadioGroup)view1.findViewById(R.id.radiogroup);
        GoalsJerseyNumber = (EditText)view1.findViewById(R.id.player_jersey_number);
        GoalsPlayerName = (EditText)view1.findViewById(R.id.player_name);
        GoalsTimeSpinner = (Spinner)view1.findViewById(R.id.time_in_minutes_spinner_goal);
        GoalsTeamA = (RadioButton)view1.findViewById(R.id.radio_button_teamA);
        GoalsTeamB = (RadioButton)view1.findViewById(R.id.radio_button_teamB);
        GoalsFile = (Button)view1.findViewById(R.id.file_the_input);
        GoalsCancel = (TextView)view1.findViewById(R.id.cancel_button_goals);

        view2 = findViewById(R.id.substitution_information_fields);
        SubstitutionRadioGroup = (RadioGroup)view2.findViewById(R.id.radiogroup);
        SubstitutionJerseyNumberOut = (EditText)view2.findViewById(R.id.player_out_jersey_number);
        SubstitutionJerseyNumberIn = (EditText)view2.findViewById(R.id.player_in_jersey_number);
        SubstitutionPlayerNameOut = (EditText)view2.findViewById(R.id.player_out_name);
        SubstitutionPlayerNameIn = (EditText)view2.findViewById(R.id.player_in_name);
        SubstitutionTimeSpinner = (Spinner)view2.findViewById(R.id.time_in_minutes_spinner_substitution);
        SubstitutionTeamA = (RadioButton)view2.findViewById(R.id.radio_button_teamA);
        SubstitutionTeamB = (RadioButton)view2.findViewById(R.id.radio_button_teamB);
        SubstitutionFile = (Button)view2.findViewById(R.id.file_the_substitution);
        SubstitutionCancel = (TextView)view2.findViewById(R.id.cancel_button_substitution);

        view3 = findViewById(R.id.red_cards_information_fields);
        RedCardsRadioGroup = (RadioGroup)view3.findViewById(R.id.radiogroup);
        RedCardsJerseyNumber = (EditText)view3.findViewById(R.id.player_jersey_number);
        RedCardsPlayerName = (EditText)view3.findViewById(R.id.player_name);
        RedCardsTimeSpinner = (Spinner)view3.findViewById(R.id.time_in_minutes_spinner_goal);
        RedCardsTeamA = (RadioButton)view3.findViewById(R.id.radio_button_teamA);
        RedCardsTeamB = (RadioButton)view3.findViewById(R.id.radio_button_teamB);
        RedCardsFile = (Button)view3.findViewById(R.id.file_the_input);
        RedCardsCancel = (TextView)findViewById(R.id.cancel_button_goals);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            UniqueMatchID = bundle.getString("Live Match ID");
            AgeGroup = bundle.getString("Age Group");
        }
        TeamProfileReference = RootReference.child(AgeGroup).child("Team Names");
        LiveMatchReference = RootReference.child("Live Matches").child(UniqueMatchID);

        SetUpLiveMatchCard();

        String possible_minutes[] = new String[101];
        possible_minutes[0] = "Select";
        for(int i=1;i<101;i++)
            possible_minutes[i]=(i+"");
        final ArrayAdapter<String> age_group_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,possible_minutes);
        age_group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        GoalsTimeSpinner.setAdapter(age_group_adapter);
        RedCardsTimeSpinner.setAdapter(age_group_adapter);
        SubstitutionTimeSpinner.setAdapter(age_group_adapter);

        alertDialog = new AlertDialog.Builder(this);

        GoalsFile.setText("Register");
        GoalsFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register_A_Goal();
            }
        });

        RedCardsFile.setText("Register");
        RedCardsFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register_A_Red_Card();
            }
        });

        SubstitutionFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register_A_Substitution();
            }
        });

        GoalsCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear_the_goals_fields();
            }
        });

        SubstitutionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear_the_substitution_fields();
            }
        });

        RedCardsCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear_the_red_cards_fields();
            }
        });
    }

    public void add_the_playing7(View view){

        Intent intent = new Intent(LiveMatchInformationFeeder.this,AddLineup.class);
        intent.putExtra("Live Match ID",UniqueMatchID);
        intent.putExtra("Team A",teamA_name.getText().toString());
        intent.putExtra("Team B",teamB_name.getText().toString());
        startActivity(intent);

    }

    public void SetUpLiveMatchCard(){

        LiveMatchReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()==null)
                    return;
                final Map<String,String> map = (Map<String,String>)dataSnapshot.getValue();
                String TeamA = map.get("Team A");
                teamA_name.setText(TeamA);
                GoalsTeamA.setText(TeamA);
                SubstitutionTeamA.setText(TeamA);
                RedCardsTeamA.setText(TeamA);
                String TeamB = map.get("Team B");
                teamB_name.setText(TeamB);
                GoalsTeamB.setText(TeamB);
                SubstitutionTeamB.setText(TeamB);
                RedCardsTeamB.setText(TeamB);
                add_the_playing7_button.setClickable(true);
                live_match_teama_goals.setText(map.get("Team A Goals"));
                live_match_teamb_goals.setText(map.get("Team B Goals"));
                live_match_age_group.setText(map.get("Age Group"));
                live_match_start_time.setText(map.get("Start Time"));
                TeamProfileReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final Map<String,String> team_profile_pic_download_urls = new HashMap<String, String>();

                        for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                            Map<String, String> urlmap = (Map<String, String>) childSnapshot.getValue();
                            String obtainedteamname = childSnapshot.getKey();
                            team_profile_pic_download_urls.put(obtainedteamname, urlmap.get("Team Profile Pic Thumbnail Url"));
                        }
                        Picasso.with(LiveMatchInformationFeeder.this)
                                .load(team_profile_pic_download_urls.get(map.get("Team A")))
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .into(teamA_image, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        //Try again online if cache failed
                                        Picasso.with(LiveMatchInformationFeeder.this)
                                                .load(team_profile_pic_download_urls.get(map.get("Team A")))
                                                //.error(R.drawable.common_full_open_on_phone)
                                                .into(teamA_image, new Callback() {
                                                    @Override
                                                    public void onSuccess() {

                                                    }

                                                    @Override
                                                    public void onError() {
                                                    }
                                                });
                                    }
                                });
                        Picasso.with(LiveMatchInformationFeeder.this)
                                .load(team_profile_pic_download_urls.get(map.get("Team B")))
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .into(teamB_image, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        //Try again online if cache failed
                                        Picasso.with(LiveMatchInformationFeeder.this)
                                                .load(team_profile_pic_download_urls.get(map.get("Team B")))
                                                //.error(R.drawable.common_full_open_on_phone)
                                                .into(teamB_image, new Callback() {
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
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void Register_A_Goal(){

        alertDialog.setMessage("Are You sure you want to add the goal to the scorecard.\nThe change will be permanent.");
        alertDialog.setPositiveButton("Add The Goal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Get The Information
                int radioButtonID = GoalsRadioGroup.getCheckedRadioButtonId();
                View checkedRadioButton = GoalsRadioGroup.findViewById(radioButtonID);
                if(checkedRadioButton==null) {
                    Toast.makeText(LiveMatchInformationFeeder.this,"No Team Selected",Toast.LENGTH_SHORT).show();
                    return;
                }
                int idx = GoalsRadioGroup.indexOfChild(checkedRadioButton);
                //idx=0 signifies Team A and idx=1 shows Team B
                String child_name;
                String goals;
                String TeamName;
                if(idx==0){
                    child_name = "Team A Goals";
                    TeamName = teamA_name.getText().toString();
                    goals = "" + (1 + Integer.parseInt(live_match_teama_goals.getText().toString()));
                }
                else{
                    child_name = "Team B Goals";
                    TeamName = teamB_name.getText().toString();
                    goals = "" + (1 + Integer.parseInt(live_match_teamb_goals.getText().toString()));
                }
                //int GoalsForTeamA = Integer.parseInt(live_match_teama_goals.getText().toString());
                //int GoalsForeTeamB = Integer.parseInt(live_match_teamb_goals.getText().toString());

                String JerseyNumber = GoalsJerseyNumber.getText().toString();
                String PlayerName = RegistrationActivity.properly_format_input(GoalsPlayerName.getText().toString());

                if(JerseyNumber.length()<1 || PlayerName.length()<2){
                    Toast.makeText(LiveMatchInformationFeeder.this,"Invalid Player Details",Toast.LENGTH_SHORT).show();
                    return;
                }
                try{
                    Integer.parseInt(JerseyNumber);
                }catch (Exception e){
                    Toast.makeText(LiveMatchInformationFeeder.this,"Invalid Jersey Number",Toast.LENGTH_SHORT).show();
                    return;
                }
                int selected = GoalsTimeSpinner.getSelectedItemPosition();
                if(selected==0){
                    Toast.makeText(LiveMatchInformationFeeder.this,"Please enter time of goal.",Toast.LENGTH_SHORT).show();
                    return;
                }
                String TimeOfGoal = selected+"";

                String Key = TimeOfGoal+PlayerName;
                LiveMatchReference.child(child_name).setValue(goals);
                Map<String,String> goaluploadData = new HashMap<String, String>();
                goaluploadData.put("Team Name",TeamName);
                goaluploadData.put("Player Name",PlayerName);
                goaluploadData.put("Player Jersey Number",JerseyNumber);
                goaluploadData.put("Time",TimeOfGoal);
                LiveMatchReference.child("Goals").child(Key).setValue(goaluploadData);
                clear_the_goals_fields();
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

    public void Register_A_Red_Card(){

        alertDialog.setMessage("Are You sure you want to add the red card to the match.\nThe change will be permanent.");
        alertDialog.setPositiveButton("Add The Red Card", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Get The Information
                int radioButtonID = RedCardsRadioGroup.getCheckedRadioButtonId();
                View checkedRadioButton = RedCardsRadioGroup.findViewById(radioButtonID);
                if(checkedRadioButton==null) {
                    Toast.makeText(LiveMatchInformationFeeder.this,"No Team Selected",Toast.LENGTH_SHORT).show();
                    return;
                }
                int idx = RedCardsRadioGroup.indexOfChild(checkedRadioButton);
                //idx=0 signifies Team A and idx=1 shows Team B
                String TeamName;
                if(idx==0){
                    TeamName = teamA_name.getText().toString();
                }
                else{
                    TeamName = teamB_name.getText().toString();
                }

                String JerseyNumber = RedCardsJerseyNumber.getText().toString();
                String PlayerName = RegistrationActivity.properly_format_input(RedCardsPlayerName.getText().toString());

                if(JerseyNumber.length()<1 || PlayerName.length()<2){
                    Toast.makeText(LiveMatchInformationFeeder.this,"Invalid Player Details",Toast.LENGTH_SHORT).show();
                    return;
                }
                try{
                    Integer.parseInt(JerseyNumber);
                }catch (Exception e){
                    Toast.makeText(LiveMatchInformationFeeder.this,"Invalid Jersey Number",Toast.LENGTH_SHORT).show();
                    return;
                }
                int selected = RedCardsTimeSpinner.getSelectedItemPosition();
                if(selected==0){
                    Toast.makeText(LiveMatchInformationFeeder.this,"Please enter time of Red Card Given.",Toast.LENGTH_SHORT).show();
                    return;
                }
                String TimeOfEvent = selected+"";

                String Key = TimeOfEvent+PlayerName;
                Map<String,String> redCardUploadData = new HashMap<String, String>();
                redCardUploadData.put("Team Name",TeamName);
                redCardUploadData.put("Player Name",PlayerName);
                redCardUploadData.put("Player Jersey Number",JerseyNumber);
                redCardUploadData.put("Time",TimeOfEvent);
                LiveMatchReference.child("Red Cards").child(Key).setValue(redCardUploadData);
                clear_the_red_cards_fields();
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

    public  void Register_A_Substitution(){

        alertDialog.setMessage("Are You sure you want to add the substitution to the match.\nThe change will be permanent.");
        alertDialog.setPositiveButton("Add The Substitution", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Get The Information
                int radioButtonID = SubstitutionRadioGroup.getCheckedRadioButtonId();
                View checkedRadioButton = SubstitutionRadioGroup.findViewById(radioButtonID);
                if(checkedRadioButton==null) {
                    Toast.makeText(LiveMatchInformationFeeder.this,"No Team Selected",Toast.LENGTH_SHORT).show();
                    return;
                }
                int idx = SubstitutionRadioGroup.indexOfChild(checkedRadioButton);
                //idx=0 signifies Team A and idx=1 shows Team B
                String TeamName;
                if(idx==0){
                    TeamName = teamA_name.getText().toString();
                }
                else{
                    TeamName = teamB_name.getText().toString();
                }

                String JerseyNumberOut = SubstitutionJerseyNumberOut.getText().toString();
                String PlayerNameOut = RegistrationActivity.properly_format_input(SubstitutionPlayerNameOut.getText().toString());
                String JerseyNumberIn = SubstitutionJerseyNumberIn.getText().toString();
                String PlayerNameIn = RegistrationActivity.properly_format_input(SubstitutionPlayerNameIn.getText().toString());

                if(JerseyNumberOut.length()<1 || PlayerNameOut.length()<2 || JerseyNumberIn.length()<1 || PlayerNameIn.length()<2){
                    Toast.makeText(LiveMatchInformationFeeder.this,"Invalid Player Details",Toast.LENGTH_SHORT).show();
                    return;
                }
                try{
                    Integer.parseInt(JerseyNumberOut);
                    Integer.parseInt(JerseyNumberIn);
                }catch (Exception e){
                    Toast.makeText(LiveMatchInformationFeeder.this,"Invalid Jersey Number",Toast.LENGTH_SHORT).show();
                    return;
                }
                int selected = SubstitutionTimeSpinner.getSelectedItemPosition();
                if(selected==0){
                    Toast.makeText(LiveMatchInformationFeeder.this,"Please enter time of Substitution.",Toast.LENGTH_SHORT).show();
                    return;
                }
                String TimeOfEvent = selected+"";

                String Key = TimeOfEvent+PlayerNameOut+PlayerNameIn;
                Map<String,String> substitutionUploadData = new HashMap<String, String>();
                substitutionUploadData.put("Team Name",TeamName);
                substitutionUploadData.put("Player Name Out",PlayerNameOut);
                substitutionUploadData.put("Player Jersey Number Out",JerseyNumberOut);
                substitutionUploadData.put("Player Name In",PlayerNameIn);
                substitutionUploadData.put("Player Jersey Number In",JerseyNumberIn);
                substitutionUploadData.put("Time",TimeOfEvent);
                LiveMatchReference.child("Substitutions").child(Key).setValue(substitutionUploadData);
                clear_the_substitution_fields();
                dialog.cancel();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        //Clear The Fields
        alertDialog.show();

    }

    public void clear_the_goals_fields(){

        GoalsTeamA.setChecked(false);
        GoalsTeamB.setChecked(false);
        GoalsJerseyNumber.setText("");
        GoalsPlayerName.setText("");
        GoalsTimeSpinner.setSelection(0);

    }

    public void clear_the_substitution_fields(){

        SubstitutionTeamA.setChecked(false);
        SubstitutionTeamB.setChecked(false);
        SubstitutionJerseyNumberOut.setText("");
        SubstitutionPlayerNameOut.setText("");
        SubstitutionJerseyNumberIn.setText("");
        SubstitutionPlayerNameIn.setText("");
        SubstitutionTimeSpinner.setSelection(0);

    }

    public void clear_the_red_cards_fields(){

        RedCardsTeamA.setChecked(false);
        RedCardsTeamB.setChecked(false);
        RedCardsJerseyNumber.setText("");
        RedCardsPlayerName.setText("");
        RedCardsTimeSpinner.setSelection(0);

    }

    public void conclude_live_match(View view){

        alertDialog.setMessage("Are You Sure You Want To Conclude The Match?");
        alertDialog.setPositiveButton("Yes,Conclude Match", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                LiveMatchReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //Obtain All The Data Of The Concluded Live Match
                        final Map<String,String> map = (Map<String,String>)dataSnapshot.getValue();
                        final String Team_A = map.get("Team A");
                        final String Team_B = map.get("Team B");
                        final String Number_Of_Goals_A = map.get("Team A Goals");
                        final String Number_Of_Goals_B = map.get("Team B Goals");
                        String Venue_Of_Match = map.get("Venue");
                        String Start_Time_Of_Match = map.get("Start Time");
                        String Age_Group_Of_Match = map.get("Age Group");

                        ArrayList<GoalsOrRedCards> GoalsArrayList = new ArrayList<GoalsOrRedCards>();
                        ArrayList<GoalsOrRedCards> RedCardsArrayList = new ArrayList<GoalsOrRedCards>();
                        ArrayList<Substitutions> SubstitutionsArrayList = new ArrayList<Substitutions>();
                        ArrayList<Player> lineupteamA = new ArrayList<Player>();
                        ArrayList<Player> lineupteamB = new ArrayList<Player>();

                        DataSnapshot dataSnapshotGoals = dataSnapshot.child("Goals");
                        for(DataSnapshot childSnapshot : dataSnapshotGoals.getChildren()){
                            Map<String,String> map1 = (Map<String,String>) childSnapshot.getValue();
                            GoalsArrayList.add(new GoalsOrRedCards(map1.get("Team Name"),map1.get("Player Name"),map1.get("Player Jersey Number"),map1.get("Time")));
                        }

                        RedCardsA = RedCardsB = 0;
                        DataSnapshot dataSnapshotRedCards = dataSnapshot.child("Red Cards");
                        for(DataSnapshot childSnapshot : dataSnapshotRedCards.getChildren()){
                            Map<String,String> map1 = (Map<String,String>)childSnapshot.getValue();
                            if(map1.get("Team Name").equals(Team_A))
                                RedCardsA++;
                            else
                                RedCardsB++;
                            RedCardsArrayList.add(new GoalsOrRedCards(map1.get("Team Name"),map1.get("Player Name"),map1.get("Player Jersey Number"),map1.get("Time")));
                        }
                        DataSnapshot dataSnapshotSubstitutions = dataSnapshot.child("Substitutions");
                        for(DataSnapshot childSnapshot : dataSnapshotSubstitutions.getChildren()){
                            Map<String,String> map1 = (Map<String,String>) childSnapshot.getValue();
                            SubstitutionsArrayList.add(new Substitutions(map1.get("Team Name"),map1.get("Player Name Out"),map1.get("Player Jersey Number Out"),map1.get("Player Name In"),
                                    map1.get("Player Jersey Number In"),map1.get("Time")));
                        }

                        DataSnapshot TeamALineupSnapshot = dataSnapshot.child("Lineups").child("Team A");
                        for(DataSnapshot childSnapshot : TeamALineupSnapshot.getChildren()){
                            Map<String,String> map1 = (HashMap<String,String>) childSnapshot.getValue();
                            lineupteamA.add(new Player(map1.get("Jersey Number"),map1.get("Player Name")));
                        }
                        DataSnapshot TeamBLineupSnapshot = dataSnapshot.child("Lineups").child("Team B");
                        for(DataSnapshot childSnapshot : TeamBLineupSnapshot.getChildren()){
                            Map<String,String> map1 = (HashMap<String,String>) childSnapshot.getValue();
                            lineupteamB.add(new Player(map1.get("Jersey Number"),map1.get("Player Name")));
                        }
                        //Data Retrieval Complete
                        //The match has yet to be removed from the live match page

                        //Upload The Data To The Results Section
                        DatabaseReference ResultsPageReference = RootReference.child(Age_Group_Of_Match).child("Results").child(UniqueMatchID);

                        Map<String,String> uploadData = new HashMap<String, String>();
                        uploadData.put("Team A",Team_A);
                        uploadData.put("Team B",Team_B);
                        uploadData.put("Team A Goals",Number_Of_Goals_A);
                        uploadData.put("Team B Goals",Number_Of_Goals_B);
                        uploadData.put("Venue", Venue_Of_Match);
                        uploadData.put("Start Time",Start_Time_Of_Match);
                        ResultsPageReference.setValue(uploadData);

                        for(GoalsOrRedCards goalsOrRedCards : GoalsArrayList){
                            String Key = goalsOrRedCards.ResponsibleTime+goalsOrRedCards.ResponsiblePlayerName;
                            Map<String,String> goaluploadData = new HashMap<String, String>();
                            goaluploadData.put("Team Name",goalsOrRedCards.ResponsibleTeamName);
                            goaluploadData.put("Player Name",goalsOrRedCards.ResponsiblePlayerName);
                            goaluploadData.put("Player Jersey Number",goalsOrRedCards.ResponsiblePlayerJerseyNumber);
                            goaluploadData.put("Time",goalsOrRedCards.ResponsibleTime);
                            ResultsPageReference.child("Goals").child(Key).setValue(goaluploadData);
                        }
                        for(GoalsOrRedCards goalsOrRedCards : RedCardsArrayList){
                            String Key = goalsOrRedCards.ResponsibleTime+goalsOrRedCards.ResponsiblePlayerName;
                            Map<String,String> redCardUploadData = new HashMap<String, String>();
                            redCardUploadData.put("Team Name",goalsOrRedCards.ResponsibleTeamName);
                            redCardUploadData.put("Player Name",goalsOrRedCards.ResponsiblePlayerName);
                            redCardUploadData.put("Player Jersey Number",goalsOrRedCards.ResponsiblePlayerJerseyNumber);
                            redCardUploadData.put("Time",goalsOrRedCards.ResponsibleTime);
                            ResultsPageReference.child("Red Cards").child(Key).setValue(redCardUploadData);
                        }
                        for(Substitutions substitutions : SubstitutionsArrayList){
                            String Key = substitutions.SubstitutionsTime+substitutions.SubstitutionsPlayerJerseyNumberOut+substitutions.SubstitutionsPlayerNameIn;
                            Map<String,String> substitutionUploadData = new HashMap<String, String>();
                            substitutionUploadData.put("Team Name",substitutions.SubstitutionsTeamName);
                            substitutionUploadData.put("Player Name Out",substitutions.SubstitutionsPlayerNameOut);
                            substitutionUploadData.put("Player Jersey Number Out",substitutions.SubstitutionsPlayerJerseyNumberOut);
                            substitutionUploadData.put("Player Name In",substitutions.SubstitutionsPlayerNameIn);
                            substitutionUploadData.put("Player Jersey Number In",substitutions.SubstitutionsPlayerJerseyNumberIn);
                            substitutionUploadData.put("Time",substitutions.SubstitutionsTime);
                            ResultsPageReference.child("Substitutions").child(Key).setValue(substitutionUploadData);
                        }
                        for(Player player : lineupteamA){
                            Map<String,String> lineupuploadData=  new HashMap<String,String>();
                            lineupuploadData.put("Player Name",player.PlayerName);
                            lineupuploadData.put("jersey Number",player.JerseyNumber);
                            ResultsPageReference.child("Lineups").child("Team A").push().setValue(lineupuploadData);
                        }
                        for(Player player : lineupteamB){
                            Map<String,String> lineupuploadData=  new HashMap<String,String>();
                            lineupuploadData.put("Player Name",player.PlayerName);
                            lineupuploadData.put("jersey Number",player.JerseyNumber);
                            ResultsPageReference.child("Lineups").child("Team B").push().setValue(lineupuploadData);
                        }
                        //Uploading the Data To The Results Page Complete

                        //Remove the match from the live match page
                        LiveMatchReference.removeValue();

                        //Get The Data From The Leaderboard Section
                        final DatabaseReference LeaderboardReference = RootReference.child(Age_Group_Of_Match).child("Leaderboard");
                        LeaderboardReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                leaderboardInformationsArrayList = new ArrayList<LeaderboardInformation>();
                                PositionOfTeamsInArrayList = new HashMap<String, Integer>();

                                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                                    Map<String,String> map1 = (Map<String,String>)childSnapshot.getValue();
                                    LeaderboardInformation fetchedinformation = new LeaderboardInformation(map1.get("Team Name"),map1.get("Matches Played"),map1.get("Matches Won"),
                                            map1.get("Matches Drawn"),map1.get("Matches Lost"),map1.get("Goals Scored"),map1.get("Goals Conceived"),map1.get("Red Cards"),map1.get("Points"));
                                    PositionOfTeamsInArrayList.put(map1.get("Team Name"),leaderboardInformationsArrayList.size());
                                    leaderboardInformationsArrayList.add(fetchedinformation);
                                }
                                LeaderboardReference.removeValue();
                                //Match Data Before The Current Match Has Been Put

                                //Now Putting The Current Match Data
                                int indexA = PositionOfTeamsInArrayList.get(Team_A);
                                int indexB = PositionOfTeamsInArrayList.get(Team_B);

                                LeaderboardInformation informationA = leaderboardInformationsArrayList.get(indexA);
                                informationA.RedCards = ""+(Integer.parseInt(informationA.RedCards)+RedCardsA);
                                informationA.GoalsConceived = ""+(Integer.parseInt(informationA.GoalsConceived)+Integer.parseInt(Number_Of_Goals_B));
                                informationA.GoalsScored = ""+(Integer.parseInt(informationA.GoalsScored)+Integer.parseInt(Number_Of_Goals_A));
                                informationA.MatchesPlayed = ""+(Integer.parseInt(informationA.MatchesPlayed)+1);

                                LeaderboardInformation informationB = leaderboardInformationsArrayList.get(indexB);
                                informationB.RedCards = ""+(Integer.parseInt(informationB.RedCards)+RedCardsB);
                                informationB.GoalsConceived = ""+(Integer.parseInt(informationB.GoalsConceived)+Integer.parseInt(Number_Of_Goals_A));
                                informationB.GoalsScored = ""+(Integer.parseInt(informationB.GoalsScored)+Integer.parseInt(Number_Of_Goals_B));
                                informationB.MatchesPlayed = ""+(Integer.parseInt(informationB.MatchesPlayed)+1);

                                if(Integer.parseInt(Number_Of_Goals_A)>Integer.parseInt(Number_Of_Goals_B)){
                                    informationA.Points = ""+(Integer.parseInt(informationA.Points)+WIN_POINTS);
                                    informationB.Points = ""+(Integer.parseInt(informationB.Points)+LOOSE_POINTS);
                                    informationA.MathcesWon = ""+(Integer.parseInt(informationA.MathcesWon)+1);
                                    informationB.MatchesLost = ""+(Integer.parseInt(informationB.MatchesLost)+1);
                                }
                                else if(Integer.parseInt(Number_Of_Goals_A)<Integer.parseInt(Number_Of_Goals_B)){
                                    informationA.Points = ""+(Integer.parseInt(informationA.Points)+LOOSE_POINTS);
                                    informationB.Points = ""+(Integer.parseInt(informationB.Points)+WIN_POINTS);
                                    informationA.MatchesLost = ""+(Integer.parseInt(informationA.MatchesLost)+1);
                                    informationB.MathcesWon = ""+(Integer.parseInt(informationB.MathcesWon)+1);
                                }
                                else {
                                    informationA.Points = ""+(Integer.parseInt(informationA.Points)+DRAW_POINTS);
                                    informationB.Points = ""+(Integer.parseInt(informationB.Points)+DRAW_POINTS);
                                    informationA.MatchesDrawn = ""+(Integer.parseInt(informationA.MatchesDrawn)+1);
                                    informationB.MatchesDrawn = ""+(Integer.parseInt(informationB.MatchesDrawn)+1);
                                }

                                leaderboardInformationsArrayList.remove(indexA);
                                leaderboardInformationsArrayList.add(indexA,informationA);
                                leaderboardInformationsArrayList.remove(indexB);
                                leaderboardInformationsArrayList.add(indexB,informationB);
                                //Sort The Match List
                                Collections.sort(leaderboardInformationsArrayList,new GenerateLeaderboard());

                                //Update The Sorted List Of Leaderboard On The Server
                                int idx = 1;
                                for(LeaderboardInformation info : leaderboardInformationsArrayList){
                                    String Key = idx+"-"+info.TeamName;
                                    Map<String,String> leaderboardData = new HashMap<String, String>();
                                    leaderboardData.put("Team Name",info.TeamName);
                                    leaderboardData.put("Matches Played",info.MatchesPlayed);
                                    leaderboardData.put("Matches Won",info.MathcesWon);
                                    leaderboardData.put("Matches Drawn",info.MatchesDrawn);
                                    leaderboardData.put("Matches Lost",info.MatchesLost);
                                    leaderboardData.put("Goals Scored",info.GoalsScored);
                                    leaderboardData.put("Goals Conceived",info.GoalsConceived);
                                    leaderboardData.put("Red Cards",info.RedCards);
                                    leaderboardData.put("Points",info.Points);
                                    LeaderboardReference.child(Key).setValue(leaderboardData);
                                    idx++;
                                }

                                //All The Data Has been updated
                                //Open The new Activity here
                                Intent intent = new Intent(LiveMatchInformationFeeder.this,Interface.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        //Retrieval From Leaderboards Page Complete
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

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

}

class GoalsOrRedCards{
    String ResponsibleTeamName,ResponsiblePlayerName,ResponsiblePlayerJerseyNumber,ResponsibleTime;
    GoalsOrRedCards(String ResponsibleTeamName,String ResponsiblePlayerName,String ResponsiblePlayerJerseyNumber,String ResponsibleTime){
        this.ResponsibleTeamName = ResponsibleTeamName;
        this.ResponsiblePlayerName = ResponsiblePlayerName;
        this.ResponsiblePlayerJerseyNumber = ResponsiblePlayerJerseyNumber;
        this.ResponsibleTime = ResponsibleTime;
    }
}

class Substitutions{
    String SubstitutionsTeamName,SubstitutionsPlayerNameOut,SubstitutionsPlayerJerseyNumberOut,SubstitutionsPlayerNameIn,SubstitutionsPlayerJerseyNumberIn,SubstitutionsTime;
    Substitutions(String SubstitutionsTeamName,String SubstitutionsPlayerNameOut,String SubstitutionsPlayerJerseyNumberOut,String SubstitutionsPlayerNameIn,String SubstitutionsPlayerJerseyNumberIn,String SubstitutionsTime){
        this.SubstitutionsTeamName = SubstitutionsTeamName;
        this.SubstitutionsPlayerNameOut = SubstitutionsPlayerNameOut;
        this.SubstitutionsPlayerJerseyNumberOut = SubstitutionsPlayerJerseyNumberOut;
        this.SubstitutionsPlayerNameIn = SubstitutionsPlayerNameIn;
        this.SubstitutionsPlayerJerseyNumberIn = SubstitutionsPlayerJerseyNumberIn;
        this.SubstitutionsTime = SubstitutionsTime;
    }
}

class LeaderboardInformation{
    String TeamName,MatchesPlayed,MathcesWon,MatchesDrawn,MatchesLost,GoalsScored,GoalsConceived,RedCards,Points;
    LeaderboardInformation(String TeamName,String MatchesPlayed,String MatchesWon,String MatchesDrawn,String MatchesLost,String GoalsScored,String GoalsConceived,String RedCards,String Points ){
        this.TeamName = TeamName;
        this.MatchesPlayed = MatchesPlayed;
        this.MathcesWon = MatchesWon;
        this.MatchesDrawn = MatchesDrawn;
        this.MatchesLost = MatchesLost;
        this.GoalsScored = GoalsScored;
        this.GoalsConceived = GoalsConceived;
        this.RedCards = RedCards;
        this.Points = Points;
    }
}

class GenerateLeaderboard implements Comparator<LeaderboardInformation>{

    @Override
    public int compare(LeaderboardInformation o1, LeaderboardInformation o2) {
        //Compare Points
        int Points1 = Integer.parseInt(o1.Points);
        int Points2 = Integer.parseInt(o2.Points);
        if(Points1>Points2)
            return -1;
        else if(Points1<Points2)
            return 1;
        //Compare Matches Played
        Points1 = Integer.parseInt(o1.MatchesPlayed);
        Points2 = Integer.parseInt(o2.MatchesPlayed);
        if(Points1<Points2)
            return -1;
        else if(Points1>Points2)
            return 1;
        //Compare Matches Won
        Points1 = Integer.parseInt(o1.MathcesWon);
        Points2 = Integer.parseInt(o2.MathcesWon);
        if(Points1>Points2)
            return -1;
        else if(Points1>Points2)
            return 1;
        //Compare Matches Drawn
        Points1 = Integer.parseInt(o1.MatchesDrawn);
        Points2 = Integer.parseInt(o2.MatchesDrawn);
        if(Points1>Points2)
            return -1;
        else if(Points1>Points2)
            return 1;
        //Compare Goals Scored
        Points1 = Integer.parseInt(o1.GoalsScored);
        Points2 = Integer.parseInt(o2.GoalsScored);
        if(Points1>Points2)
            return -1;
        else if(Points1>Points2)
            return 1;
        //Compare Goals Conceived
        Points1 = Integer.parseInt(o1.GoalsConceived);
        Points2 = Integer.parseInt(o2.GoalsConceived);
        if(Points1<Points2)
            return -1;
        else if(Points1>Points2)
            return 1;
        //Compare Red Cards
        Points1 = Integer.parseInt(o1.RedCards);
        Points2 = Integer.parseInt(o2.RedCards);
        if(Points1<Points2)
            return -1;
        else if(Points1>Points2)
            return 1;
        else
            return o1.TeamName.compareTo(o2.TeamName);
    }
}