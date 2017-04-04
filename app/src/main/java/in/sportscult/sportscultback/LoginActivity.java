package in.sportscult.sportscultback;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static EditText email,password;
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    SharedPreferences sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Email",user.getEmail());
                    editor.commit();
                    Intent intent = new Intent(LoginActivity.this,Interface.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    // User is signed in
                    //Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {

                    email.requestFocus();
                    // User is signed out
                    //Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void email_sign_in(View view){

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging You In...");
        progressDialog.show();

        String Email = email.getText().toString();
        String Password = password.getText().toString();

        boolean verification_status = true;
        //Verify Email ID
        if(Email.length()==0){
            email.setError("Email Too Short");
            email.requestFocus();
            verification_status = false;
        }
        else if(!Email.contains(".com") || !Email.contains("@") || Email.length()<6){
            email.requestFocus();
            email.setError("Invalid Email");
            verification_status = false;
        }
        //Verify Password
        if(Password.length()<6){
            password.setError("Password Too Short");
            password.requestFocus();
            verification_status = false;
        }

        if(!verification_status)
            return;

        if(!isNetworkAvailable()){
            Toast.makeText(LoginActivity.this,"Your pohone has lost internet connection.",Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }

        //Sign The User In
        mAuth.signInWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Sign In Failed",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                        else
                            progressDialog.dismiss();
                    }
                });
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
