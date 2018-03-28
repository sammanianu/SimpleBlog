package com.example.sanu.simpleblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText mLoginEmailField, mLoginPasswordField;
    private Button mLoginButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;

    private ProgressDialog mPogress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        mPogress = new ProgressDialog(this);

        mLoginEmailField = (EditText)findViewById(R.id.loginEmailField);
        mLoginPasswordField = (EditText)findViewById(R.id.loginPasswordField);
        mLoginButton = (Button)findViewById(R.id.loginButton);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkLogin();
            }
        });

    }

    private void checkLogin() {

        String email = mLoginEmailField.getText().toString().trim();
        String passwod = mLoginPasswordField.getText().toString().trim();

        if(!TextUtils.isEmpty(email) && (!TextUtils.isEmpty(passwod))){

            mPogress.setMessage("Checking Login...");
            mPogress.show();

            mAuth.signInWithEmailAndPassword(email,passwod).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){

                        mPogress.dismiss();

                        checkUserExist();

                    }else{

                        Toast.makeText(LoginActivity.this,"Error Login", Toast.LENGTH_LONG).show();

                        mPogress.dismiss();
                    }

                }
            });
        }
    }

    private void checkUserExist() {

        final String user_id = mAuth.getCurrentUser().getUid();

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(user_id)){

                    Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);

                }else{

                    Intent setupIntent = new Intent(LoginActivity.this,SetupActivity.class);
                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(setupIntent);                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
