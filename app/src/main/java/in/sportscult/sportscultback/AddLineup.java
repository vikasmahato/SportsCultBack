package in.sportscult.sportscultback;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddLineup extends AppCompatActivity {

    private static String UniqueMatchID,TeamA,TeamB;
    private static TextView lineup_teama,lineup_teamb;
    private static EditText addMatchJerseyNumber,addMatchPlayerName;
    private static int selection_for_team;
    private static ArrayList<Player> TeamArrayListA,TeamArrayListB;
    private static DatabaseReference databaseReference;
    private static RecyclerView add_players_recyclerview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lineup);

        Bundle bundle = getIntent().getExtras();
        UniqueMatchID = bundle.getString("Live Match ID");
        TeamA = bundle.getString("Team A");
        TeamB = bundle.getString("Team B");

        lineup_teama = (TextView)findViewById(R.id.lineup_teama);
        lineup_teamb = (TextView)findViewById(R.id.lineup_teamb);
        addMatchJerseyNumber = (EditText)findViewById(R.id.addMatchJerseyNumber);
        addMatchPlayerName = (EditText)findViewById(R.id.addMatchPlayerName);
        add_players_recyclerview = (RecyclerView)findViewById(R.id.add_players_recyclerview);
        add_players_recyclerview.setLayoutManager(new LinearLayoutManager(this));

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Live Matches").child(UniqueMatchID).child("Lineups");
        lineup_teama.setText(TeamA);
        lineup_teamb.setText(TeamB);

        selection_for_team = 1;
        GetLineup();

    }

    public void add_player_to_lineup(View view){

        String PlayerName = RegistrationActivity.properly_format_input(addMatchPlayerName.getText().toString());
        String JerseyNumber = addMatchJerseyNumber.getText().toString();

        if(PlayerName.length()<2 || JerseyNumber.length()<1){
            Toast.makeText(this,"Invalid Player Details",Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String,String> player = new HashMap<>();
        player.put("Player Name",PlayerName);
        player.put("Jersey Number",JerseyNumber);
        if(selection_for_team==1)
            databaseReference.child("Team A").push().setValue(player);
        else
            databaseReference.child("Team B").push().setValue(player);
        addMatchPlayerName.setText("");
        addMatchJerseyNumber.setText("");
        addMatchJerseyNumber.requestFocus();
        //Log.d("MY CHECK","Player Added");
    }

    public void display_lineup_of_teamA(View view){

        selection_for_team = 1;
        lineup_teamb.setTextColor(getResources().getColor(R.color.splash));
        lineup_teama.setTextColor(Color.parseColor("#FFFFFF"));
        lineup_teama.setBackgroundColor(getResources().getColor(R.color.button_grey));
        lineup_teamb.setBackgroundColor(getResources().getColor(R.color.viewBg));
        add_players_recyclerview.setAdapter(new AddLinueupAdapter(this,TeamArrayListA));

    }

    public void display_lineup_of_teamB(View view){

        selection_for_team = 2;
        lineup_teama.setTextColor(getResources().getColor(R.color.splash));
        lineup_teamb.setTextColor(Color.parseColor("#FFFFFF"));
        lineup_teama.setBackgroundColor(getResources().getColor(R.color.viewBg));
        lineup_teamb.setBackgroundColor(getResources().getColor(R.color.button_grey));
        add_players_recyclerview.setAdapter(new AddLinueupAdapter(this,TeamArrayListB));
    }

    public void GetLineup(){

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TeamArrayListA = new ArrayList<Player>();
                TeamArrayListB = new ArrayList<Player>();

                DataSnapshot TeamASnapshot = dataSnapshot.child("Team A");
                for(DataSnapshot childSnapshot : TeamASnapshot.getChildren()){
                    Map<String,String> map = (Map<String,String>) childSnapshot.getValue();
                    TeamArrayListA.add(new Player(map.get("Jersey Number"),map.get("Player Name")));
                }

                DataSnapshot TeamBSnapshot = dataSnapshot.child("Team B");
                for(DataSnapshot childSnapshot : TeamBSnapshot.getChildren()){
                    Map<String,String> map = (Map<String,String>) childSnapshot.getValue();
                    TeamArrayListB.add(new Player(map.get("Jersey Number"),map.get("Player Name")));
                }

                if(selection_for_team==1)
                    display_lineup_of_teamA(lineup_teama);
                else
                    display_lineup_of_teamB(lineup_teamb);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}

class Player{
    String JerseyNumber,PlayerName;
    Player(String JerseyNumber,String PlayerName){
        this.JerseyNumber = JerseyNumber;
        this.PlayerName = PlayerName;
    }
}

class AddLinueupAdapter extends RecyclerView.Adapter<AddLineupViewHolder>{

    Context context;
    ArrayList<Player> playerArrayList;
    AddLinueupAdapter(Context context,ArrayList<Player> playerArrayList){
        this.context = context;
        this.playerArrayList = playerArrayList;
    }

    @Override
    public AddLineupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.player_description_row,parent,false);
        return new AddLineupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AddLineupViewHolder holder, int position) {

        Player temp = playerArrayList.get(position);
        holder.specific_team_player_name.setText(temp.PlayerName);
        holder.specific_team_player_jersey_number.setText(temp.JerseyNumber);
    }

    @Override
    public int getItemCount() {
        return playerArrayList.size();
    }
}

class AddLineupViewHolder extends RecyclerView.ViewHolder{

    TextView specific_team_player_jersey_number,specific_team_player_name;
    public AddLineupViewHolder(View itemView) {
        super(itemView);
        specific_team_player_jersey_number = (TextView)itemView.findViewById(R.id.specific_team_player_jersey_number);
        specific_team_player_name = (TextView)itemView.findViewById(R.id.specific_team_player_name);
    }
}