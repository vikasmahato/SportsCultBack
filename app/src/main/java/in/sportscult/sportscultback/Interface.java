package in.sportscult.sportscultback;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Interface extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface);
    }

    public void generate_fixtures(View view){
        startActivity(new Intent(this,Add_Matches_To_Fixtures.class));
    }
    public void live_match(View view){
        startActivity(new Intent(this,Add_Live_Matches.class));
    }
}
