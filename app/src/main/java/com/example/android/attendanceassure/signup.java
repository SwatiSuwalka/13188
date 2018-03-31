package com.example.android.attendanceassure;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class signup extends AppCompatActivity

{

    private EditText email2;
    private EditText password2;
    private Button signupButton;
    private TextView login;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);

        email2=(EditText)findViewById(R.id.email2_id);
        password2=(EditText)findViewById(R.id.password2_id);
        signupButton=(Button)findViewById(R.id.signupButton_id);
        login =(TextView) findViewById(R.id.login_id);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(signup.this, welcomewindow.class);
                startActivity(i);

            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                /*if(validate())
                {*/
                    progressDialog.setMessage("Please wait while you are registered");
                    progressDialog.show();

                    String emails=email2.getText().toString().trim();
                    String passwords=password2.getText().toString().trim();


                    firebaseAuth.createUserWithEmailAndPassword(emails, passwords).addOnCompleteListener(signup.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(signup.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(signup.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                //}
            }
        });
    }

    /*private Boolean validate() {
        Boolean result =false;

        if (email2.getText().toString().isEmpty() || password2.getText().toString().isEmpty()) {
            Toast.makeText(signup.this, "Please fill all fields ", Toast.LENGTH_LONG).show();
        }
        else
        {
            result=true;
        }
        return result;
    }*/
}
