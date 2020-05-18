package com.example.yamtalk;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public RequestManager mGlideRequestManager; //이미지를 그려줄 글라이드 변수
    public static String uid; //현재 로그인한 유저의 uid를 저장할 전역 변수
    private static final String TAG = "MainActivity";
    public static double latitude; //나의 위도를 저장할 변수
    public static double longitude; //나의 경도를 저장할 변수

    ArrayList<String> arrayList_me_and_friends_uid = new ArrayList<>();                //나와 친구들의 아이디를 저장할 리스트
    ArrayList<String> arrayList_friends_uid = new ArrayList<>();                //친구들의 아이디를 저장할 리스트
    ArrayList<String> arrayList_friends_id = new ArrayList<>();                //친구들의 아이디를 저장할 리스트
    ArrayList<String> arrayList_friends_message = new ArrayList<>();            //친구들의 상태메시지를 저장할 리스트
    ArrayList<String> arrayList_friends_profile_image = new ArrayList<>();      //친구들의 프로필 이미지를 저장할 리스트
    ArrayList<String> arrayList_friends_background_image = new ArrayList<>();      //친구들의 배경 이미지를 저장할 리스트
    ArrayList<String> arrayList_friends_latitude = new ArrayList<>();                //친구들의 위도를 저장할 리스트
    ArrayList<String> arrayList_friends_longitude = new ArrayList<>();                //친구들의 경도를 저장할 리스트

    private ArrayList<String> arrayList_unknowns_id = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private ArrayList<String> arrayList_unknowns_uid = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private ArrayList<String> arrayList_unknowns_message = new ArrayList<>();          //유저의 상태 메시지를 저장할 리스트
    private ArrayList<String> arrayList_unknowns_profile_image = new ArrayList<>();           //유저의 프로필 이미지를 저장할 리스트
    private ArrayList<String> arrayList_unknowns_background_image = new ArrayList<>();           //유저의 프로필 이미지를 저장할 리스트
    private boolean isPermission; //사용자의 gps 정보를 가져올 수 있는 퍼미션
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    Gson gson;
    private FirebaseAuth mAuth;             //파이어베이스를 사용하게해줄 파이어베이스 객체
    private Toolbar toolbar;                //툴바 역할을 해줄 툴바 객체
    FirebaseUser current_user;
    TextView tv_chat;           //클릭하여 채팅 액티비티로 이동
    TextView tv_ask;            //클릭하여 문의하기 액티비티로 이동
    ImageView iv_profile; //프로필 이미지를 설정할 이미지뷰
    TextView tv_id; //유저의 아이디를 입력할 텍스트뷰
    TextView tv_message; //유저의 상태메시지를 입력할 텍스트뷰
    ConstraintLayout layout_my_info; //내 정보를 담을 아이템 레이아웃
    ProfileRecyclerViewAdapter profileRecyclerViewAdapter;
    private DatabaseReference database;
    private ProgressDialog progressDialog;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = ProgressDialog.show(MainActivity.this,"",
                        "Please wait",true);
                handler.postDelayed( new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (progressDialog!=null&&progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                        } catch ( Exception e ) {
                            e.printStackTrace();
                        }
                    }
                }, 3000);
            }
        });

        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser(); //내 uid를 알아낼 변수
        uid = current_user.getUid(); //내 uid를 전역변수에 저장
        Log.e(TAG, "onCreate: uid : " + uid);

        check_permission(); //나의 gps 퍼미션 상태를 체크할 메소드

        if(isPermission == false) { //만약 나의 퍼미션이 false라면
            callPermission(); //퍼미션을 요청할 메소드
            if(isPermission == false) { //만약 퍼미션을 수락하지 않으면 어플 종료
                Log.e(TAG, "onCreate: isPermission : " + isPermission);
//                moveTaskToBack(true);
//                finish();
//                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }

        Thread gpsWorker = new Thread(new GpsWorker());
        gpsWorker.setDaemon(true);
        gpsWorker.start();

//        Thread updateWorker = new Thread(new UpdateWorker());
//        updateWorker.setDaemon(true);
//        updateWorker.start();

        mGlideRequestManager = Glide.with(this);

        iv_profile = (ImageView)findViewById(R.id.iv_my_image);
        tv_id = (TextView)findViewById(R.id.tv_my_id);
        tv_message = (TextView)findViewById(R.id.tv_my_message);
        mAuth = FirebaseAuth.getInstance();
        toolbar = (Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("YamTalk");
        tv_chat = (TextView)findViewById(R.id.tv_chat);             //채팅목록 탭 역할을할 텍스트뷰와 연결
        tv_ask = (TextView)findViewById(R.id.tv_ask);               //문의하기 탭 역할을할 텍스트뷰와 연결

//        layout_my_info = (ConstraintLayout)findViewById(R.id.layout_my_info);
//
//        layout_my_info.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, MyDetailProfileActivity.class);
//                intent.putExtra("id", tv_id.getText().toString());
//                intent.putExtra("message", tv_message.getText().toString());
//                startActivity(intent);
//            }
//        });

        tv_chat.setOnClickListener(new View.OnClickListener() {         //클릭하여 채팅 액티비티로 이동
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChatListActivity.class);
                startActivity(intent);                                  //채팅 액티비티 호출
                overridePendingTransition(0, 0);    //액티비티 호출시 애니메이션 제거
                finish();                                               //현재 액티비티 종료
            }
        });

        tv_ask.setOnClickListener(new View.OnClickListener() {      //클릭하여 문의하기 액티비티로 이동
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AskActivity.class);
                startActivity(intent);                                  //문의하기 액티비티 호출
                overridePendingTransition(0, 0);    //액티비티 호출시 애니메이션 제거
                finish();                                               //현재 액티비티 종료
            }
        });
    }

    public class UpdateWorker implements Runnable {
        @Override
        public void run() {
            Log.e(TAG, "run: UpdateWorker running");
            while (true) {
                handler_update.sendEmptyMessage(0);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    Handler handler_update = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                set_my_info_and_load_unknown();
            }
        }
    };

    public class GpsWorker implements Runnable { //주기적으로 유저의 위치 정보를 가져올 스레드
        @Override
        public void run() {
            Log.e(TAG, "run: GpsWorker running");
            while(true) {
                handler_gps.sendEmptyMessage(0); //유저의 현재 위치를 알아낼 핸들러
                try {
                    Thread.sleep(60000); //60초씩 슬립
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    Handler handler_gps = new Handler() { //유저의 현재 위치를 알아낼 핸들러
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0) {
                GpsInfo gps;
                gps = new GpsInfo(MainActivity.this);
                // GPS 사용유무 가져오기
                if (gps.isGetLocation()) {

                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();

//                    Log.e(TAG, "onCreate: 위도 : " + String.valueOf(latitude));
//                    Log.e(TAG, "onCreate: 경도 : " + String.valueOf(longitude));
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("profile").child(MainActivity.uid);
                    databaseReference.child("latitude").setValue(String.valueOf(latitude)).isComplete();
                    databaseReference.child("longitude").setValue(String.valueOf(longitude)).isComplete();
//                    profileRecyclerViewAdapter.notifyDataSetChanged();
                } else {
                    // GPS 를 사용할수 없으므로
                    gps.showSettingsAlert();
                }
            }
        }
    };

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
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference.child("profile").child(MainActivity.uid).child("permission").setValue(true).isComplete();
            Log.e(TAG, "onRequestPermissionsResult: isPermission : " + isPermission);
        }
    }

    private void callPermission() { //유저의 위치를 추적할 수 있도록 유저의 전화번호에 접근할 수 있는 퍼미션을 요청할 메소드
        // Check the SDK version and whether the permission is already granted or not.
        Log.e(TAG, "callPermission: 진입");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "callPermission: if문 진입");
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            Log.e(TAG, "callPermission: else if문 진입");
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            Log.e(TAG, "callPermission: else문 진입");
            isPermission = true;
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference.child("profile").child(MainActivity.uid).child("permission").setValue(true).isComplete();
        }
    }

    private void check_permission() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("profile");
        databaseReference.child(MainActivity.uid).child("permission");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String string_permission = dataSnapshot.getValue().toString();
                if(string_permission.equals("true")) {
                    isPermission = true;
                } else {
                    isPermission = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void load_unknowns_uid() {
        DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().child("profile");
        databaseReference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> hashMap = (HashMap<String, String>) dataSnapshot.getValue();        //모든 유저의 uid 구하기 시작
                Iterator iterator = hashMap.entrySet().iterator();
                arrayList_unknowns_uid.clear();
                while (iterator.hasNext()) {                                                        //HashMap에 저장된 모든 Key와 Value를 참조할 반복문
                    Map.Entry entry = (Map.Entry)iterator.next();
                    arrayList_unknowns_uid.add(entry.getKey().toString());
                }                                                                                           //모든 유저의 uid 구하기 종료

                for(int a = 0;a < arrayList_me_and_friends_uid.size();a++) {
                    for(int b = 0;b < arrayList_unknowns_uid.size();b++) {
                        if(arrayList_me_and_friends_uid.get(a).equals(arrayList_unknowns_uid.get(b))) {
                            arrayList_unknowns_uid.remove(b);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        load_friends_profiles();
    }

    private void load_friends_profiles() {                                                         //친구의 프로필 정보를 가져올 메소드
        arrayList_friends_uid.add(0, uid);
        DatabaseReference databaseReference3 = FirebaseDatabase.getInstance().getReference().child("profile");
        databaseReference3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (int a = 0; a < arrayList_friends_uid.size(); a++) {
                    arrayList_friends_id.add(dataSnapshot.child(arrayList_friends_uid.get(a)).child("id").getValue().toString());
                    arrayList_friends_profile_image.add(dataSnapshot.child(arrayList_friends_uid.get(a)).child("profile_image").getValue().toString());
                    arrayList_friends_message.add(dataSnapshot.child(arrayList_friends_uid.get(a)).child("message").getValue().toString());
                    arrayList_friends_background_image.add(dataSnapshot.child(arrayList_friends_uid.get(a)).child("background_image").getValue().toString());
                    arrayList_friends_latitude.add(dataSnapshot.child(arrayList_friends_uid.get(a)).child("latitude").getValue().toString());
                    arrayList_friends_longitude.add(dataSnapshot.child(arrayList_friends_uid.get(a)).child("longitude").getValue().toString());
                }
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

    private void initRecyclerView() {       //리사이클러뷰 어댑터를 생성하는 메소드

        if(arrayList_friends_id.size() > arrayList_friends_uid.size()) {
            while(arrayList_friends_id.size() > arrayList_friends_uid.size()) {
                arrayList_friends_id.remove(0);
                arrayList_friends_message.remove(0);
                arrayList_friends_profile_image.remove(0);
                arrayList_friends_background_image.remove(0);

            }
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerv_view_friends);                  //리사이클러뷰 객체를 생성하여 activity_main.xml 파일의 recycler_view와 연결
        profileRecyclerViewAdapter = new ProfileRecyclerViewAdapter(
                this
                , mGlideRequestManager
                , arrayList_friends_latitude
                , arrayList_friends_longitude
                , arrayList_friends_uid
                , arrayList_friends_id
                , arrayList_friends_message
                , arrayList_friends_profile_image
                , arrayList_friends_background_image);       //만들어 놓은 리사이클러뷰 어댑터 클래스로 객체를 생성하고 유저의 정보를 담은 리스트들을 파라미터로 넘겨줌
        recyclerView.setAdapter(profileRecyclerViewAdapter);                                               //리사이클러뷰 객체의 어댑터를 위에서 생성한 어댑터로 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(this){ //리사이클러뷰 객체의 레이아웃을 리니어 레이아웃으로 설정
            @Override
            public boolean canScrollVertically() { // 세로스크롤 막기
                return true;
            }
        });
    }

    private void set_my_info_and_load_unknown() {

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("profile").child(MainActivity.uid);
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.e(TAG, "onDataChange: 여기서 멈추나?");
                String id = dataSnapshot.child("id").getValue().toString();
                String message = dataSnapshot.child("message").getValue().toString();
                String uri = dataSnapshot.child("profile_image").getValue().toString();

//                if(!uri.equals("")) {
//                    mGlideRequestManager.load(uri).into(iv_profile);
//                } else {
//                    mGlideRequestManager.load(R.drawable.default_profile_image).into(iv_profile);
//                }
//                tv_id.setText(id);
//                tv_message.setText(message);

                String string_friends = dataSnapshot.child("friends").getValue().toString();

                Type listType = new TypeToken<ArrayList<String>>() {
                }.getType();
                gson = new GsonBuilder().create();
                arrayList_friends_uid = gson.fromJson(string_friends, listType);
                arrayList_me_and_friends_uid = gson.fromJson(string_friends, listType);

                for(int a = 0;a < arrayList_friends_uid.size();a++) {
                    if(arrayList_friends_uid.get(a).equals(MainActivity.uid)) {
                        arrayList_friends_uid.remove(a);
                        break;
                    }
                }
                load_unknowns_uid();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        set_my_info_and_load_unknown();

    }

    private void send_to_start() {
        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_btn) {
            FirebaseAuth.getInstance().signOut();
            send_to_start();
        }
        if(item.getItemId() == R.id.main_another_users_btn) {
            if(arrayList_unknowns_uid.size() > 0) {
                for (int a = 0;a < arrayList_unknowns_uid.size(); a++) {
                    for(int b = a + 1;b < arrayList_unknowns_uid.size();b++) {
                        if(arrayList_unknowns_uid.get(a).equals(arrayList_unknowns_uid.get(b))) {
                            arrayList_unknowns_uid.remove(b);
                            b--;
                        }
                    }
                }
            }
            Intent intent = new Intent(getApplicationContext(), AnotherUsersActivity.class);
            intent.putStringArrayListExtra("arrayList_unknowns_uid", arrayList_unknowns_uid);
            intent.putStringArrayListExtra("arrayList_me_and_friends_uid", arrayList_me_and_friends_uid);
            startActivity(intent);
        }
        return true;
    }
}
