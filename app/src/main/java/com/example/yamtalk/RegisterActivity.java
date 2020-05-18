package com.example.yamtalk;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    double latitude;
    double longitude;

    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = true;

    private GpsInfo gps;

    TextInputLayout textInputLayout_id;
    TextInputLayout textInputLayout_email;
    TextInputLayout textInputLayout_password;
    TextInputLayout textInputLayout_password2;
    Button button_join;
    private Toolbar toolbar;                //툴바 역할을 해줄 툴바 객체
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        textInputLayout_id = (TextInputLayout)findViewById(R.id.textInputLayout_id);
        textInputLayout_email = (TextInputLayout)findViewById(R.id.textInputLayout_email);
        textInputLayout_password = (TextInputLayout)findViewById(R.id.textInputLayout_password);
        textInputLayout_password2 = (TextInputLayout)findViewById(R.id.textInputLayout_password2);
        button_join = (Button)findViewById(R.id.button_join);

        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        toolbar = (Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Join");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        button_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string_id = textInputLayout_id.getEditText().getText().toString();
                String string_email = textInputLayout_email.getEditText().getText().toString();
                String string_password = textInputLayout_password.getEditText().getText().toString();
                String string_password2 = textInputLayout_password2.getEditText().getText().toString();

                if(string_id.equals("")) {
                    Toast.makeText(getApplicationContext(), "아이디를 입력해주세요", Toast.LENGTH_SHORT).show();
                } else {
                    if(string_email.equals("")) {
                        Toast.makeText(getApplicationContext(), "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                    } else {
                        if (string_password.equals("")) {
                            Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                        } else {
                            if(string_password2.equals("")) {
                                Toast.makeText(getApplicationContext(), "비밀번호 확인을 입력해주세요", Toast.LENGTH_SHORT).show();
                            } else {
                                if(!string_password.equals(string_password2)) {
                                    Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                                } else {
//                                    Toast.makeText(getApplicationContext(), "회원가입에 성공했습니다", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "onClick: " + "register_user 메소드 호출");

                                    progressDialog.setTitle("Please wait");
                                    progressDialog.setCanceledOnTouchOutside(false);
                                    progressDialog.show();

                                    register_user(string_id, string_email, string_password);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public void get_gps() {
        if (!isPermission) {
            callPermission();
            return;
        }

        gps = new GpsInfo(RegisterActivity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }

    // 전화번호 권한 요청
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }
    private void register_user(final String id, final String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();
                    database = FirebaseDatabase.getInstance().getReference().child("profile").child(uid);

                    ArrayList<String> arrayList_friends = new ArrayList<>();
                    arrayList_friends.add(uid);

                    Type listType = new TypeToken<ArrayList<String>>() {
                    }.getType();
                    gson = new GsonBuilder().create();
                    String string_friends = gson.toJson(arrayList_friends, listType);

                    get_gps();

                    HashMap hashMap_add_user = new HashMap();
                    hashMap_add_user.put("id", id);
                    hashMap_add_user.put("email", email);
                    hashMap_add_user.put("friends", string_friends);
                    hashMap_add_user.put("message", "");
                    hashMap_add_user.put("profile_image", "");
                    hashMap_add_user.put("background_image", "");
                    hashMap_add_user.put("latitude", latitude);
                    hashMap_add_user.put("longitude", longitude);
                    hashMap_add_user.put("permission", true);
                    database.setValue(hashMap_add_user);

                    Intent intent = new Intent(RegisterActivity.this, StartActivity.class);
                    intent.putExtra("email", textInputLayout_email.getEditText().getText().toString());
                    startActivity(intent);
                    finish();
                    Toast.makeText(getApplicationContext(), "회원 가입에 성공했습니다", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "회원 가입에 실패했습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
