package com.example.yamtalk;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ManageProfileActivity extends AppCompatActivity {                          //나의 프로필 이미지, 배경 이미지, 상태메시지를 변경할 수 있는 액티비티
    public RequestManager mGlideRequestManager;
    private static final String TAG = "ManageProfileActivity";
    final int REQ_CODE_SELECT_IMAGE_profile=1000;
    final int REQ_CODE_SELECT_IMAGE_background=1001;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;

    Bitmap bitmap_profile;                                  //프로필 이미지를 비트맵으로 저장할 변수
    Bitmap bitmap_background;                               //배경 이미지를 비트맵으로 저장할 변수
    String str_message;                                     //상태메시지를 저장할 변수
    String str_profile;                                     //프로필 이미지를 String형으로 저장할 변수
    String str_background;                                  //배경 이미지를 String형으로 저장할 변수
    TextView tv_message;                                    //상태메시지를 보여줄 텍스트뷰
    TextView tv_ID;                                    //아이디를 보여줄 텍스트뷰
    ImageView iv_profile;                //프로필 이미지를 나타낼 이미지 뷰를 생성하여 activity_manage_profile의 이미지뷰와 연결
    ImageView iv_background;          //배경 이미지를 나타낼 이미지 뷰를 생성하여 activity_manage_profile의 이미지뷰와 연결
    Gson gson;
    private ProgressDialog progressDialog;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_profile);

        mGlideRequestManager = Glide.with(this);

        Log.e(TAG, TAG + " onCreate: start");

        set_images();

        storageReference = FirebaseStorage.getInstance().getReference();
        ConstraintLayout layout_message = (ConstraintLayout)findViewById(R.id.layout_message);      //상태메시지 레이아웃을 생성하여 activity_manage_profile의 레이아웃과 연결
        iv_profile = (ImageView)findViewById(R.id.iv_profile);                //프로필 이미지를 나타낼 이미지 뷰를 생성하여 activity_manage_profile의 이미지뷰와 연결
        iv_background = (ImageView)findViewById(R.id.iv_background);          //배경 이미지를 나타낼 이미지 뷰를 생성하여 activity_manage_profile의 이미지뷰와 연결
        tv_message = (TextView)findViewById(R.id.tv_message);                              //상태메시지를 나타낼 텍스트뷰를 생성하여 activity_manage_profile의 텍스트뷰와 연결
        tv_ID = (TextView)findViewById(R.id.tv_id);                                                 //activity_manage_profile의 아이디를 나타낼 텍스트뷰와 연결

        layout_message.setOnClickListener(new View.OnClickListener() {                              //상태메시지 레이아웃을 클릭
            @Override
            public void onClick(View view) {
                final EditText et_message = new EditText(ManageProfileActivity.this);                       //변경할 상태메시지 내용을 입력받을 에디트텍스트 생성
                AlertDialog.Builder dialog = new AlertDialog.Builder(ManageProfileActivity.this);       //변경할 상태메시지 내용을 입력받을 AlertDialog 생성
                dialog.setTitle("상태메시지 변경");                                                                //AlertDialog 제목 설정
                dialog.setView(et_message);                                                                          //AlertDialog에 위에서 생성한 에디트텍스트 추가
                Intent intent = getIntent();
                et_message.setText(intent.getStringExtra("message"));
                dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        String current_uid = firebaseUser.getUid();
                        databaseReference = FirebaseDatabase.getInstance().getReference().child("profile").child(current_uid);
                        str_message = et_message.getText().toString();                              //에디트텍스트에 입력받은 내용을 변수에 저장
                        tv_message.setText(str_message);                                            //상태메시지를 입력받은 내용으로 수정
                        databaseReference.child("message").setValue(str_message);
                        dialog.cancel();
                    }
                });
                dialog.show();                                                                      //AlertDialog를 화면에 띄워줌
            }
        });

        iv_profile.setOnClickListener(new View.OnClickListener() {                      //프로필 이미지를 클릭하여 프로필 이미지를 변경
            @Override
            public void onClick(View view) {
                final CharSequence[] items = new CharSequence[]{"앨범에서 사진 선택", "기본 이미지로 변경"};       //AlertDialog가 나타낼 리스트 목록
                AlertDialog.Builder dialog = new AlertDialog.Builder(ManageProfileActivity.this);                      //AlertDialog 객체 생성
                dialog.setTitle("프로필 변경");                                                             //AlertDialog 의 제목 설정
                dialog.setItems(items, new DialogInterface.OnClickListener() {                      //리스트를 클릭하여 이벤트 발생
                    public void onClick(DialogInterface dialog, int which) {
//                        if(items[which].equals("사진 촬영")) {                                         //선택한 리스트의 항목이 "사진 촬영"이라면
//                            Intent intent = new Intent(Intent.ACTION_PICK);                                     //*나중에 진행할 예정*
//                            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
//                            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                            startActivityForResult(intent, REQ_CODE_SELECT_IMAGE_profile);
//                        }
                        if(items[which].equals("앨범에서 사진 선택")) {                                         //선택한 리스트의 항목이 "앨범에서 사진 선택"이라면
                            Intent intent = new Intent(Intent.ACTION_PICK);                                     //갤러리에서 이미지를 가져올 인텐트 생성
                            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, REQ_CODE_SELECT_IMAGE_profile);
                        }
                        else if(items[which].equals("기본 이미지로 변경")) {                                         //선택한 리스트의 항목이 "기본 이미지로 변경"이라면
                            Bitmap bitmap_profile = BitmapFactory.decodeResource(getResources(), R.drawable.default_profile_image);             //기본 프로필 이미지를 비트맵으로 변환

                            iv_profile.setImageBitmap(bitmap_profile);
                        }
                    }
                });
                dialog.show();                                                                      //AlertDialog를 화면에 띄워줌
            }
        });

        iv_background.setOnClickListener(new View.OnClickListener() {                      //배경 이미지를 클릭하여 프로필 이미지를 변경
            @Override
            public void onClick(View view) {
                final CharSequence[] items = new CharSequence[]{/*"사진 촬영", */"앨범에서 사진 선택", "기본 이미지로 변경"};       //AlertDialog가 나타낼 리스트 목록
                AlertDialog.Builder dialog = new AlertDialog.Builder(ManageProfileActivity.this);                      //AlertDialog 객체 생성
                dialog.setTitle("배경사진 변경");                                                             //AlertDialog 의 제목 설정
                dialog.setItems(items, new DialogInterface.OnClickListener() {                      //리스트를 클릭하여 이벤트 발생
                    public void onClick(DialogInterface dialog, int which) {
//                        if(items[which].equals("사진 촬영")) {                                         //선택한 리스트의 항목이 "사진 촬영"이라면
//                            Intent intent = new Intent(Intent.ACTION_PICK);                                     //*나중에 진행할 예정*
//                            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
//                            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                            startActivityForResult(intent, REQ_CODE_SELECT_IMAGE_background);
//                        }
                        if(items[which].equals("앨범에서 사진 선택")) {                                         //선택한 리스트의 항목이 "앨범에서 사진 선택"이라면
                            Intent intent = new Intent(Intent.ACTION_PICK);                                     //갤러리에서 이미지를 가져올 인텐트 생성
                            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, REQ_CODE_SELECT_IMAGE_background);
                        }
                        else if(items[which].equals("기본 이미지로 변경")) {                                         //선택한 리스트의 항목이 "기본 이미지로 변경"이라면
                            Bitmap bitmap_background = BitmapFactory.decodeResource(getResources(), R.drawable.default_background_image);       //기본 배경 이미지를 비트맵으로 변환

                            iv_background.setImageBitmap(bitmap_background);
                        }
                    }
                });
                dialog.show();                     //AlertDialog를 화면에 띄워줌
            }
        });
        load_my_profile();
    }

    @Override
    protected void onPause() {
        super.onPause();

//        save_profile(MainActivity.logined_user_id);                                                 //프로필 정보 저장 메소드 호출
    }

