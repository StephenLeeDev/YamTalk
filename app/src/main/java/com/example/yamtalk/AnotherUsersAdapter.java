package com.example.yamtalk;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.support.constraint.Constraints.TAG;

public class AnotherUsersAdapter extends RecyclerView.Adapter<AnotherUsersAdapter.ViewHolder> {

    public RequestManager mGlideRequestManager;
    private ArrayList<String> arrayList_me_and_friends_uid = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private ArrayList<String> arrayList_unknowns_uid = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private ArrayList<String> arrayList_unknowns_IDs = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private ArrayList<String> arrayList_unknowns_message = new ArrayList<>();          //유저의 상태 메시지를 저장할 리스트
    private ArrayList<String> arrayList_unknowns_profile_image = new ArrayList<>();           //유저의 프로필 이미지를 저장할 리스트
    private ArrayList<String> arrayList_unknowns_background_image = new ArrayList<>();           //유저의 프로필 이미지를 저장할 리스트
    private Context context;                                            //AnotherUser 화면에 대신 접근해줄 context
    String string_friends;
    Gson gson;

    public AnotherUsersAdapter(Context context, ArrayList<String> arrayList_me_and_friends_uid, RequestManager requestManager, ArrayList<String> arrayList_unknowns_uid, ArrayList<String> arrayList_unknowns_IDs, ArrayList<String> arrayList_unknowns_message, ArrayList<String> arrayList_unknowns_profile_image, ArrayList<String> arrayList_unknowns_background_image) {        //생성자로 MainActivity에서 리사이클러뷰 어댑터를 생성하며 파라미너로 넘겨준 데이터를 리스트들에 저장
        this.mGlideRequestManager = requestManager;
        this.arrayList_me_and_friends_uid = arrayList_me_and_friends_uid;
//        Log.e(TAG, "AnotherUsersAdapter: 전송받은 크기 : " + arrayList_me_and_friends_uid.size());
        this.arrayList_unknowns_uid = arrayList_unknowns_uid;
        this.arrayList_unknowns_IDs = arrayList_unknowns_IDs;                   //MainActivity에서 넘겨준 유저의 아이디를 저장
        this.arrayList_unknowns_message = arrayList_unknowns_message;         //MainActivity에서 넘겨준 유저의 상태메시지 저장
        this.arrayList_unknowns_profile_image = arrayList_unknowns_profile_image;             //MainActivity에서 넘겨준 유저의 프로필 이미지 저장
        this.arrayList_unknowns_background_image = arrayList_unknowns_background_image;       //MainActivity에서 넘겨준 유저의 배경 이미지 저장
        this.context = context;                 //MainActivity에서 넘겨준 context를 저장
//        for(int a = 0;a < arrayList_me_and_friends_uid.size();a++) {
//            Log.e(TAG, "initRecyclerView: arrayList_me_and_friends_uid_" + a + " " + arrayList_me_and_friends_uid.get(a));
//        }
//        for(int a = 0;a < arrayList_unknowns_uid.size();a++) {
//            Log.e(TAG, "initRecyclerView: arrayList_unknowns_uid" + a + " " + arrayList_unknowns_uid.get(a));
//        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {              //아래에서 생성한 ViewHolder 객체의 onCreateViewHolder 생명주기

        Log.e(TAG, "onCreateViewHolder: 온크리에이트 진입");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_unknown_item, parent, false);     //View 객체 view 를 생성하고 이 view가 LayoutInflater가 리사이클러뷰 아이템 하나하나를 표현할 profile_item.xml 파일과 연결해서 내부 변수들의 데이터를 변경할 수 있게 해줌
        ViewHolder holder = new ViewHolder(view);               //ViewHolder 객체 holder를 생성하여 위에서 선언된 view 객체를 파라미터로 넘겨줌
        return holder;                                          //위에서 생성한 holder 객체를 리턴
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {               //ViewHolder 객체의 onBindViewHolder 생명주기. 여기서 리사이클러뷰 내부 변수들의 데이터를 수정함

        Log.e(TAG, "onBindViewHolder: position : " + position + " " + arrayList_unknowns_uid.get(position));

        if(!arrayList_unknowns_profile_image.get(position).equals("")) {
            mGlideRequestManager.load(arrayList_unknowns_profile_image.get(position)).into(holder.image);
        } else {
            mGlideRequestManager.load(R.drawable.default_profile_image).into(holder.image);
        }

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        holder.tv_user_id.setText(arrayList_unknowns_IDs.get(position));                                            //이 어댑터가 선언될 때 전해받은 유저 아이디를 연결된 텍스트뷰에 입력
        holder.btn_add_friends.setOnClickListener(new View.OnClickListener() {                      //친구추가 버튼을 클릭하여 친구 추가
            @Override
            public void onClick(View view) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("profile").child(MainActivity.uid).child("friends");
                Log.e(TAG, "AnotherUsersAdapter: 더하기 전 크기 : " + arrayList_me_and_friends_uid.size());
                arrayList_me_and_friends_uid.add(arrayList_unknowns_uid.get(position));
                Log.e(TAG, "AnotherUsersAdapter: 저장 전 크기 : " + arrayList_me_and_friends_uid.size());
                for(int a = 0;a < arrayList_me_and_friends_uid.size();a++) {
                    Log.e(TAG, "onClick: arrayList_me_and_friends_uid " + a + "번 " + arrayList_me_and_friends_uid.get(a));
                }

                Intent intent = new Intent(context, AnotherUsersActivity.class);
                intent.putStringArrayListExtra("arrayList_me_and_friends_uid", arrayList_me_and_friends_uid);

                Type listType = new TypeToken<ArrayList<String>>() {
                }.getType();
                gson = new GsonBuilder().create();
                String string_friends = gson.toJson(arrayList_me_and_friends_uid, listType);

                databaseReference.setValue(string_friends);
                delete(position);
            }
        });
    }

    @Override
    public int getItemCount() {         //리사이클러뷰의 아이템의 갯수를 알려줄 메소드
        Log.e(TAG, "getItemCount: " + arrayList_unknowns_uid.size());
        return arrayList_unknowns_uid.size();           //userID 리스트의 사이즈를 리턴
    }

    public class ViewHolder extends RecyclerView.ViewHolder{        //profile_unknowns_item.xml의 내부 변수들을 연결시켜서 데이터를 변경할 수 있게 해줄 ViewHolder 클래스

        CircleImageView image;              //사용자의 이미지를 저장할 이미지뷰(이미지를 동그랗게 잘라줌)
        TextView tv_user_id;                //사용자의 아이디를 저장할 텍스트뷰
        Button btn_add_friends;              //친구추가 버튼
        ConstraintLayout parentLayout;      //아이템들이 담기는 부모 레이아웃

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);                          //profile_unknown_item.xml파일의 이미지뷰와 연결
            tv_user_id = itemView.findViewById(R.id.tv_user_id);                //profile_unknown_item.xml파일의 유저 아이디를 나타낼 텍스트뷰와 연결
            btn_add_friends = itemView.findViewById(R.id.btn_add_friends);      //profile_unknown_item.xml파일의 버튼과 연결
            parentLayout = itemView.findViewById(R.id.parent_layout);           //profile_unknown_item.xml파일의 부모 레이아웃과 연결
        }
    }

    public void delete(int position) {
        arrayList_unknowns_uid.remove(position);
        arrayList_unknowns_IDs.remove(position);
        arrayList_unknowns_message.remove(position);
        arrayList_unknowns_profile_image.remove(position);
        arrayList_unknowns_background_image.remove(position);

        notifyItemRemoved(position);
        notifyItemRangeChanged(position,arrayList_unknowns_IDs.size());
    }
}