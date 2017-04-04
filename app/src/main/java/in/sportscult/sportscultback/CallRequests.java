package in.sportscult.sportscultback;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import static android.view.View.GONE;

public class CallRequests extends AppCompatActivity {

    private static TextView display_on_empty_call_requests;
    private static RecyclerView call_requests_recyclerview;
    private static ArrayList<CallRequest> callRequestArrayList;
    private static final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Call Requests");

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,Interface.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_requests);

        display_on_empty_call_requests = (TextView) findViewById(R.id.display_on_empty_call_requests);
        call_requests_recyclerview = (RecyclerView) findViewById(R.id.call_requests_recyclerview);

        callRequestArrayList = new ArrayList<CallRequest>();
        call_requests_recyclerview.setAdapter(new CallRequestAdapter(CallRequests.this, callRequestArrayList));
        call_requests_recyclerview.setLayoutManager(new LinearLayoutManager(this));

        Get_Call_Requests_From_Firebase();

        call_requests_recyclerview.addOnItemTouchListener(new RecyclerItemClickListener(this, call_requests_recyclerview, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + callRequestArrayList.get(position).contact));

                if (ActivityCompat.checkSelfPermission(CallRequests.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(CallRequests.this);
                alertDialog.setMessage("Do You want to Delete this request.\nThis would mean that you have handled the request");
                alertDialog.setPositiveButton("YES,Delete It", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        databaseReference.child(callRequestArrayList.get(position).Key).removeValue();
                        dialog.cancel();
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
            }
        }));
    }

    public void Get_Call_Requests_From_Firebase(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                callRequestArrayList = new ArrayList<CallRequest>();
                if(dataSnapshot.getChildren()==null){
                    EmptyArrayList();
                    return;
                }
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    Map<String,String> map = (Map<String,String>) childSnapshot.getValue();
                    callRequestArrayList.add(new CallRequest(childSnapshot.getKey(),map.get("Name"),map.get("Contact Number"),map.get("City")));
                }
                if(callRequestArrayList.size()==0)
                    EmptyArrayList();
                else{
                    call_requests_recyclerview.setAdapter(new CallRequestAdapter(CallRequests.this,callRequestArrayList));
                    ArrayListNotEmpty();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void EmptyArrayList(){
        display_on_empty_call_requests.setVisibility(View.VISIBLE);
        call_requests_recyclerview.setVisibility(GONE);
    }
    public void ArrayListNotEmpty(){
        display_on_empty_call_requests.setVisibility(GONE);
        call_requests_recyclerview.setVisibility(View.VISIBLE);
    }
}

class CallRequest{
    String Key,name,contact,city;
    CallRequest(String Key,String name,String contact,String city){
        this.Key = Key;
        this.name = name;
        this.contact = contact;
        this.city = city;
    }
}

class CallRequestAdapter extends RecyclerView.Adapter<CallRequestViewHolder>{

    Context context;
    ArrayList<CallRequest> callRequest;
    CallRequestAdapter(Context context,ArrayList<CallRequest> callRequest){
        this.context = context;
        this.callRequest = callRequest;
    }
    @Override
    public CallRequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.call_request_row,parent,false);
        return new CallRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CallRequestViewHolder holder, int position) {
        CallRequest temp = callRequest.get(position);
        holder.call_request_name.setText(temp.name);
        holder.call_request_city.setText(temp.city);
        holder.call_request_contact.setText(temp.contact);
    }

    @Override
    public int getItemCount() {
        return callRequest.size();
    }
}

class CallRequestViewHolder extends RecyclerView.ViewHolder{

    TextView call_request_name,call_request_contact,call_request_city;
    CallRequestViewHolder(View view){
        super(view);
        call_request_city = (TextView)view.findViewById(R.id.call_request_city);
        call_request_contact = (TextView)view.findViewById(R.id.call_request_contact);
        call_request_name = (TextView)view.findViewById(R.id.call_request_name);
    }
}