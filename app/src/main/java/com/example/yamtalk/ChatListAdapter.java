package com.example.yamtalk;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {       //RecyclerView.Adapter를 상속받아 리사이클러뷰 어탭터 역할을할 클래스

    private static final String TAG = "ProfileRecyclerViewAdapter";

    public RequestManager mGlideRequestManager; //이미지를 그려줄 글라이드 변수
    private ArrayList<String> arrayList_friends_uid = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private ArrayList<String> arrayList_friends_IDs = new ArrayList<>();               //유저의 아이디를 저장할 리스트
//    private ArrayList<String> arrayList_friends_last_message = new ArrayList<>();          //유저의 상태 메시지를 저장할 리스트
    private ArrayList<String> arrayList_friends_profile_image = new ArrayList<>();           //유저의 프로필 이미지를 저장할 리스트
    private Context context;                                            //메모리에 대신 접근해줄 context

    public ChatListAdapter(Context context, RequestManager requestManager, ArrayList<String> arrayList_friends_uid, ArrayList<String> arrayList_friends_IDs, ArrayList<String> arrayList_friends_profile_image) {        //생성자로 MainActivity에서 리사이클러뷰 어댑터를 생성하며 파라미너로 넘겨준 데이터를 리스트들에 저장
        this.context = context;
        this.mGlideRequestManager = requestManager;
        this.arrayList_friends_uid = arrayList_friends_uid;                   //MainActivity에서 넘겨준 유저의 아이디를 저장
        this.arrayList_friends_IDs = arrayList_friends_IDs;                   //MainActivity에서 넘겨준 유저의 아이디를 저장
        this.arrayList_friends_profile_image = arrayList_friends_profile_image;             //MainActivity에서 넘겨준 유저의 프로필 이미지 저장

        for (int a = 0; a < this.arrayList_friends_uid.size(); a++) {
            for (int b = a + 1; b < this.arrayList_friends_uid.size(); b++) {
                if(this.arrayList_friends_uid.get(a).equals(this.arrayList_friends_uid.get(b))) {
                    this.arrayList_friends_uid.remove(b);
                    this.arrayList_friends_IDs.remove(b);
                    this.arrayList_friends_profile_image.remove(b);
                }
            }
        }
    }

    @Override
    public ChatListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {              //아래에서 생성한 ViewHolder 객체의 onCreateViewHolder 생명주기

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_room_item, parent, false);     //View 객체 view 를 생성하고 이 view가 LayoutInflater가 리사이클러뷰 아이템 하나하나를 표현할 profile_item.xml 파일과 연결해서 내부 변수들의 데이터를 변경할 수 있게 해줌
        ChatListAdapter.ViewHolder holder = new ChatListAdapter.ViewHolder(view);               //ViewHolder 객체 holder를 생성하여 위에서 선언된 view 객체를 파라미터로 넘겨줌
        return holder;                                          //위에서 생성한 holder 객체를 리턴
    }

    @Override
    public void onBindViewHolder(ChatListAdapter.ViewHolder holder, final int position) {               //ViewHolder 객체의 onBindViewHolder 생명주기. 여기서 리사이클러뷰 내부 변수들의 데이터를 수정함

        if (!arrayList_friends_profile_image.get(position).equals("")) {
            mGlideRequestManager.load(arrayList_friends_profile_image.get(position)).into(holder.image);
        } else if(arrayList_friends_profile_image.get(position).equals("")) {
            mGlideRequestManager.load(R.drawable.default_profile_image).into(holder.image);
        }
        holder.tv_user_id.setText(arrayList_friends_IDs.get(position) + "님과의 채팅방입니다");                                            //이 어댑터가 선언될 때 전해받은 유저 아이디를 연결된 텍스트뷰에 입력
//        holder.tv_last_message.setText(arrayList_friends_last_message.get(position));                                  //이 어댑터가 선언될 때 전해받은 유저 상태메시지를 연결된 텍스트뷰에 입력
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {                         //이 리사이클러뷰 아이템을 클릭하면 이벤트 발생
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);        //채팅 액티비티로 이동할 인텐트 생성
                intent.putExtra("name", arrayList_friends_IDs.get(position));                     //채팅을 할 친구의 아이디를 "name"이라는 이름으로 전송
                intent.putExtra("uid", arrayList_friends_uid.get(position));                                        //채팅을 할 친구의 uid를 "uid"이라는 이름으로 전송
                context.startActivity(intent);                                                              //채팅 액티비티 생성
            }
        });
    }

    @Override
    public int getItemCount() {         //리사이클러뷰의 아이템의 갯수를 알려줄 메소드

        for (int a = 0; a < this.arrayList_friends_uid.size(); a++) {
            for (int b = a + 1; b < this.arrayList_friends_uid.size(); b++) {
                if(this.arrayList_friends_uid.get(a).equals(this.arrayList_friends_uid.get(b))) {
                    this.arrayList_friends_uid.remove(b);
                    this.arrayList_friends_IDs.remove(b);
                    this.arrayList_friends_profile_image.remove(b);
                }
            }
        }

        Log.e(TAG, "getItemCount: 좀 돼라 : " + arrayList_friends_IDs.size());
        return arrayList_friends_IDs.size();           //userID 리스트의 사이즈를 리턴
    }

    public class ViewHolder extends RecyclerView.ViewHolder{        //profile_item.xml의 내부 변수들을 연결시켜서 데이터를 변경할 수 있게 해줄 ViewHolder 클래스

        CircleImageView image;              //사용자의 이미지를 저장할 이미지뷰(이미지를 동그랗게 잘라줌)
        TextView tv_user_id;                //사용자의 아이디를 저장할 텍스트뷰
        ConstraintLayout parentLayout;      //아이템들이 담기는 부모 레이아웃

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profile_image);                          //profile_item.xml파일의 이미지뷰와 연결
            tv_user_id = itemView.findViewById(R.id.tv_user_id);                //profile_item.xml파일의 유저 아이디를 나타낼 텍스트뷰와 연결
            parentLayout = itemView.findViewById(R.id.chat_room_parent);           //profile_item.xml파일의 부모 레이아웃과 연결
        }
    }
}