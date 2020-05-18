package com.example.yamtalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChatListActivity extends AppCompatActivity {

    private static final String TAG = "ChatListActivity";
    public RequestManager mGlideRequestManager; //이미지를 그려줄 글라이드 변수
    private ArrayList<String> arrayList_friends_uid = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private ArrayList<Messages> arrayList_messages = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private ArrayList<String> arrayList_friends_IDs = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private ArrayList<String> arrayList_friends_last_message = new ArrayList<>();          //유저의 상태 메시지를 저장할 리스트
    private ArrayList<String> arrayList_friends_profile_image = new ArrayList<>();           //유저의 프로필 이미지를 저장할 리스트
    TextView tv_ask;            //클릭하여 문의하기 액티비티로 이동
    TextView tv_friends;        //클릭하여 친구목록 액티비티로 이동
    private Toolbar toolbar;                //툴바 역할을 해줄 툴바 객체
    DatabaseReference databaseReference; //DB에 접근할 객체
    DatabaseReference databaseReference_root; //DB에 접근할 객체
    String string_last_message;
    private RecyclerView recyclerView_chat_room_list;
    private ChatListAdapter chatListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        mGlideRequestManager = Glide.with(this);

        chatListAdapter = new ChatListAdapter(this, mGlideRequestManager, arrayList_friends_uid, arrayList_friends_IDs, arrayList_friends_profile_image); //메시지 목록을 그릴 어댑터 생성
        recyclerView_chat_room_list = (RecyclerView)findViewById(R.id.recycler_view_chat_room); //생성한 리사이클러뷰 객체를 레이아웃의 리사이클러뷰와 연걸
        recyclerView_chat_room_list.setHasFixedSize(true);
        recyclerView_chat_room_list.setLayoutManager(new LinearLayoutManager(this)); //메시지 목록을 그릴 리사이클러뷰는 리니어레이아웃으로 설정
        recyclerView_chat_room_list.setAdapter(chatListAdapter); //메시지 목록을 그릴 리사이클러뷰의 어댑터를 설정

        databaseReference_root = FirebaseDatabase.getInstance().getReference(); //데이터베이스 레퍼런스 설정

        toolbar = (Toolbar)findViewById(R.id.main_page_toolbar); //툴바 연결
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("YamTalk");
        tv_ask = (TextView)findViewById(R.id.tv_ask); //문의하기 액티비티로 이동할 텍스트뷰를 레이아웃의 변수와 연결
        tv_friends = (TextView)findViewById(R.id.tv_friends); //친구목록 액티비티로 이동할 텍스트뷰를 레이아웃 변수와 연결
        databaseReference = FirebaseDatabase.getInstance().getReference().child("messages").child(MainActivity.uid);

        load_chat_friends_uid(); //채팅 상대들의 uid 가져오기 메소드

        tv_friends.setOnClickListener(new View.OnClickListener() {      //클릭하여 친구목록 액티비티로 이동
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class); //친구목록 액티비티로 이동할 인텐트 생성
                startActivity(intent);                                  //친구목록(메인) 액티비티 호출
                overridePendingTransition(0, 0);    //액티비티 호출시 애니메이션 제거
                finish();                                               //현재 액티비티 종료
            }
        });

        tv_ask.setOnClickListener(new View.OnClickListener() {      //클릭하여 문의하기 액티비티로 이동
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AskActivity.class); //문의하기 액티비티로 이동할 인텐트 생성
                startActivity(intent);                                  //문의하기 액티비티 호출
                overridePendingTransition(0, 0);    //액티비티 호출시 애니메이션 제거
                finish();                                               //현재 액티비티 종료
            }
        });
    }

    private void load_messages() { //메시지 내역을 불러올 메소드
        Log.e(TAG, "load_messages: 호출");
//        for(int a = 0;a < arrayList_friends_uid.size();a++) {
            databaseReference_root.child("messages").child(MainActivity.uid).child(arrayList_friends_uid.get(0)).limitToLast(1).addChildEventListener(new ChildEventListener() { //데이터베이스에 저장된 메시지 목록을 불러올 데이터베이스 변수
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    Messages messages = dataSnapshot.getValue(Messages.class); //메시지의 정보를 객체에 저장
                    arrayList_messages.add(messages); //메시지의 정보가 저장된 객체를 리스트에 저장
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            FirebaseDatabase.getInstance().getReference().child("test").setValue("test").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isComplete()) {

                    }
                }
            });
