package com.example.yamtalk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {

    private static final String TAG = "StartActivity";
    TextView tv_yam;
    TextView tv_talk;
    TextView tv_range;
    EditText editText_email;
    EditText editText_password;
    Button button_register;
    Button button_login;
    CheckBox mCheckBox;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        editText_email = (EditText)findViewById(R.id.editLogin);
        editText_password = (EditText)findViewById(R.id.editPassward);
        button_register = (Button)findViewById(R.id.button_register);
        button_login = (Button)findViewById(R.id.button_login);
        mCheckBox = findViewById(R.id.checkBox);
        tv_yam = (TextView)findViewById(R.id.tv_yam);
        tv_talk = (TextView)findViewById(R.id.tv_talk);
        tv_range = (TextView)findViewById(R.id.tv_range);

        TranslateAnimation translateAnimation_yam = new TranslateAnimation(100, 0, 0, 0); //뷰를 이동시킬 애니메이션 객체
        translateAnimation_yam.setDuration(1500); //2초 동안 동작

        TranslateAnimation translateAnimation_talk = new TranslateAnimation(100, 0, 0, 0); //뷰를 이동시킬 애니메이션 객체
        translateAnimation_talk.setDuration(1500); //2초 동안 동작

        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1); //뷰를 투명한 상태에서 불투명 상태로 만들어줄 객체
        alphaAnimation.setDuration(1500); //2초 동안 동작

        AlphaAnimation alphaAnimation1 = new AlphaAnimation(0, 1); //뷰를 투명한 상태에서 불투명 상태로 만들어줄 객체
        alphaAnimation1.setDuration(3000); //2초 동안 동작

        AnimationSet animationSet_yam = new AnimationSet(true); //뷰를 이동시키는 애니메이션과 투명도를 조정해주는 애니메이션을 한 세트로 묶어줄 애니메이션 객체
        animationSet_yam.addAnimation(translateAnimation_yam); //뷰를 이동시킬 애니메이션 객체 추가
        animationSet_yam.addAnimation(alphaAnimation); //뷰의 투명도를 조정해줄 애니메이션 객체 추가

        AnimationSet animationSet_talk = new AnimationSet(true); //뷰를 이동시키는 애니메이션과 투명도를 조정해주는 애니메이션을 한 세트로 묶어줄 애니메이션 객체
        animationSet_talk.addAnimation(translateAnimation_talk); //뷰를 이동시킬 애니메이션 객체 추가
        animationSet_talk.addAnimation(alphaAnimation); //뷰의 투명도를 조정해줄 애니메이션 객체 추가

        tv_yam.startAnimation(animationSet_yam); //'YAM' 텍스트뷰에 애니메이션 효과 주기
        tv_talk.startAnimation(animationSet_talk); //'Talk' 텍스트뷰에 애니메이션 효과 주기
        tv_range.startAnimation(alphaAnimation1);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();
        mEditor.commit();

        checkSharedPreferences();

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editText_email.getText().toString();
                String password = editText_password.getText().toString();
                if(email.equals("")) {
                    Toast.makeText(getApplicationContext(), "아이디를 입력해주세요", Toast.LENGTH_SHORT).show();
                } else if(password.equals("")) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                } else {

                    progressDialog.setMessage("Please wait");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    login_user(email, password);
                }
            }
        });

        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void onResume() {
        super.onResume();
//        Log.e(TAG, "onResume: start");

        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
//        Log.e(TAG, "onResume: email : " + email);
        if(email != null) {
            editText_email.setText(email);
            editText_password.setText("");
        }
    }

//    protected void onRestart() {
//        super.onRestart();
//        Log.e(TAG, "onRestart: start");
//
//        Intent intent = getIntent();
//        String email = intent.getStringExtra("email");
//        Log.e(TAG, "onRestart: email : " + email);
//        if(email != null) {
//            editText_email.setText(email);
//        }
//    }

    private void login_user(final String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (mCheckBox.isChecked()) {

                        mEditor.putString(getString(R.string.checkbox), "True");
                        mEditor.commit();

                        String name = editText_email.getText().toString();
                        mEditor.putString(getString(R.string.name), name);
                        mEditor.commit();

                        String passWord = editText_password.getText().toString();
                        mEditor.putString(getString(R.string.password), passWord);
                        mEditor.commit();

                    } else {

                        mEditor.putString(getString(R.string.checkbox), "False");
                        mEditor.commit();

                        mEditor.putString(getString(R.string.name), "");
                        mEditor.commit();

                        mEditor.putString(getString(R.string.password), "");
                        mEditor.commit();

                    }
                    Intent intent = new Intent(StartActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "로그인 정보를 확인해주세요", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    private void checkSharedPreferences() {

        String name = mPreferences.getString(getString(R.string.name), "");
        String password = mPreferences.getString(getString(R.string.password), "");
        String checkbox = mPreferences.getString(getString(R.string.checkbox), "False");

        editText_email.setText(name);
        editText_password.setText(password);

        if(checkbox.equals("True")) {
            mCheckBox.setChecked(true);
        } else {
            mCheckBox.setChecked(false);
        }
    }
}