//    @Override
//    protected void onDestroy() {                                                                    //변경된 프로필 정보를 저장
//        super.onDestroy();
//        Log.e(TAG, "onDestroy: start");
//
//        save_profile();                                                 //프로필 정보 저장 메소드 호출
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {                 //갤러리에서 이미지를 가져와줄 메소드
        if(requestCode == REQ_CODE_SELECT_IMAGE_profile) {                                          //프로필 이미지 변경
            if(resultCode== AppCompatActivity.RESULT_OK) {
                try {
                    bitmap_profile = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());           //이미지 데이터를 비트맵으로 받아온다.
                    ImageView image = (ImageView)findViewById(R.id.iv_profile);                     //이미지뷰 변수를 생성해서 activity_manage_profile의 iv_background와 연결
                    image.setImageBitmap(bitmap_profile);                                           //연결된 iv_background를 비트맵 이미지를 세팅

                    Uri uri_image = data.getData();
                    StorageReference filepath = storageReference.child(MainActivity.uid).child("profile_image.jpg");
//                    StorageReference filepath = storageReference.child("profile_images").child("profile_image.jpg");
                    filepath.putFile(uri_image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()) {
                                String string_uri = task.getResult().getDownloadUrl().toString();
                                databaseReference = FirebaseDatabase.getInstance().getReference().child("profile").child(MainActivity.uid);
                                databaseReference.child("profile_image").setValue(string_uri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        handler = new Handler();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog = ProgressDialog.show(ManageProfileActivity.this,"",
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

                                    }
                                });
                            }
                        }
                    });

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        if(requestCode == REQ_CODE_SELECT_IMAGE_background) {                                       //배경 이미지 변경
            if(resultCode== AppCompatActivity.RESULT_OK) {
                try {
                    bitmap_background = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());        //이미지 데이터를 비트맵으로 받아옴
                    ImageView image = (ImageView)findViewById(R.id.iv_background);                  //이미지뷰 변수를 생성해서 activity_manage_profile의 iv_background와 연결
                    image.setImageBitmap(bitmap_background);                                        //연결된 iv_background를 비트맵 이미지를 세팅

                    Uri uri_image = data.getData();
                    StorageReference filepath = storageReference.child(MainActivity.uid).child("background_image.jpg");
                    filepath.putFile(uri_image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()) {
                                String string_uri = task.getResult().getDownloadUrl().toString();
                                databaseReference = FirebaseDatabase.getInstance().getReference().child("profile").child(MainActivity.uid);
                                databaseReference.child("background_image").setValue(string_uri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        handler = new Handler();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog = ProgressDialog.show(ManageProfileActivity.this,"",
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

                                    }
                                });
                            }
                        }
                    });

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
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

                if(!uri_profile.equals("")) {
                    mGlideRequestManager.load(uri_profile).into(iv_profile);
//                    Glide.with(ManageProfileActivity.this).load(uri_profile).into(iv_profile);
                } else {
                    mGlideRequestManager.load(R.drawable.default_profile_image).into(iv_profile);
//                    Glide.with(ManageProfileActivity.this).load(R.drawable.default_profile_image).into(iv_profile);
                }
                if(!uri_background.equals("")) {
                    mGlideRequestManager.load(uri_background).into(iv_background);
//                    Glide.with(ManageProfileActivity.this).load(uri_background).into(iv_background);
                } else {
                    mGlideRequestManager.load(R.drawable.default_background_image).into(iv_background);
//                    Glide.with(ManageProfileActivity.this).load(R.drawable.default_background_image).into(iv_background);
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

    private void load_my_profile() {                                                                //내 프로필 정보를 꺼내줄 메소드
        Intent intent = getIntent();
        tv_ID.setText(intent.getExtras().get("id").toString());
        tv_message.setText(intent.getExtras().get("message").toString());
    }
}
