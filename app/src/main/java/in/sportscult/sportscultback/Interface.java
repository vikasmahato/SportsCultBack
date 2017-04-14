package in.sportscult.sportscultback;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class Interface extends AppCompatActivity {

    private static TextView logged_in_mail;
    private static SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface);

        logged_in_mail = (TextView)findViewById(R.id.logged_in_mail);
        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        logged_in_mail.setText("You are Logged in as " + sharedPreferences.getString("Email","Nobody"));
    }

    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void generate_fixtures(View view){
        startActivity(new Intent(this,Add_Matches_To_Fixtures.class));
    }
    public void live_match(View view){
        startActivity(new Intent(this,Add_Live_Matches.class));
    }

    public void register(View view){
        startActivity(new Intent(this,RegistrationActivity.class));
    }
    public void call_requests(View view){
        startActivity(new Intent(this,CallRequests.class));
    }

    public void log_the_user_out(View view){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Interface.this,LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
        sharedPreferences.edit().remove("Email").commit();
        startActivity(intent);
    }

    public static String formatDate(int day,int month,int year){
        String FormattedDate = "";
        if(day<10)
            FormattedDate+=("0"+day);
        else
            FormattedDate+=day;
        FormattedDate+="-";
        if(month<10)
            FormattedDate+=("0"+month);
        else
            FormattedDate+=month;
        FormattedDate+="-";
        if(year<10)
            FormattedDate+=("0"+year);
        else
            FormattedDate+=year;
        return FormattedDate;
    }
}
