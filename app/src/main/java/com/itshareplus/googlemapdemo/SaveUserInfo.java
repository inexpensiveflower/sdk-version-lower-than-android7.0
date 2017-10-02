package com.itshareplus.googlemapdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SaveUserInfo extends AppCompatActivity implements View.OnClickListener{

    private EditText editTextAddress;
    private EditText editTextName;
    private EditText editTextEmail;
    private Button buttonSave;
    private Button buttonBack;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_user_info);

        editTextName = (EditText) findViewById(R.id.saveNewName);
        editTextAddress = (EditText) findViewById(R.id.saveNewAddress);
        editTextEmail = (EditText) findViewById(R.id.saveNewEmail);
        editTextEmail.setVisibility(View.GONE);
        buttonSave = (Button) findViewById(R.id.renewBtn);
        buttonBack = (Button) findViewById(R.id.backToPre);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("member");

        buttonSave.setOnClickListener(this);
        buttonBack.setOnClickListener(this);

        FirebaseUser user = mAuth.getCurrentUser();
        mDatabaseReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MemberInfo memberInfo = dataSnapshot.getValue(MemberInfo.class);
                Iterable child = dataSnapshot.getChildren();
                String userName = memberInfo.getName();
                String userAddress = memberInfo.getAddress();

                editTextName.setText(userName);
                editTextAddress.setText(userAddress);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public boolean onKeyDown(int keyCode , KeyEvent event){
        if((keyCode == KeyEvent.KEYCODE_BACK)){
            AlertDialog.Builder dialog = new AlertDialog.Builder(SaveUserInfo.this);
            dialog.setTitle("返回上一頁");
            dialog.setMessage("請透過按鈕 '返回' 回到上一頁");
            dialog.setPositiveButton("好", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });

            dialog.show();

        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public void onClick(View v) {

        if(v == buttonSave){
            saveInformation();

        }

        if(v == buttonBack){
            startActivity(new Intent(this, MapsActivity.class));
        }

    }

    private void saveInformation() {
        String name = editTextName.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this , "請輸入您的姓名", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(address)){
            Toast.makeText(this , "請輸入您的地址", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        mDatabaseReference.child(user.getUid()).child("name").setValue(name);
        mDatabaseReference.child(user.getUid()).child("address").setValue(address);

        Toast.makeText(this, "資料更新成功",Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MapsActivity.class));
    }

}
