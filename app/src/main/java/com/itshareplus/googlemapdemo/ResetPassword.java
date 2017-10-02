package com.itshareplus.googlemapdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class ResetPassword extends AppCompatActivity implements View.OnClickListener{

    private TextView firstStep;
    private EditText resetPasswordEmail;
    private Button resetPasswordBtn;
    private Button cancel;

    private EditText userLoginEmail;
    private EditText userLoginPassword;
    private Button userLoginBtn;

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        firstStep = (TextView) findViewById(R.id.firstStep);
        resetPasswordEmail = (EditText) findViewById(R.id.resetPasswordEmail);
        resetPasswordBtn = (Button) findViewById(R.id.resetPasswordBtn);
        cancel = (Button) findViewById(R.id.cancel);

        userLoginEmail = (EditText) findViewById(R.id.userLoginEmail);
        userLoginPassword = (EditText) findViewById(R.id.userLoginPassword);
        userLoginBtn = (Button) findViewById(R.id.userLoginBtn);

        userLoginEmail.setVisibility(View.GONE);
        userLoginPassword.setVisibility(View.GONE);
        userLoginBtn.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        resetPasswordEmail.setText(getIntent().getExtras().getString("userEmail"));

        resetPasswordBtn.setOnClickListener(this);
        cancel.setOnClickListener(this);
        userLoginBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if(view == resetPasswordBtn){
            sendResetEmail();
        }

        if(view == cancel){
            startActivity(new Intent(ResetPassword.this , LoginPage.class));
        }

        if(view == userLoginBtn){
            userLogin();
        }

    }

    private void userLogin() {

        final String email = userLoginEmail.getText().toString().trim();
        String password = userLoginPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "請輸入您的信箱", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this , "請輸入您的密碼", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("登入中...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            startActivity(new Intent(ResetPassword.this, MapsActivity.class));
                        } else {
                            Toast.makeText(ResetPassword.this, "您的信箱或是密碼有誤，請再試一次!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendResetEmail() {
        final String email = resetPasswordEmail.getText().toString().trim();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "請輸入您的信箱!", Toast.LENGTH_SHORT).show();
            //Stopping the function
            return;
        }
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(ResetPassword.this, "請至信箱重設密碼",Toast.LENGTH_LONG).show();
                            firstStep.setText("請透過新密碼登入");

                            resetPasswordEmail.setVisibility(View.GONE);
                            resetPasswordBtn.setVisibility(View.GONE);
                            cancel.setVisibility(View.GONE);

                            userLoginEmail.setText(email);
                            userLoginEmail.setVisibility(View.VISIBLE);
                            userLoginPassword.setVisibility(View.VISIBLE);
                            userLoginBtn.setVisibility(View.VISIBLE);

                        }
                    }
                });

    }
}
