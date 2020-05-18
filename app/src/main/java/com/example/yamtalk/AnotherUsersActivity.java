package com.example.yamtalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

public class AnotherUsersActivity extends AppCompatActivity {

    public RequestManager mGlideRequestManager;
    private static final String TAG = "AnotherUsersActivity";
    private DatabaseReference mUsersDatabase;
    private ArrayList<String> arrayList_friends_uid = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private ArrayList<String> arrayList_me_and_friends_uid = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private ArrayList<String> arrayList_unknowns_id = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private ArrayList<String> arrayList_unknowns_uid = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private ArrayList<String> arrayList_unknowns_message = new ArrayList<>();          //유저의 상태 메시지를 저장할 리스트
    private ArrayList<String> arrayList_unknowns_profile_image = new ArrayList<>();           //유저의 프로필 이미지를 저장할 리스트
    private ArrayList<String> arrayList_unknowns_background_image = new ArrayList<>();           //유저의 프로필 이미지를 저장할 리스트
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another_users);

        mGlideRequestManager = Glide.with(this); //글라이드 변수 설정

        Intent intent = getIntent();                                                    //MainActivity에서 보낸 arrayList_friends_IDs를 수신할 인텐트 생성
        arrayList_unknowns_uid = intent.getStringArrayListExtra("arrayList_unknowns_uid");       //MainActivity에서 보낸 arrayList_friends_IDs를 수신해서 저장
        arrayList_me_and_friends_uid = intent.getStringArrayListExtra("arrayList_me_and_friends_uid");
//        Log.e(TAG, "onCreate: arrayList_me_and_friends_uid : " + arrayList_me_and_friends_uid.size());
        load_unknowns_profiles(); //모르는 유저들의 프로필 정보를 불러올 메소드
//        initRecyclerView(); //모르는 유저들의 목록을 그려줄 메소드
    }

    private void load_unknowns_profiles() {                                                         //친구가 아닌 모든 유저의 프로필 정보를 가져올 메소드
        Log.e(TAG, "load_unknowns_profiles: 반복문 바로위로 진입 : " + arrayList_unknowns_uid.size());
        for(int a = 0;a < arrayList_unknowns_uid.size();a++) {
            Log.e(TAG, "load_unknowns_profiles: 반복문 진입 : " + a +"_번 uid" + arrayList_unknowns_uid.get(a));
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("profile").child(arrayList_unknowns_uid.get(a));
            mUsersDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.e(TAG, "onDataChange: 진입");
                    arrayList_unknowns_id.add(dataSnapshot.child("id").getValue().toString());
                    Log.e(TAG, "onDataChange: " + dataSnapshot.child("id").getValue().toString());
                    arrayList_unknowns_profile_image.add(dataSnapshot.child("profile_image").getValue().toString());
                    arrayList_unknowns_background_image.add(dataSnapshot.child("background_image").getValue().toString());
                    arrayList_unknowns_message.add(dataSnapshot.child("message").getValue().toString());

                    FirebaseDatabase.getInstance().getReference().child("test").setValue("test").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isComplete()) {
                                initRecyclerView();
                            }
                        }
                    });
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void initRecyclerView() {       //리사이클러뷰 어댑터를 생성하는 메소드
        Log.e(TAG, "initRecyclerView: 다른 유저들 리스트 그리기 메소드 진입");
        RecyclerView recyclerView = findViewById(R.id.recyclerv_view_users);                  //리사이클러뷰 객체를 생성하여 activity_main.xml 파일의 recycler_view와 연결
        AnotherUsersAdapter adapter = new AnotherUsersAdapter(this, arrayList_me_and_friends_uid, mGlideRequestManager, arrayList_unknowns_uid, arrayList_unknowns_id, arrayList_unknowns_message, arrayList_unknowns_profile_image, arrayList_unknowns_background_image);       //만들어 놓은 리사이클러뷰 어댑터 클래스로 객체를 생성하고 유저의 정보를 담은 리스트들을 파라미터로 넘겨줌
        recyclerView.setAdapter(adapter);                                               //리사이클러뷰 객체의 어댑터를 위에서 생성한 어댑터로 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this));           //리사이클러뷰 객체의 레이아웃을 리니어 레이아웃으로 설정
    }

    protected void onDestroy() {
        super.onDestroy();
        Intent intent = getIntent();
        ArrayList<String> arrayList_me_and_friends_uid = intent.getStringArrayListExtra("arrayList_me_and_friends_uid");
        Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
        intent1.putStringArrayListExtra("arrayList_me_and_friends_uid", arrayList_me_and_friends_uid);
        finish();
    }
}