//        }
        Log.e(TAG, "onCreate: 메시지 크기 : " + arrayList_messages.size());
//        Log.e(TAG, "onCreate: 메시지 내용 : " + arrayList_messages.get(arrayList_messages.size() - 1));

    }

    private void load_friends_profiles() { //친구의 프로필 정보를 가져올 메소드
        Log.e(TAG, "load_friends_profiles: 친구의 프로필 정보를 가져올 메소드");
        DatabaseReference databaseReference3 = FirebaseDatabase.getInstance().getReference().child("profile");
        databaseReference3.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                arrayList_friends_uid.clear();
//                arrayList_friends_IDs.clear();
//                arrayList_friends_profile_image.clear();
                for (int a = 0; a < arrayList_friends_uid.size(); a++) {
                    arrayList_friends_IDs.add(dataSnapshot.child(arrayList_friends_uid.get(a)).child("id").getValue().toString());
                    arrayList_friends_profile_image.add(dataSnapshot.child(arrayList_friends_uid.get(a)).child("profile_image").getValue().toString());
                }

                for (int a = 0; a < arrayList_friends_uid.size(); a++) {
                    for (int b = a + 1; b < arrayList_friends_uid.size(); b++) {
                        if(arrayList_friends_uid.get(a).equals(arrayList_friends_uid.get(b))) {
                            arrayList_friends_uid.remove(b);
                            arrayList_friends_IDs.remove(b);
                            arrayList_friends_profile_image.remove(b);
                        }
                    }
                }
                chatListAdapter.notifyDataSetChanged(); //리사이클러뷰 업데이트

//                load_messages(); //친구들과의 마지막 메시지 목록을 가져올 메소드
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

//    private void load_messages() { //메시지 내역을 불러올 메소드
//
//        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
//        databaseReference.child("messages").child(MainActivity.uid).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(int a = 0;a < arrayList_friends_uid.size();a++) {
//                    arrayList_friends_last_message.add(dataSnapshot.child(arrayList_friends_uid.get(a)).getValue().toString());
//
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        while (arrayList_friends_uid.size() < arrayList_friends_IDs.size()) {
//            arrayList_friends_IDs.remove(arrayList_friends_uid.size());
//            arrayList_friends_profile_image.remove(arrayList_friends_uid.size());
//        }
//        while (arrayList_friends_last_message.size() > arrayList_friends_uid.size()) {
//            arrayList_friends_last_message.remove(arrayList_friends_uid.size());
//        }
//
//        Log.e(TAG, "load_messages: 친구 uid : " + arrayList_friends_uid.size());
//        Log.e(TAG, "load_messages: 친구 id :" + arrayList_friends_IDs.size());
//        Log.e(TAG, "load_messages: 친구 메시지수 : " + arrayList_friends_last_message.size());
//        Log.e(TAG, "load_messages: 친구 이미지 : " + arrayList_friends_profile_image.size());
//        Log.e(TAG, "load_messages: 마지막 메시지 수 : " + arrayList_friends_last_message.size());
//    }

    private void load_chat_friends_uid() { //채팅 상대들의 uid를 가져올 메소드
        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().child("messages").child(MainActivity.uid);
        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> hashMap = (HashMap<String, String>) dataSnapshot.getValue();        //모든 유저의 uid 구하기 시작
                if(hashMap != null) {
                    Iterator iterator = hashMap.entrySet().iterator();
                    arrayList_friends_uid.clear();
                    while (iterator.hasNext()) {                                                        //HashMap에 저장된 모든 Key와 Value를 참조할 반복문
                        Map.Entry entry = (Map.Entry) iterator.next();
                        arrayList_friends_uid.add(entry.getKey().toString());
                    }                                                                                           //모든 유저의 uid 구하기 종료
                }
                load_friends_profiles(); //친구의 프로필 정보를 가져올 메소드
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu); //툴바 설정

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

//        if(item.getItemId() == R.id.main_another_users_btn) {
//            Intent intent = new Intent(getApplicationContext(), AnotherUsersActivity.class);
//            startActivity(intent);
//        }
        return true;
    }
}
