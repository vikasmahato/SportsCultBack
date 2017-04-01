package in.sportscult.sportscultback;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText reg_team_name,reg_coach_name,reg_coach_contact,reg_coach_email,reg_password,reg_confirm_password;
    private Spinner age_group_dropdown;
    private String team_name,coach_name,coach_contact,coach_email,age_group,location,password,confirm_password;
    private static RecyclerView player_list;
    private static Uri id_proof_scan_uri,profile_pic_uri;
    private static ArrayList<String> names_of_players,contact_of_players,jersey_number_of_players;
    private static ArrayList<Uri> uri_of_players;
    private static View AlertDialogView;
    private static Player_List_Adapter player_list_adapter;
    private static ProgressDialog progressDialog;
    //Provide the minimum number of permissable team members in a team
    private static final int minimum_number_of_players = 2;
    private static final int GAlLERY_ACCESS = 1000;
    private static final int PROFILE_PIC_ACCESS = 1001;
    private static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 1002;
    private static final String[] age_group_codes = {"0","A","B","C","D"};

    private static DatabaseReference databaseReference;
    private static DatabaseReference databaseReference1;
    private static StorageReference storageReference;

    //Add Team Profile Pic, Group Jersey,Password Field
    //Dropdown Menu for Location, Age Group
    //Under the player description, add jersey number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        reg_team_name = (EditText)findViewById(R.id.reg_team_name);
        reg_coach_name = (EditText)findViewById(R.id.reg_coach_name);
        reg_coach_email = (EditText)findViewById(R.id.reg_coach_email);
        reg_coach_contact = (EditText)findViewById(R.id.reg_coach_contact);
        reg_password = (EditText)findViewById(R.id.reg_password);
        reg_confirm_password = (EditText)findViewById(R.id.reg_confirm_password);
        player_list = (RecyclerView) findViewById(R.id.player_list);
        player_list.setNestedScrollingEnabled(false);
        age_group_dropdown = (Spinner)findViewById(R.id.age_group_dropdown);
        names_of_players = new ArrayList<String>();
        contact_of_players = new ArrayList<String>();
        jersey_number_of_players = new ArrayList<String>();
        uri_of_players = new ArrayList<Uri>();
        profile_pic_uri = null;
        age_group = "Group - 0";

        //Set Up Spinner For Age Group
        ArrayAdapter<String> ageGroupAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.age_groups));
        ageGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        age_group_dropdown.setAdapter(ageGroupAdapter);
        age_group_dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                age_group = "Group - "+age_group_codes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        player_list.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        //Comment this section out after adding additional functionality for them
        //age_group = "Group - A";
        location = "Rohini";
        //Till here

        setuplistview();
    }

    //ListView SetUp
    public void setuplistview(){

        player_list_adapter = new Player_List_Adapter(this,names_of_players,contact_of_players,jersey_number_of_players,uri_of_players);
        player_list.setAdapter(player_list_adapter);
        player_list.setLayoutManager(new LinearLayoutManager(this));

    }

    //Add a new player
    public void add_new_player(View view){

        id_proof_scan_uri = null;
        AlertDialogView = LayoutInflater.from(RegistrationActivity.this).inflate(R.layout.add_new_player,null);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegistrationActivity.this, R.style.MyAlertDialogStyle);
        alertDialog.setView(AlertDialogView);
        alertDialog.setTitle("Enter The Player Details");
        alertDialog.setCancelable(false);
        final EditText player_name = (EditText)AlertDialogView.findViewById(R.id.player_name);
        final EditText player_contact = (EditText)AlertDialogView.findViewById(R.id.player_contact);
        final EditText player_jersey = (EditText)AlertDialogView.findViewById(R.id.player_jersey);
        final Button upload_id_proof_scan = (Button)AlertDialogView.findViewById(R.id.upload_id_proof_scan);

        upload_id_proof_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserID();
            }
        });

        alertDialog.setPositiveButton("Add Player", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String playername = properly_format_input(player_name.getText().toString());
                String playercontact = player_contact.getText().toString();
                String playerjersey = player_jersey.getText().toString();

                //Image uri is already present in the variable id_proof_scan_uri

                if(player_contact.length()<6 || player_name.length()==0 || player_jersey.length()<1 || id_proof_scan_uri==null){
                }
                else{

                    //Now add all the data to the list view data set
                    names_of_players.add(playername);
                    contact_of_players.add(playercontact);
                    jersey_number_of_players.add(playerjersey);
                    uri_of_players.add(id_proof_scan_uri);
                    setuplistview();
                    //player_list_adapter.notifyDataSetChanged();
                }

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


    public void startActionForProfilePic(){
        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent,PROFILE_PIC_ACCESS);
    }

    //Choosing Profile Pic
    public void add_team_profile_pic(View view){

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            startActionForProfilePic();
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startActionForProfilePic();
            } else {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "External Storage and Camera permission is required to read images from device", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                android.Manifest.permission.CAMERA},
                        ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
            }

        }


    }

    public void getUserID(){
        startActionForUserID();
    }
    public void startActionForUserID(){
        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent,GAlLERY_ACCESS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GAlLERY_ACCESS && resultCode==RESULT_OK){
            ImageView id_proof_scan = (ImageView) AlertDialogView.findViewById(R.id.id_proof_scan);
            id_proof_scan_uri = data.getData();
            id_proof_scan.setVisibility(View.VISIBLE);
            //id_proof_scan.setImageURI(id_proof_scan_uri);
            Picasso.with(this).load(id_proof_scan_uri).resize(300,300).centerCrop().into(id_proof_scan);
        }
        else if(requestCode==PROFILE_PIC_ACCESS && resultCode==RESULT_OK){
            ImageView reg_team_profile_pic = (ImageView)findViewById(R.id.reg_team_profile_pic);
            profile_pic_uri = data.getData();
            reg_team_profile_pic.setBackgroundColor(Color.BLACK);
            //reg_team_profile_pic.setImageURI(profile_pic_uri);
            Picasso.with(this).load(profile_pic_uri).resize(150,150).centerCrop().into(reg_team_profile_pic);
        }
    }

    //Register Your Team
    public void register_team(View view){

        progressDialog = new ProgressDialog(RegistrationActivity.this);
        progressDialog.setMessage("Registering Your Team...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        //Get the contents from fields
        team_name = properly_format_input(reg_team_name.getText().toString());
        coach_name = properly_format_input(reg_coach_name.getText().toString());
        coach_contact = reg_coach_contact.getText().toString().trim();
        coach_email = reg_coach_email.getText().toString().trim();
        password = reg_password.getText().toString().trim();
        confirm_password = reg_confirm_password.getText().toString().trim();

        //Verify if the details were entered correctly
        boolean correct = verify_details();

        if(!correct) {
            progressDialog.dismiss();
            return;
        }

        //Verify if a team name with the same name in that age group already exists
        DatabaseReference tempDatabaseReference = FirebaseDatabase.getInstance().getReference().child(age_group).child("Team Names");
        tempDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,String> map = (Map<String,String>)dataSnapshot.getValue();
                if(map!=null && map.get(team_name)!=null){
                    reg_team_name.setError("Team Name Already Exists");
                    reg_team_name.requestFocus();
                    progressDialog.dismiss();
                    return;
                }
                else{
                    //Add Team To Leaderboard
                    DatabaseReference LeaderboardReference = FirebaseDatabase.getInstance().getReference().child(age_group).child("Leaderboard").child(team_name);
                    Map<String,String> leaderboardData = new HashMap<String, String>();
                    leaderboardData.put("Team Name",team_name);
                    leaderboardData.put("Matches Played","0");
                    leaderboardData.put("Matches Won","0");
                    leaderboardData.put("Matches Drawn","0");
                    leaderboardData.put("Matches Lost","0");
                    leaderboardData.put("Goals Scored","0");
                    leaderboardData.put("Goals Conceived","0");
                    leaderboardData.put("Red Cards","0");
                    leaderboardData.put("Points","0");
                    LeaderboardReference.setValue(leaderboardData);

                    //Add Team Name along with password
                    databaseReference1 = FirebaseDatabase.getInstance().getReference().child(age_group).child("Team Names").child(team_name);
                    databaseReference1.child("Password").setValue(password);

                    //Add Team Details
                    storageReference = FirebaseStorage.getInstance().getReference().child(age_group).child(team_name);
                    //Add Team Profile Pic
                    if(profile_pic_uri!=null) {

                        //Generate thumbnail and save it and its download url
                        byte[] thumbnailForProfilePic = generateThumbnailForImage(profile_pic_uri);
                        storageReference.child("Team Profile Pic Thumbnail").putBytes(thumbnailForProfilePic).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUri = taskSnapshot.getDownloadUrl();
                                databaseReference1.child("Team Profile Pic Thumbnail Url").setValue(downloadUri.toString());
                            }
                        });

                        //Save image and its download url
                        storageReference.child("Team Profile Pic").putFile(profile_pic_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUri = taskSnapshot.getDownloadUrl();
                                databaseReference1.child("Team Profile Pic Url").setValue(downloadUri.toString());
                            }
                        });
                    }
                    databaseReference = FirebaseDatabase.getInstance().getReference().child(age_group).child("Team Description").child(team_name);
                    databaseReference.child("Coach Name").setValue(coach_name);
                    databaseReference.child("Contact Number").setValue(coach_contact);
                    databaseReference.child("Email").setValue(coach_email);
                    databaseReference.child("Location").setValue(location);

                    //Adding information about players
                    storageReference = storageReference.child("Player ID Scans");
                    databaseReference.child("Number Of Players").setValue(names_of_players.size());
                    databaseReference = databaseReference.child("Players");
                    for(int i=0;i<names_of_players.size();i++){
                        //Generate a proper player code so that players can be distinguished
                        String player_code = jersey_number_of_players.get(i)+"-"+ names_of_players.get(i);
                        final DatabaseReference tempreference = databaseReference.child(player_code);
                        Map<String,String> playerUploadData = new HashMap<String, String>();
                        playerUploadData.put("Name",names_of_players.get(i));
                        playerUploadData.put("Contact",contact_of_players.get(i));
                        playerUploadData.put("Jersey Number",jersey_number_of_players.get(i));
                        tempreference.setValue(playerUploadData);
                        storageReference.child(player_code).putFile(uri_of_players.get(i)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUri = taskSnapshot.getDownloadUrl();
                                tempreference.child("Player ID Scans Url").setValue(downloadUri.toString());
                                progressDialog.dismiss();
                            }
                        });
                    }

                    Intent intent = new Intent(RegistrationActivity.this,Interface.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    boolean verify_details(){

        View focus = null;
        boolean verification_success = true;
        if(password.length()<6){
            reg_confirm_password.setText("");
            reg_password.setError("Password Must Atleast 6 characters");
            focus = reg_password;
            verification_success = false;
        }
        else if(!password.equals(confirm_password)){
            reg_confirm_password.setError("Passwords Don't Match");
            focus = reg_confirm_password;
            verification_success = false;
        }
        if(coach_email.length()==0){
            reg_coach_email.setError(getString(R.string.empty_field));
            focus = reg_coach_email;
            verification_success = false;
        }
        else if(!(coach_email.contains("@") && coach_email.contains(".com"))){
            reg_coach_email.setError("Invalid Email");
            focus = reg_coach_email;
            verification_success = false;
        }
        if(coach_contact.length()==0){
            reg_coach_contact.setError(getString(R.string.empty_field));
            focus = reg_coach_contact;
            verification_success = false;
        }
        else if(coach_contact.length()<6){
            reg_coach_contact.setError("Invalid Contact Number");
            focus = reg_coach_contact;
            verification_success = false;
        }
        if(coach_name.length()==0) {
            reg_coach_name.setError(getString(R.string.empty_field));
            focus = reg_coach_name;
            verification_success = false;
        }
        if(age_group.equals("Group - 0")){
            Toast.makeText(RegistrationActivity.this,"Please Select An Age Group For Team",Toast.LENGTH_LONG).show();
            verification_success = false;
            focus = age_group_dropdown;
        }
        if(team_name.length()==0){
            reg_team_name.setError(getString(R.string.empty_field));
            focus = reg_team_name;
            verification_success = false;
        }
        if(verification_success && names_of_players.size()<minimum_number_of_players){
            Toast.makeText(RegistrationActivity.this,("You need to add atleast "+(minimum_number_of_players-names_of_players.size())+" more players."),Toast.LENGTH_LONG).show();
            verification_success = false;
            if(focus==null)
                focus = reg_team_name;
        }

        if(!verification_success)
            focus.requestFocus();
        return verification_success;
    }



    //Till here Register Your Team


    //Helper Methods

    public String properly_format_input(String s){
        String array[] = s.split(" ");
        StringBuilder stringBuilder = new StringBuilder("");
        for(String a:array) {
            stringBuilder.append(a.substring(0, 1).toUpperCase());
            stringBuilder.append(a.substring(1).toLowerCase()).append(" ");
        }
        return stringBuilder.toString().trim();
    }

    public byte[] generateThumbnailForImage(Uri uri){
        final int THUMBSIZE = 100;

        Bitmap thumbImage = ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(getPath(uri)),
                THUMBSIZE,
                THUMBSIZE);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumbImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    //Adapter for displaying player details in listview
    class Player_List_Adapter extends RecyclerView.Adapter<Player_List_Adapter.ViewHolder0>{

        ArrayList<String> names,contacts,jersey;
        ArrayList<Uri> uris;
        Context context;

        public Player_List_Adapter(Context context,ArrayList<String> names,ArrayList<String> contacts,ArrayList<String> jersey,ArrayList<Uri> uris) {
            this.context = context;
            this.names = names;
            this.contacts = contacts;
            this.uris = uris;
            this.jersey = jersey;
        }

        @Override
        public ViewHolder0 onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.player_row,parent,false);
            ViewHolder0 viewHolder0 = new ViewHolder0(view);
            return viewHolder0;
        }

        @Override
        public void onBindViewHolder(ViewHolder0 viewHolder, int position) {
            viewHolder.individual_name.setText(names.get(position));
            viewHolder.individual_contact.setText(contacts.get(position));
            viewHolder.individual_jersey.setText(jersey.get(position));
            //viewHolder.individual_id_proof.setImageURI(uris.get(position));
            Picasso.with(RegistrationActivity.this).load(uris.get(position)).resize(300,300).centerCrop().into(viewHolder.individual_id_proof);
        }

        @Override
        public int getItemCount() {
            return names.size();
        }

        public class ViewHolder0 extends RecyclerView.ViewHolder{
            TextView individual_name,individual_contact,individual_jersey;
            ImageView individual_id_proof;
            ViewHolder0(View v){
                super(v);
                individual_name = (TextView)v.findViewById(R.id.individual_name);
                individual_contact = (TextView)v.findViewById(R.id.individual_contact);
                individual_jersey = (TextView)v.findViewById(R.id.player_jersey);
                individual_id_proof = (ImageView)v.findViewById(R.id.individual_id_proof);
            }
        }


    }


}
