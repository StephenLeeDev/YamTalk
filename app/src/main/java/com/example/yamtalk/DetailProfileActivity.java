package com.example.yamtalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.gson.Gson;

public class DetailProfileActivity extends AppCompatActivity {

    private static final String TAG = "DetailProfileActivity";
    public RequestManager mGlideRequestManager;

    ImageView iv_background;      //배경 이미지를 나타낼 이미지뷰를 생성
    ImageView iv_profile;            //프로필 이미지를 나타낼 이미지뷰를 생성
    TextView tv_my_message;              //상태메시지를 나타낼 텍스트뷰를 생성
    TextView tv_my_id;                        //아이디를 나타낼 텍스트뷰를 생성
    Button btn_chat;                      //채팅 액티비티로 이동할 버튼을 생성
    Button btn_profile;                //프로필 수정 액티비티로 이동할 버튼을 생성

    String friends_uid;              //친구의 uid를 저장할 변수
    String friends_id;              //친구의 아이디를 저장할 변수
    String friends_message;              //친구의 아이디를 저장할 변수
    String friends_profile;              //친구의 아이디를 저장할 변수
    String friends_background;              //친구의 아이디를 저장할 변수
    String string_profile_uri; //프로필 이미지 uri를 저장해둘 변수
    String string_background_uri; //배경 이미지 uri를 저장해둘 변수
    Gson gson; //제이슨을 사용하게 해줄 지슨

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_profile);

        mGlideRequestManager = Glide.with(this); //이미지를 그려줄 글라이드 변수
        iv_background = (ImageView)findViewById(R.id.iv_background);      //배경 이미지를 나타낼 이미지뷰를 생성하여 activity_my_detail_profile의 이미지뷰와 연결
        iv_profile = (ImageView)findViewById(R.id.iv_profile);            //프로필 이미지를 나타낼 이미지뷰를 생성하여 activity_my_detail_profile의 이미지뷰와 연결
        tv_my_message = (TextView)findViewById(R.id.tv_message);              //상태메시지를 나타낼 텍스트뷰를 생성하여 activity_my_detail_profile의 텍스트뷰와 연결
        tv_my_id = (TextView)findViewById(R.id.tv_id);                        //아이디를 나타낼 텍스트뷰를 생성하여 activity_my_detail_profile의 텍스트뷰와 연결
        btn_chat = (Button)findViewById(R.id.btn_chat);                      //채팅 액티비티로 이동할 버튼을 생성하여 activity_my_detail_profile의 버튼과 연결

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
                intent.putExtra("name", tv_my_id.getText().toString());                     //채팅을 할 친구의 아이디를 "name"이라는 이름으로 전송
                intent.putExtra("uid", friends_uid);                                        //채팅을 할 친구의 uid를 "uid"이라는 이름으로 전송
                startActivity(intent);                                                              //채팅 액티비티 생성
                finish();                                                                           //현재 액티비티 종료
            }
        });

        Intent intent = getIntent(); //친구목록(메인) 액티비티에서 전송해준 데이터를 수신할 인텐트 생성

        friends_uid = intent.getStringExtra("arrayList_friends_uid"); //친구의 uid 정보 수신
        friends_id = intent.getStringExtra("arrayList_friends_IDs"); //친구의 아이디 정보 수신
        friends_message = intent.getStringExtra("arrayList_friends_message"); //친구의 메시지 정보 수신
        friends_profile = intent.getStringExtra("arrayList_friends_profile_image"); //친구의 프로필 이미지 정보 수신
        friends_background = intent.getStringExtra("arrayList_friends_background_image"); //친구의 배경 이미지 정보 수신

        load_friend_profile();                                                                          //내 프로필 로드
    }

    private void load_friend_profile() {                                                                //내 프로필 정보를 꺼내줄 메소드

        string_profile_uri = friends_profile;
        string_background_uri = friends_background;
        tv_my_id.setText(friends_id); //친구의 아이디 입력
        tv_my_message.setText(friends_message); //친구의 상태메시지 입력
//        Log.e(TAG, "load_friend_profile: " + friends_profile);
        if(!friends_profile.equals("")) { //만약 친구의 프로필 사진이 있다면
            mGlideRequestManager.load(friends_profile).into(iv_profile); //이미지뷰에 친구의 프로필 이미지 설정
        } else if(friends_profile.equals("")) { //만약 친구의 프로필 사진이 없다면
            mGlideRequestManager.load(R.drawable.default_profile_image).into(iv_profile); //이미지뷰에 기본 프로필 이미지 설정
        }
        if(!friends_background.equals("")) { //만약 친구의 배경이미지가 없다면
            mGlideRequestManager.load(friends_background).into(iv_background); //이미지뷰에 친구의 배경 이미지 설정
        } else if(friends_background.equals("")) { //만약 친구의 배경이미지가 없다면
            mGlideRequestManager.load(R.drawable.default_background_image).into(iv_background); //이미지뷰에 기본 배경이미지 설정
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

}
