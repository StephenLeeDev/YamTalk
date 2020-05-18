package com.example.yamtalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class AskActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;             //파이어베이스를 사용하게해줄 파이어베이스 객체
    TextView tv_chat;           //클릭하여 채팅 액티비티로 이동
    TextView tv_friends;        //클릭하여 친구목록 액티비티로 이동
    TextView tv_email;          //클릭하여 이메일 보내기
    private Toolbar toolbar;                //툴바 역할을 해줄 툴바 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask);

        mAuth = FirebaseAuth.getInstance();
        toolbar = (Toolbar)findViewById(R.id.main_page_toolbar); //툴바를 연결
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("YamTalk");
        tv_chat = (TextView)findViewById(R.id.tv_chat); //채팅 액티비티로 이동할 텍스트뷰를 레이아웃의 변수와 연결
        tv_friends = (TextView)findViewById(R.id.tv_friends); //친구목록 액티비티로 이동할 텍스트뷰를 레이아웃의 변수와 연결
        tv_email = (TextView)findViewById(R.id.email); //메일 보내기 버튼 역할을할 텍스트뷰를 레이아웃의 변수와 연결

        tv_email.setOnClickListener(new View.OnClickListener() {        //이메일 주소를 클릭하여 암시적 인텐트로 이메일을 보낼수 있는 어플을 호출
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND); //메일을 보낼수 있는 어플을 암시적으로 호출할 인텐트 생성
                String[] tos = {"sulla2357@gmail.com"};
                intent.putExtra(Intent.EXTRA_EMAIL, tos);               //메일 수신자의 주소를 입력
                intent.setType("message/rfc822");
                startActivity(Intent.createChooser(intent, "Choose Email Client"));     //메일을 보낼수 있는 어플들과 함께 뜰 메시지
            }
        });

        tv_chat.setOnClickListener(new View.OnClickListener() {         //클릭하여 채팅 액티비티로 이동
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChatListActivity.class); //채팅목록으로 이동할 인텐트 생성
                startActivity(intent);                                  //채팅 액티비티 호출
                overridePendingTransition(0, 0);    //액티비티 호출시 애니메이션 제거
                finish();                                               //현재 액티비티 종료
            }
        });

        tv_friends.setOnClickListener(new View.OnClickListener() {         //클릭하여 친구목록 액티비티로 이동
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);//친구목록으로 이동할 인텐트 생성
                startActivity(intent);                                  //친구목록(메인) 액티비티 호출
                overridePendingTransition(0, 0);    //액티비티 호출시 애니메이션 제거
                finish();                                               //현재 액티비티 종료
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu); //툴바를 연결

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

    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

}
