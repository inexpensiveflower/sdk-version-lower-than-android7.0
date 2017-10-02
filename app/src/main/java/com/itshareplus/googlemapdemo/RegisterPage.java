package com.itshareplus.googlemapdemo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RegisterPage extends AppCompatActivity implements View.OnClickListener {

    private TextView titleTextView;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonNextStep;
    private Button buttonBack;

    private EditText typeName;
    private EditText typeAddress;
    private Button buttonRegiser;

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRefMember;


    final List<String> memberData = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        databaseRefMember = FirebaseDatabase.getInstance().getReference("member");

        titleTextView = (TextView) findViewById(R.id.titleTextView);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonNextStep = (Button) findViewById(R.id.buttonRegister);
        buttonBack = (Button) findViewById(R.id.back);

        typeName = (EditText) findViewById(R.id.saveName);
        typeName.setVisibility(View.GONE);
        typeAddress = (EditText) findViewById(R.id.saveAddress);
        typeAddress.setVisibility(View.GONE);
        buttonRegiser = (Button) findViewById(R.id.register);
        buttonRegiser.setVisibility(View.GONE);

        buttonNextStep.setOnClickListener(this);
        buttonRegiser.setOnClickListener(this);
        buttonBack.setOnClickListener(this);


    }

    private void registerUser() {
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        Integer countPassword = password.length();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "請輸入信箱", Toast.LENGTH_SHORT).show();
            //Stopping the function
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this , "請輸入密碼", Toast.LENGTH_SHORT).show();
            return;
        }

        if(countPassword < 6){
            Toast.makeText(this, "密碼不能少於6個字", Toast.LENGTH_SHORT).show();
            return;
        }
        //if validation is ok
        //we will first show a progressDialog
        progressDialog.setMessage("請稍後...");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {

                            editTextEmail.setVisibility(View.GONE);
                            editTextPassword.setVisibility(View.GONE);
                            buttonNextStep.setVisibility(View.GONE);
                            buttonBack.setVisibility(View.GONE);
                            userlogin();

                        }else{

                            databaseRefMember.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for (DataSnapshot memberSnapShot : dataSnapshot.getChildren()) {

                                        MemberInfo memberinfo = memberSnapShot.getValue(MemberInfo.class);
                                        String userEmail = memberinfo.getEmail();
                                        memberData.add(userEmail);
                                        Log.d("TEST", String.valueOf(memberData));

                                    }
                                    //檢查信箱是否被註冊過了
                                    if (memberData.contains(email)) {
                                        AlertDialog.Builder warmningDialog = new AlertDialog.Builder(RegisterPage.this);
                                        warmningDialog.setTitle("注意");
                                        warmningDialog.setMessage("該信箱已經被註冊過了。" + '\n'+
                                                "or 該信箱已連結至google，請透過google登入應用程式");
                                        warmningDialog.setPositiveButton("好", new DialogInterface.OnClickListener() {
                                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });

                                        warmningDialog.show();
                                    } else{
                                        AlertDialog.Builder warmningDialog = new AlertDialog.Builder(RegisterPage.this);
                                        warmningDialog.setTitle("注意");
                                        warmningDialog.setMessage("你的信箱格式錯誤!請再試一次");
                                        warmningDialog.setPositiveButton("好", new DialogInterface.OnClickListener() {
                                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {}
                                        });
                                        warmningDialog.show();

                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            });


                        }
                        progressDialog.dismiss();
                    }
                });
    }

    private void userlogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "請輸入您的信箱", Toast.LENGTH_SHORT).show();
            //Stopping the function
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this , "請輸入您的密碼", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            typeName.setVisibility(View.VISIBLE);
                            typeAddress.setVisibility(View.VISIBLE);
                            buttonRegiser.setVisibility(View.VISIBLE);
                            titleTextView.setText("請輸入您的基本資料");

                        }else{
                            Toast.makeText(RegisterPage.this, "您的信箱/密碼是錯誤的.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveInformation() {
        String name = typeName.getText().toString().trim();
        String address = typeAddress.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this , "請輸入您的姓名", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(address)){
            Toast.makeText(this , "請輸入您的地址", Toast.LENGTH_SHORT).show();
            return;
        }
        // 下面三行把會員資訊存進資料庫
        FirebaseUser user = mAuth.getCurrentUser();
        MemberInfo memberInfo = new MemberInfo(name , address , email );
        databaseRefMember.child(user.getUid()).setValue(memberInfo);

        progressDialog.setMessage("註冊中...");
        progressDialog.show();
        Toast.makeText(this, "註冊成功",Toast.LENGTH_LONG).show();
        startActivity(new Intent(RegisterPage.this , MapsActivity.class));
    }

    @Override
    public boolean onKeyDown(int keyCode , KeyEvent event){
        if((keyCode == KeyEvent.KEYCODE_BACK)){
            AlertDialog.Builder dialog = new AlertDialog.Builder(RegisterPage.this);
            dialog.setTitle("返回上一頁");
            dialog.setMessage("請透過按鈕 '返回' 回到上一頁");
            dialog.setPositiveButton("好", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            dialog.show();

        }
        return super.onKeyDown(keyCode,event);
    }


    @Override
    public void onClick(View view) {

        if(view == buttonNextStep){
            registerUser();
        }


        if(view == buttonBack){
            startActivity(new Intent(RegisterPage.this, MapsActivity.class));
        }

        if(view == buttonRegiser){
            saveInformation();
        }

    }



}
