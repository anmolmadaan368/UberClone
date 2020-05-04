package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    @Override
    public void onClick(View v) {

        if(edtDorP.getText().toString().equals("Driver")|| edtDorP.getText().toString().equals("Passenger")){
            if(ParseUser.getCurrentUser()==null){
                ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if(user!=null && e==null){
                            Toast.makeText(MainActivity.this,"We have an Anonymous user",Toast.LENGTH_SHORT).show();

                            user.put("as",edtDorP.getText().toString());



                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                transtitionToPassengerActvity();
                                transitiontoDriverRequestListActivity();
                            }
                        });
                        }
                    }
                });
            }
        }

    }

    enum State{
        SIGNUP,LOGIN
    }
    private State state;
    private Button btnSignUpLogin, btnOneTimeLogin;
    private RadioButton rdbPassenger,rdbdriver;
    private EditText edtUsername,edtPassword,edtDorP;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseInstallation.getCurrentInstallation().saveInBackground();
        if(ParseUser.getCurrentUser()!=null){
            //ParseUser.logOut();
            transtitionToPassengerActvity();
            transitiontoDriverRequestListActivity();
        }

        rdbdriver=findViewById(R.id.rdbDriver);
        rdbPassenger=findViewById(R.id.rdbPassenger);
        btnOneTimeLogin=findViewById(R.id.btnOneTimeLogin);
        state= State.SIGNUP;
        btnSignUpLogin=findViewById(R.id.btnSignUpLogin);
        btnOneTimeLogin.setOnClickListener(this);

        edtUsername=findViewById(R.id.edtUsername);
        edtPassword=findViewById(R.id.edtPassword);
        edtDorP=findViewById(R.id.edtDorP);


        btnSignUpLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(state==State.SIGNUP){

                    if(rdbdriver.isChecked()==false && rdbPassenger.isChecked()==false){
                        Toast.makeText(MainActivity.this,"Are you a driver or a Passenger?",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ParseUser appUser =new ParseUser();
                    appUser.setUsername(edtUsername.getText().toString());
                    appUser.setPassword(edtPassword.getText().toString());

                    if(rdbdriver.isChecked()){
                        appUser.put("as","Driver");

                    }
                    else if(rdbPassenger.isChecked()){
                        appUser.put("as","Passenger");

                    }
                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null){
                                Toast.makeText(MainActivity.this,"Signed up Successfully",Toast.LENGTH_SHORT).show();
                                transtitionToPassengerActvity();
                                transitiontoDriverRequestListActivity();

                            }
                        }
                    });
                }
                else if(state==State.LOGIN){
                    ParseUser.logInInBackground(edtUsername.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if(e==null && user !=null){
                                Toast.makeText(MainActivity.this,"User Logged In",Toast.LENGTH_SHORT).show();

                                transtitionToPassengerActvity();
                                transitiontoDriverRequestListActivity();
                            }
                        }
                    });
                }
            }
        });


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.loginitem:

                if(state==State.SIGNUP){
                    state=State.LOGIN;
                    item.setTitle("Sign Up");
                    btnSignUpLogin.setText("Log In");
                }
                else if(state==State.LOGIN){
                    state=State.SIGNUP;
                    item.setTitle("Log In");
                    btnSignUpLogin.setText("Sign Up");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void transtitionToPassengerActvity(){

        if(ParseUser.getCurrentUser()!=null){
            if(ParseUser.getCurrentUser().get("as").equals("Passenger")){

                Intent intent=new Intent(MainActivity.this,PassengerActivity.class);
                startActivity(intent);
            }
        }
    }
    private  void transitiontoDriverRequestListActivity(){

        if(ParseUser.getCurrentUser()!=null){

            if(ParseUser.getCurrentUser().get("as").equals("Driver")){

                Intent intent=new Intent(this,DriverRequestListActivity.class);
                startActivity(intent);
            }
        }
    }
}
