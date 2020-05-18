package com.example.yamtalk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity { //채팅을 진행할 액티비티

    private static final String TAG = "ChatActivity";
    private static final int GALLERY_PICK = 1;
    private DatabaseReference user_message_push;
    private StorageReference storageReference;
    public RequestManager mGlideRequestManager; //이미지를 그려줄 글라이드 변수
    private Toolbar toolbar_chat; //채팅 화면 위에 추가될 툴바
    private DatabaseReference databaseReference; //파이어베이스의 데이터베이스에 접근할 데이터베이스 변수
    private DatabaseReference databaseReference_root; //파이어베이스의 데이터베이스에 접근할 데이터베이스 변수
    private String string_chat_uid; //채팅 상대의 uid를 저장할 변수
    private ImageButton imageButton_send; //채팅 메시지를 보낼 버튼
    private ImageButton imageButton_add; //추후에 이미지나 동영상을 업로드하는 기능을 추가할 계획
    private EditText editText_message; //메시지를 입력받는 에디트텍스트
    private RecyclerView recyclerv_view_chat_message_list; //채팅 메시지 목록을 그릴 리사이클러뷰
    private MessageAdapter messageAdapter; //채팅 내역을 그려줄 리사이클러뷰 어답터
    private final ArrayList<Messages> arrayList_messages = new ArrayList<>(); //메시지 목록 정보를 저장할 리스트
    private final ArrayList<String> arrayList_messages_type = new ArrayList<>(); //메시지 타입 목록 정보를 저장할 리스트
    private LinearLayout layout_editText_message; //에디트텍스트를 감싸고있는 레이아웃
    private ProgressDialog progressDialog;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mGlideRequestManager = Glide.with(this); //글라이드 변수 설정
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference(); //데이터베이스 레퍼런스 설정
        databaseReference_root = FirebaseDatabase.getInstance().getReference(); //데이터베이스 레퍼런스 설정

        imageButton_send = (ImageButton)findViewById(R.id.ImageButton_send); //생성한 이미지버튼 변수를 레이아웃의 이미지 버튼과 연결
        imageButton_add = (ImageButton)findViewById(R.id.ImageButton_add); //생성한 이미지버튼 변수를 레이아웃의 이미지 버튼과 연결
        editText_message = (EditText)findViewById(R.id.editText_message); //생성한 에디트텍스트 변수를 레이아웃의 에디트텍스트와 연결
        toolbar_chat = (Toolbar)findViewById(R.id.chat_toolbar); //툴바를 연결
        layout_editText_message = (LinearLayout)findViewById(R.id.layout_editText_message); //xml파일의 레이아웃과 연결
        setSupportActionBar(toolbar_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        messageAdapter = new MessageAdapter(this, arrayList_messages, mGlideRequestManager, arrayList_messages_type); //메시지 목록을 그릴 어댑터 생성
        recyclerv_view_chat_message_list = (RecyclerView)findViewById(R.id.recyclerv_view_chat_message_list); //생성한 리사이클러뷰 객체를 레이아웃의 리사이클러뷰와 연걸
        recyclerv_view_chat_message_list.setHasFixedSize(true);
        recyclerv_view_chat_message_list.setLayoutManager(new LinearLayoutManager(this)); //메시지 목록을 그릴 리사이클러뷰는 리니어레이아웃으로 설정
        recyclerv_view_chat_message_list.setAdapter(messageAdapter); //메시지 목록을 그릴 리사이클러뷰의 어댑터를 설정

        Intent intent = getIntent(); //채팅 상대의 아이디를 전송받을 인텐트 생성
        string_chat_uid = intent.getStringExtra("uid"); //전송받은 uid를 변수에 저장
        getSupportActionBar().setTitle(intent.getStringExtra("name")); //툴바의 제목을 채팅 상대 아이디로 설정

//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        load_messages(); //메시지 내역 불러오기

        recyclerv_view_chat_message_list.scrollToPosition(arrayList_messages.size() - 1); //마지막 메시지 위치로 스크롤 이동

        databaseReference.child("profile").child(MainActivity.uid).addListenerForSingleValueEvent(new ValueEventListener() { //현재 로그인된 유저의 메시지 저장 경로에 접근할 수 있도록 데이터베이스 변수 설정
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(string_chat_uid)) { //지금 채팅을 하는 대상과 처음으로 채팅을 하는거라면 이 대상과의 채팅 내역을 저장할 데이터베이스 저장공간 생성
                    HashMap hashMap_add_chat = new HashMap(); //메시지를 봤는지 확인하고 메시지 발송 시간을 확인할 정보를 저장할 해시맵 변수
                    hashMap_add_chat.put("seen", false); //메시지를 읽었는지 확인할 정보는 일단 false로 설정
                    hashMap_add_chat.put("timestamp", ServerValue.TIMESTAMP); //메시지가 발송된 시간을 저장

                    HashMap hashMap_chat_user = new HashMap(); //메시지 발신자와 수신자의 정보를 저장할 해시맵 변수
                    hashMap_chat_user.put("chat/" + MainActivity.uid + "/" + string_chat_uid, hashMap_add_chat);
                    hashMap_chat_user.put("chat/" + string_chat_uid + "/" + MainActivity.uid, hashMap_add_chat);

                    databaseReference.updateChildren(hashMap_chat_user, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        imageButton_send.setOnClickListener(new View.OnClickListener() { //메시지 보내기 버튼을 클릭
            @Override
            public void onClick(View view) {
                send_message(); //메시지를 발송할 메소드
            }
        });

        imageButton_add.setOnClickListener(new View.OnClickListener() { //추후에 이미지 업로드에 사용할 이미지 버튼
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: 이미지 보내기 클릭");
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });

        editText_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        layout_editText_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "layout_editText_message: 클릭");

                recyclerv_view_chat_message_list.scrollToPosition(arrayList_messages.size() - 1); //마지막 메시지 위치로 스크롤 이동
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference(); //데이터베이스에 정보를 저장할 데이터베이스 변수
            Uri imageUri = data.getData();
            final String current_user_ref = "messages/" + MainActivity.uid + "/" + string_chat_uid;
            final String chat_user_ref = "messages/" + string_chat_uid + "/" + MainActivity.uid;
            user_message_push = databaseReference1.push();
            final String push_id = user_message_push.getKey();

            StorageReference filepath = storageReference.child("message_images").child( push_id + ".jpg");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){
                        String download_url = task.getResult().getDownloadUrl().toString();

                        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference();

                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("sender_uid", MainActivity.uid); //메시지 발신자의 uid를 저장
                        messageMap.put("receiver_uid", string_chat_uid); //메시지 수신자의 uid를 저장
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
                        recyclerv_view_chat_message_list.scrollToPosition(arrayList_messages.size() - 1); //마지막 메시지 위치로 스크롤 이동
                        databaseReference1.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError != null){
                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());
                                    recyclerv_view_chat_message_list.scrollToPosition(arrayList_messages.size() - 1); //마지막 메시지 위치로 스크롤 이동
                                }
                            }
                        });
                    }
                }
            });

            handler = new Handler();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = ProgressDialog.show(ChatActivity.this,"",
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
                            recyclerv_view_chat_message_list.scrollToPosition(arrayList_messages.size() - 1); //마지막 메시지 위치로 스크롤 이동
                        }
                    }, 10000);
                }
            });

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void load_messages() { //메시지 내역을 불러올 메소드
        Log.e(TAG, "load_messages: 호출");
        databaseReference_root.child("messages").child(MainActivity.uid).child(string_chat_uid).addChildEventListener(new ChildEventListener() { //데이터베이스에 저장된 메시지 목록을 불러올 데이터베이스 변수
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages messages = dataSnapshot.getValue(Messages.class); //메시지의 정보를 객체에 저장
                arrayList_messages.add(messages); //메시지의 정보가 저장된 객체를 리스트에 저장
                arrayList_messages_type.add(messages.getType());
                messageAdapter.notifyDataSetChanged(); //리사이클러뷰 업데이트
                recyclerv_view_chat_message_list.scrollToPosition(arrayList_messages.size() - 1); //마지막 메시지 위치로 스크롤 이동
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
        recyclerv_view_chat_message_list.scrollToPosition(arrayList_messages.size() - 1); //마지막 메시지 위치로 스크롤 이동
    }

    private void send_message() { //메시지를 발송할 메소드
        Log.e(TAG, "send_message: 메소드 시작");
        String string_message = editText_message.getText().toString(); //입력받은 메시지를 변수에 저장
        if(!string_message.equals("")) { //에디트텍스트에 입력된 메시지가 있다면
            Log.e(TAG, "send_message: 조건문 진입");
            String string_my_ref = "messages/" + MainActivity.uid + "/" + string_chat_uid; //내 채팅 내용을 저장할 경로
            String string_chat_user_ref = "messages/" + string_chat_uid + "/" + MainActivity.uid; //채팅 상대의 채팅 내용을 저장할 경로

            DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference(); //데이터베이스에 정보를 저장할 데이터베이스 변수
            DatabaseReference databaseReference_message = databaseReference1.child("messages").child(MainActivity.uid).child(string_chat_user_ref).push(); //데이터베이스 레퍼런슷 변수가 접근할 경로 설정

            String string_push_uid = databaseReference_message.getKey(); //발송되어 데이터베이스에 저장된 메시지의 키를 저장

            HashMap hashMap_message = new HashMap(); //메시지의 정보를 저장할 해시맵 변수 생성
            hashMap_message.put("sender_uid", MainActivity.uid); //메시지 발신자의 uid를 저장
            hashMap_message.put("receiver_uid", string_chat_uid); //메시지 수신자의 uid를 저장
            hashMap_message.put("message", string_message); //채팅 내용을 저장
            hashMap_message.put("seen", false);
            hashMap_message.put("type", "text"); //메시지가 텍스트인지 이미지인지 판별할 정보
            hashMap_message.put("time", ServerValue.TIMESTAMP); //메시지가 발송된 시간

            HashMap hashMap_user_map = new HashMap(); //발송된 메시지를 저장할 해시맵 변수
            hashMap_user_map.put(string_my_ref + "/" + string_push_uid, hashMap_message); //발송된 메시지를 발신자 데이터베이스에 저장
            hashMap_user_map.put(string_chat_user_ref + "/" + string_push_uid, hashMap_message); //발송된 메시지를 수신자 데이터베이스에 저장

            DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference(); //데이터베이스에 정보를 저장할 데이터베이스 변수
//            databaseReference2.child("messages").child("last_message").child(MainActivity.uid).child(string_chat_uid).setValue(string_message).isComplete();
//            databaseReference2.child("messages").child("last_message").child(string_chat_uid).child(MainActivity.uid).setValue(string_message).isComplete();

            databaseReference1.updateChildren(hashMap_user_map, new DatabaseReference.CompletionListener() { //데이터베이스에 저장된 내용 업데이트
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                }
            });
        }
        editText_message.setText(""); //에디트텍스트 초기화
//        Log.e(TAG, "send_message: 메소드 종료");
    }

}
