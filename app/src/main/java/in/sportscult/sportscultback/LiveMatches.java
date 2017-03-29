package in.sportscult.sportscultback;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import java.util.HashMap;
import java.util.Map;

public class LiveMatches extends AppCompatActivity {

    private RecyclerView recycler_view_for_live_matches;
    private LiveMatchAdapter liveMatchAdapter;
    private Map<String,String> team_profile_pic_download_urls;
    ArrayList<LiveMatch> liveMatchArrayList;
    private ProgressDialog progressDialog;


    @Override
    public void onBackPressed() {
        Intent intent  = new Intent(LiveMatches.this,Interface.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_matches);

        recycler_view_for_live_matches = (RecyclerView) findViewById(R.id.recycler_view_for_live_matches);
        team_profile_pic_download_urls = new HashMap<String, String>();
        liveMatchArrayList = new ArrayList<LiveMatch>();


        liveMatchAdapter = new LiveMatchAdapter(this,liveMatchArrayList,team_profile_pic_download_urls);
        recycler_view_for_live_matches.setAdapter(liveMatchAdapter);
        recycler_view_for_live_matches.setLayoutManager(new LinearLayoutManager(this));

        recycler_view_for_live_matches.addOnItemTouchListener(new RecyclerItemClickListener(this, recycler_view_for_live_matches, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(LiveMatches.this,LiveMatchInformationFeeder.class);
                LiveMatch liveMatch = liveMatchArrayList.get(position);
                intent.putExtra("Age Group",liveMatch.AgeGroup);
                intent.putExtra("Live Match ID",liveMatch.UniqueID);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, final int position) {
            }
        }));

        Fetch_Live_Matches_From_Firebase();
    }

    public void Fetch_Live_Matches_From_Firebase(){

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Data....");
        progressDialog.setCancelable(false);
        //progressDialog.show();

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Live Matches").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                liveMatchArrayList = new ArrayList<LiveMatch>();
                if(dataSnapshot.getValue()==null){
                    progressDialog.dismiss();
                    recycler_view_for_live_matches.setAdapter(new LiveMatchAdapter(LiveMatches.this,liveMatchArrayList,team_profile_pic_download_urls));
                    return;
                }
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    Map<String,String> map = (Map<String,String>)childSnapshot.getValue();
                    final LiveMatch liveMatch = new LiveMatch(childSnapshot.getKey(),map.get("Team A"),map.get("Team B"),map.get("Team A Goals"),
                            map.get("Team B Goals"),map.get("Start Time"),map.get("Age Group"));
                    liveMatchArrayList.add(liveMatch);

                    databaseReference.child(liveMatch.AgeGroup).child("Team Names").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot ChildSnapshot : dataSnapshot.getChildren()) {
                                Map<String, String> urlmap = (Map<String, String>) ChildSnapshot.getValue();
                                team_profile_pic_download_urls.put(liveMatch.AgeGroup + ChildSnapshot.getKey(), urlmap.get("Team Profile Pic Thumbnail Url"));
                            }
                            progressDialog.dismiss();
                            recycler_view_for_live_matches.setAdapter(new LiveMatchAdapter(LiveMatches.this,liveMatchArrayList,team_profile_pic_download_urls));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            progressDialog.dismiss();
                            Toast.makeText(LiveMatches.this,"Some Error Occurred",Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(LiveMatches.this,"Some Error Occurred",Toast.LENGTH_LONG).show();
            }
        });

    }
}


class LiveMatch{
    String TeamA,TeamB,TeamAGoals,TeamBGoals,StartTime,AgeGroup,UniqueID;
    LiveMatch(String UniqueID,String TeamA,String TeamB,String TeamAGoals,String TeamBGoals,String StartTime,String AgeGroup){
        this.TeamA = TeamA;
        this.TeamB = TeamB;
        this.TeamAGoals = TeamAGoals;
        this.TeamBGoals = TeamBGoals;
        this.StartTime = StartTime;
        this.AgeGroup = AgeGroup;
        this.UniqueID = UniqueID;
    }
}

class LiveMatchAdapter extends RecyclerView.Adapter<LiveMatchAdapter.Viewholder3>{

    ArrayList<LiveMatch> arrayListForLiveMatch;
    Map<String,String> urlMap;
    LayoutInflater layoutInflater;
    Context context;

    LiveMatchAdapter(Context context,ArrayList<LiveMatch> arrayListForLiveMatch,Map<String,String> urlMap){
        this.arrayListForLiveMatch = arrayListForLiveMatch;
        layoutInflater = LayoutInflater.from(context);
        this.urlMap = urlMap;
        this.context = context;
    }

    @Override
    public Viewholder3 onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.live_match_card,parent,false);
        Viewholder3 viewholder3 = new Viewholder3(view);
        return viewholder3;
    }

    @Override
    public void onBindViewHolder(Viewholder3 viewHolder, int position) {
        LiveMatch data = arrayListForLiveMatch.get(position);
        viewHolder.teamA_name.setText(data.TeamA);
        viewHolder.teamB_name.setText(data.TeamB);
        viewHolder.live_match_score_A.setText(data.TeamAGoals);
        viewHolder.live_match_score_B.setText(data.TeamBGoals);
        viewHolder.live_match_start_time.setText("Start Time : " + data.StartTime);
        String age_group_text;
        switch (data.AgeGroup){
            case "Group - A":
                age_group_text = "9yrs - 10yrs";
                break;
            case "Group - B":
                age_group_text = "10yrs - 11yrs";
                break;
            case "Group - C":
                age_group_text = "11yrs - 12yrs";
                break;
            case "Group - D":
                age_group_text = "12yrs - 13yrs";
                break;
            default:
                age_group_text = "";
                break;
        }
        viewHolder.display_age_group.setText("Age Group : "+age_group_text);
        final ImageView tempImageViewA = viewHolder.teamA_image;
        final ImageView tempImageViewB = viewHolder.teamB_image;
        final String urlA = urlMap.get(data.AgeGroup + data.TeamA);
        final String urlB = urlMap.get(data.AgeGroup + data.TeamB);
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
        return arrayListForLiveMatch.size();
    }

    class Viewholder3 extends RecyclerView.ViewHolder{
        TextView teamA_name,teamB_name,live_match_score_A,live_match_score_B,live_match_start_time,display_age_group;
        ImageView teamA_image,teamB_image;
        Viewholder3(View view){
            super(view);
            teamA_name = (TextView)view.findViewById(R.id.teamA_name);
            teamB_name = (TextView)view.findViewById(R.id.teamB_name);
            live_match_score_A = (TextView)view.findViewById(R.id.live_match_teama_goals);
            live_match_score_B = (TextView)view.findViewById(R.id.live_match_teamb_goals);
            live_match_start_time = (TextView)view.findViewById(R.id.live_match_start_time);
            teamA_image = (ImageView)view.findViewById(R.id.teamA_image);
            teamB_image = (ImageView)view.findViewById(R.id.teamB_image);
            display_age_group = (TextView)view.findViewById(R.id.live_match_age_group);
        }
    }


}