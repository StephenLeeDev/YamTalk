package com.example.yamtalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

public class MyDetailProfileActivity extends AppCompatActivity {

    private static final String TAG = "MyDetailProfileActivity";

    public RequestManager mGlideRequestManager;
    ImageView iv_background;      //배경 이미지를 나타낼 이미지뷰를 생성
    ImageView iv_profile;            //프로필 이미지를 나타낼 이미지뷰를 생성
    TextView tv_my_message;              //상태메시지를 나타낼 텍스트뷰를 생성
    TextView tv_my_id;                        //아이디를 나타낼 텍스트뷰를 생성
    Button btn_chat;                      //채팅 액티비티로 이동할 버튼을 생성
    Button btn_profile;                //프로필 수정 액티비티로 이동할 버튼을 생성
    Button btn_map; //지도 화면으로 이동할 버튼
    private DatabaseReference database;
    private FirebaseUser current_user;
    String current_uid;
    String string_profile_uri; //프로필 이미지 uri를 저장해둘 변수
    String string_background_uri; //배경 이미지 uri를 저장해둘 변수

    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_detail_profile);

        Log.e(TAG,TAG + "onCreate: start");

        mGlideRequestManager = Glide.with(this);

        iv_background = (ImageView)findViewById(R.id.iv_background);      //배경 이미지를 나타낼 이미지뷰를 생성하여 activity_my_detail_profile의 이미지뷰와 연결
        iv_profile = (ImageView)findViewById(R.id.iv_profile);            //프로필 이미지를 나타낼 이미지뷰를 생성하여 activity_my_detail_profile의 이미지뷰와 연결
        tv_my_message = (TextView)findViewById(R.id.tv_message);              //상태메시지를 나타낼 텍스트뷰를 생성하여 activity_my_detail_profile의 텍스트뷰와 연결
        tv_my_id = (TextView)findViewById(R.id.tv_id);                        //아이디를 나타낼 텍스트뷰를 생성하여 activity_my_detail_profile의 텍스트뷰와 연결
        btn_chat = (Button)findViewById(R.id.btn_chat);                      //채팅 액티비티로 이동할 버튼을 생성하여 activity_my_detail_profile의 버튼과 연결
        btn_profile = (Button)findViewById(R.id.btn_profile);                //프로필 수정 액티비티로 이동할 버튼을 생성하여 activity_my_detail_profile의 버튼과 연결

        Intent intent = getIntent();
        tv_my_id.setText(intent.getExtras().get("id").toString());
        tv_my_message.setText(intent.getExtras().get("message").toString());

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        current_uid = current_user.getUid();

        iv_profile.setOnClickListener(new View.OnClickListener() { //이미지를 클릭하면 이미지를 전체 화면으로 띄움
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DetailImageActivity.class); //이미지 상세보기 액티비티를 띄워줄 인텐트
                intent.putExtra("image_uri", string_profile_uri); //"image_uri"라는 이름으로 이미지 uri전송
                startActivity(intent); //이미지 상세보기 화면 띄우기
            }
        });

        iv_background.setOnClickListener(new View.OnClickListener() { //이미지를 클릭하면 이미지를 전체 화면으로 띄움
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DetailImageActivity.class); //이미지 상세보기 액티비티를 띄워줄 인텐트
                intent.putExtra("image_uri", string_background_uri); //"image_uri"라는 이름으로 이미지 uri전송
                startActivity(intent); //이미지 상세보기 화면 띄우기
            }
        });

        btn_chat.setOnClickListener(new View.OnClickListener() {                    //클릭하여 채팅 액티비티 생성
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);        //채팅 액티비티로 이동할 인텐트 생성
                intent.putExtra("name", tv_my_id.getText().toString());                     //내 아이디를 "name"이라는 이름으로 전송
                intent.putExtra("uid", MainActivity.uid);                                   //내 uid를 "uid"이라는 이름으로 전송
                startActivity(intent);                                                              //채팅 액티비티 생성
                finish();                                                                           //현재 액티비티 종료
            }
        });

        btn_profile.setOnClickListener(new View.OnClickListener() {                 //클릭하여 프로필을 수정하는 액티비티 생성
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ManageProfileActivity.class);       //ManageProfileActivity로 이동할 인텐트 생성
                intent.putExtra("id", tv_my_id.getText().toString());
                intent.putExtra("message", tv_my_message.getText().toString());                 //현재 상태메시지를 전송
                startActivity(intent);                                                                  //ManageProfileActivity 생성
            }
        });

    }

    protected void onResume() {
        super.onResume();

        set_images();

        database = FirebaseDatabase.getInstance().getReference().child("profile").child(current_uid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String message = dataSnapshot.child("message").getValue().toString();

                tv_my_message.setText(message);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void set_images() {                            //프로필 정보를 세팅해주는 메소드

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_user = firebaseUser.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("profile").child(current_user);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uri_profile = dataSnapshot.child("profile_image").getValue().toString();
                String uri_background = dataSnapshot.child("background_image").getValue().toString();

                string_profile_uri = uri_profile;
                string_background_uri = uri_background;

                if(!uri_profile.equals("")) {
                    mGlideRequestManager.load(uri_profile).into(iv_profile);
//                    Glide.with(MyDetailProfileActivity.this).load(uri_profile).into(iv_profile);
                } else {
                    mGlideRequestManager.load(R.drawable.default_profile_image).into(iv_profile);
//                    Glide.with(MyDetailProfileActivity.this).load(R.drawable.default_profile_image).into(iv_profile);
                }
                if(!uri_background.equals("")) {
                    mGlideRequestManager.load(uri_background).into(iv_background);
//                    Glide.with(MyDetailProfileActivity.this).load(uri_background).into(iv_background);
                } else {
                    mGlideRequestManager.load(R.drawable.default_background_image).into(iv_background);
//                    Glide.with(MyDetailProfileActivity.this).load(R.drawable.default_background_image).into(iv_background);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

}
