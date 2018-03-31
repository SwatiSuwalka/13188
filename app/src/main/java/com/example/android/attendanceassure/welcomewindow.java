package com.example.android.attendanceassure;

        import android.app.ProgressDialog;
        import android.content.Intent;
        import android.support.annotation.NonNull;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.auth.AuthResult;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;

public class welcomewindow extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button loginButton;
    private TextView signup;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcomewindow);

        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);

        email=(EditText)findViewById(R.id.email_id);
        password=(EditText)findViewById(R.id.password_id);
        loginButton=(Button)findViewById(R.id.loginButton_id);
        signup = (TextView) findViewById(R.id.signup_id);

        FirebaseUser user=firebaseAuth.getCurrentUser();

        /*if(user!=null)
        {
            finish();
            startActivity(new Intent(welcomewindow.this,userprofile.class));
        }*/

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate())
                {
                    progressDialog.setMessage("Please wait");
                    progressDialog.show();

                    firebaseAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                progressDialog.dismiss();
                                if (email.equals("sid.facecut@gmail.com"))
                                {
                                    Toast.makeText(welcomewindow.this, "Admin login successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(welcomewindow.this, adminActivity.class));

                                }
                                else
                                {
                                    Toast.makeText(welcomewindow.this,"User login successful",Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(welcomewindow.this,userprofile.class));
                                }
                            }
                            else
                            {
                                progressDialog.dismiss();
                                Toast.makeText(welcomewindow.this, "Login failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        // taking id of SIGNUP page....for INTENT.

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(welcomewindow.this, signup.class);
                startActivity(intent);

            }
        });
    }

    private Boolean validate() {
        Boolean result =false;

        if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
            Toast.makeText(welcomewindow.this, "Please fill all fields ", Toast.LENGTH_LONG).show();
        }
        else
        {
            result=true;
        }
        return result;
    }
}

