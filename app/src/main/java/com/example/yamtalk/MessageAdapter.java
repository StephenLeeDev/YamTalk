package com.example.yamtalk;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final String TAG = "MessageAdapter";

    private RequestManager mGlideRequestManager; //이미지를 그려줄 글라이드 변수
    private DatabaseReference databaseReference; //데이터베이스에 접근할 데이터베이스 레퍼런스 변수
    private ArrayList<Messages> arrayList_message; //메시지 내역을 저장할 리스트
    private ArrayList<String> arrayList_message_type; //메시지 내역을 저장할 리스트
    private String sender_uid; //발신자 uid를 저장할 변수
    private Context context;
    Messages messages;

    public MessageAdapter(Context context, ArrayList<Messages> arrayList_message, RequestManager requestManager, ArrayList<String> arrayList_message_type) {
        this.context = context;
        this.arrayList_message = arrayList_message; //파라미터로 넘겨받은 리스트를 저장
        this.mGlideRequestManager = requestManager; //파라미터로 넘겨받은 글라이드를 저장
        this.arrayList_message_type = arrayList_message_type;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0) { //만약 이 메시지의 발신자가 나라면
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_right_item, parent, false); //메시지를 오른쪽에 정렬해줄 레이아웃을 연결
            return new MessageViewHolder(view);
        } else if(viewType == 1) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_image_right_item, parent, false); //메시지를 오른쪽에 정렬해줄 레이아웃을 연결
            return new MessageViewHolder(view);
        } else if(viewType == 2) { //만약 이 메시지의 발신자가 상대방이라면
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_left_item, parent, false); //메시지를 왼쪽에 정렬해줄 레이아웃을 연결
            return new MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_image_left_item, parent, false); //메시지를 왼쪽에 정렬해줄 레이아웃을 연결
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        messages = arrayList_message.get(position); //메시지 정보를 객체에 저장
        sender_uid = messages.getSender_uid(); //이 메시지의 발신자 uid를 변수에 저장
        final String string_message = messages.getMessage(); //이 메시지의 내용을 변수에 저장
        String message_type = messages.getType(); //이 메시지가 텍스트인지 이미지인지 판별할 내용을 변수에 저장

        if(MainActivity.uid.equals(sender_uid)) { //만약 이 메시지의 발신자가 나라면
            if(message_type.equals("text")) {
                databaseReference = FirebaseDatabase.getInstance().getReference().child("profile").child(MainActivity.uid); //
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                    Log.e(TAG, "onDataChange: string_message : " + string_message);
                        holder.textView_message_right.setText(string_message); //메시지의 내용을 오른쪽 텍스트뷰에 입력
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            else if(message_type.equals("image")) {
                mGlideRequestManager.load(arrayList_message.get(position).getMessage()).into(holder.imageView_image_right);
                holder.imageView_image_right.setOnClickListener(     new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, DetailImageActivity.class);
                        intent.putExtra("image_uri", arrayList_message.get(position).getMessage());
                        context.startActivity(intent);
                    }
                });
            }
        } else { //만약 이 메시지의 발신자가 상대방이라면
            if(message_type.equals("text")) {
                Log.e(TAG, "onBindViewHolder: 레프트");
                databaseReference = FirebaseDatabase.getInstance().getReference().child("profile").child(sender_uid);
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String sender_id = dataSnapshot.child("id").getValue().toString();
                        String sender_profile_image = dataSnapshot.child("profile_image").getValue().toString();

                        holder.textView_id_left.setText(sender_id); //메시지의 작성자 아이디를 왼쪽 텍스트뷰에 입력
                        holder.textView_message_left.setText(string_message); //메시지의 내용을 왼쪽 텍스트뷰에 입력

                        if (!arrayList_message.get(position).equals("")) { //만약 작성자의 프로필 이미지가 있다면
                            mGlideRequestManager.load(sender_profile_image).into(holder.imageView_profile_left); //작성자의 프로필 이미지 설정
                        } else { //만약 작성자의 프로필 이미지가 없다면
                            mGlideRequestManager.load(R.drawable.default_profile_image).into(holder.imageView_profile_left); //작성자의 프로필 이미지를 기본 이미지로 설정
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else if(message_type.equals("image")) {
                databaseReference = FirebaseDatabase.getInstance().getReference().child("profile").child(sender_uid);
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String sender_id = dataSnapshot.child("id").getValue().toString();
                        String sender_profile_image = dataSnapshot.child("profile_image").getValue().toString();

                        holder.textView_id_left.setText(sender_id); //메시지의 작성자 아이디를 왼쪽 텍스트뷰에 입력

                        if (!arrayList_message.get(position).equals("")) { //만약 작성자의 프로필 이미지가 있다면
                            mGlideRequestManager.load(sender_profile_image).into(holder.imageView_profile_left); //작성자의 프로필 이미지 설정
                        } else { //만약 작성자의 프로필 이미지가 없다면
                            mGlideRequestManager.load(R.drawable.default_profile_image).into(holder.imageView_profile_left); //작성자의 프로필 이미지를 기본 이미지로 설정
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mGlideRequestManager.load(arrayList_message.get(position).getMessage()).into(holder.imageView_image_left);
                holder.imageView_image_left.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, DetailImageActivity.class);
                        intent.putExtra("image_uri", arrayList_message.get(position).getMessage());
                        context.startActivity(intent);
                    }
                });
            }
            holder.imageView_profile_left.setOnClickListener(new View.OnClickListener() { //친구의 프로필 이미지를 클릭하면 친구의 프로필 정보 상세보기 화면 호출
                @Override
                public void onClick(View view) {

                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
//        messages = arrayList_message.get(position); //메시지 정보를 객체에 저장
//        String message_type = messages.getType(); //이 메시지가 텍스트인지 이미지인지 판별할 내용을 변수에 저장
        Log.e(TAG, "getItemViewType: arrayList_message_type 사이즈 : " + arrayList_message_type.size());
        Log.e(TAG, "getItemViewType: arrayList_message_type.get(position) : " + arrayList_message_type.get(position));
        if(arrayList_message.get(position).getSender_uid().equals(MainActivity.uid) && arrayList_message_type.get(position).equals("text")) {
            return 0;
        } else if(arrayList_message.get(position).getSender_uid().equals(MainActivity.uid) && arrayList_message_type.get(position).equals("image")) {
            return 1;
        } else if(!arrayList_message.get(position).getSender_uid().equals(MainActivity.uid) && arrayList_message_type.get(position).equals("text")) {
            return 2;
        } else {
            return 3;
        }
    }

    @Override
    public int getItemCount() { //리사이클러뷰 아이템의 갯수를 파악할 메소드
        return arrayList_message.size(); //메시지 정보를 갖고 있는 리스트의 크기 리턴
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView textView_id_left; //상대방의 아이디를 나타낼 텍스트뷰
        public CircleImageView imageView_profile_left; //상대방의 프로필 이미지를 나타낼 이미지뷰
        public TextView textView_message_left; //상대방의 메시지를 나타낼 텍스트뷰
        public ImageView imageView_image_right;
        public ImageView imageView_image_left;
        public TextView textView_message_right; //나의 메시지를 나타낼 텍스트뷰

        public MessageViewHolder(View itemView) {
            super(itemView);

            textView_id_left = (TextView)itemView.findViewById(R.id.textView_id_left); //상대방의 아이디를 나타낼 텍스트뷰를 message_left_item의 변수에 연결
            textView_message_left = (TextView)itemView.findViewById(R.id.textView_message_left); //상대방의 메시지를 나타낼 텍스트뷰를 message_left_item의 변수에 연결
            imageView_profile_left = (CircleImageView)itemView.findViewById(R.id.imageView_profile_left); //상대방의 프로필 이미지를 나타낼 이미지뷰를 message_left_item의 변수에 연결
            textView_message_right = (TextView)itemView.findViewById(R.id.textView_message_right); //나의 메시지를 나타낼 텍스트뷰를 message_right_item의 변수에 변결
            imageView_image_right = (ImageView)itemView.findViewById(R.id.ImageView_image_message_right);
            imageView_image_left = (ImageView)itemView.findViewById(R.id.ImageView_image_message_left);
        }
    }
}
