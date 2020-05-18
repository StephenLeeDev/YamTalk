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

public class ProfileRecyclerViewAdapter extends RecyclerView.Adapter<ProfileRecyclerViewAdapter.ViewHolder> {       //RecyclerView.Adapter를 상속받아 리사이클러뷰 어탭터 역할을할 클래스

    private static final String TAG = "ProfileRecyclerViewAdapter";

    public RequestManager mGlideRequestManager;
    private ArrayList<String> arrayList_friends_uid = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private ArrayList<String> arrayList_friends_IDs = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private ArrayList<String> arrayList_friends_message = new ArrayList<>();          //유저의 상태 메시지를 저장할 리스트
    private ArrayList<String> arrayList_friends_profile_image = new ArrayList<>();           //유저의 프로필 이미지를 저장할 리스트
    private ArrayList<String> arrayList_friends_background_image = new ArrayList<>();           //유저의 프로필 이미지를 저장할 리스트
    private ArrayList<String> arrayList_friends_latitude = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private ArrayList<String> arrayList_friends_longitude = new ArrayList<>();               //유저의 아이디를 저장할 리스트
    private Context context;                                            //메모리에 대신 접근해줄 context

    public ProfileRecyclerViewAdapter(
            Context context
            , RequestManager requestManager
            , ArrayList<String> arrayList_friends_latitude
            , ArrayList<String> arrayList_friends_longitude
            , ArrayList<String> arrayList_friends_uid
            , ArrayList<String> arrayList_friends_IDs
            , ArrayList<String> arrayList_friends_message
            , ArrayList<String> arrayList_friends_profile_image
            , ArrayList<String> arrayList_friends_background_image) {        //생성자로 MainActivity에서 리사이클러뷰 어댑터를 생성하며 파라미너로 넘겨준 데이터를 리스트들에 저장
        this.mGlideRequestManager = requestManager;
        this.arrayList_friends_latitude = arrayList_friends_latitude;                   //MainActivity에서 넘겨준 유저의 위도를 저장
        this.arrayList_friends_longitude = arrayList_friends_longitude;                   //MainActivity에서 넘겨준 유저의 경도를 저장
        this.arrayList_friends_uid = arrayList_friends_uid;                   //MainActivity에서 넘겨준 유저의 아이디를 저장
        this.arrayList_friends_IDs = arrayList_friends_IDs;                   //MainActivity에서 넘겨준 유저의 아이디를 저장
        this.arrayList_friends_message = arrayList_friends_message;         //MainActivity에서 넘겨준 유저의 상태메시지 저장
        this.arrayList_friends_profile_image = arrayList_friends_profile_image;             //MainActivity에서 넘겨준 유저의 프로필 이미지 저장
        this.arrayList_friends_background_image = arrayList_friends_background_image;       //MainActivity에서 넘겨준 유저의 배경 이미지 저장
        this.context = context;                 //MainActivity에서 넘겨준 context를 저장
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {              //아래에서 생성한 ViewHolder 객체의 onCreateViewHolder 생명주기
        Log.e(TAG, "onCreateViewHolder: start!");
        if(viewType == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_profile_item, parent, false);     //View 객체 view 를 생성하고 이 view가 LayoutInflater가 리사이클러뷰 아이템 하나하나를 표현할 profile_item.xml 파일과 연결해서 내부 변수들의 데이터를 변경할 수 있게 해줌
            ViewHolder holder = new ViewHolder(view);               //ViewHolder 객체 holder를 생성하여 위에서 선언된 view 객체를 파라미터로 넘겨줌
            return holder;                                          //위에서 생성한 holder 객체를 리턴
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_item, parent, false);     //View 객체 view 를 생성하고 이 view가 LayoutInflater가 리사이클러뷰 아이템 하나하나를 표현할 profile_item.xml 파일과 연결해서 내부 변수들의 데이터를 변경할 수 있게 해줌
            ViewHolder holder = new ViewHolder(view);               //ViewHolder 객체 holder를 생성하여 위에서 선언된 view 객체를 파라미터로 넘겨줌
            return holder;                                          //위에서 생성한 holder 객체를 리턴
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {               //ViewHolder 객체의 onBindViewHolder 생명주기. 여기서 리사이클러뷰 내부 변수들의 데이터를 수정함
        if(position == 0) {
            if (!arrayList_friends_profile_image.get(position).equals("")) {
                mGlideRequestManager.load(arrayList_friends_profile_image.get(position)).into(holder.my_profile_image);
            } else {
                mGlideRequestManager.load(R.drawable.default_profile_image).into(holder.my_profile_image);
            }
            holder.tv_my_id.setText(arrayList_friends_IDs.get(position));
            holder.tv_my_message.setText(arrayList_friends_message.get(position));
            holder.layout_my_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, MyDetailProfileActivity.class);
                    intent.putExtra("id", arrayList_friends_IDs.get(position));
                    intent.putExtra("message", arrayList_friends_message.get(position));
                    context.startActivity(intent);
                }
            });
        }

        else if(position > 0) {
            double range; //친구와 나의 거리
            range = distance(MainActivity.latitude, MainActivity.longitude, Double.parseDouble(arrayList_friends_latitude.get(position)), Double.parseDouble(arrayList_friends_longitude.get(position)));

            if (!arrayList_friends_profile_image.get(position).equals("")) {
                mGlideRequestManager.load(arrayList_friends_profile_image.get(position)).into(holder.friends_profile_image);
            } else {
                mGlideRequestManager.load(R.drawable.default_profile_image).into(holder.friends_profile_image);
            }
            if (Math.round(range) < 1000) {
                holder.tv_range.setText(Math.round(range) + "m 떨어짐");
            } else if (Math.round(range) > 1000) {
                holder.tv_range.setText(((Math.round(range) / 100) / 10) + "km 떨어짐");
            }
            holder.tv_friends_id.setText(arrayList_friends_IDs.get(position));                                            //이 어댑터가 선언될 때 전해받은 유저 아이디를 연결된 텍스트뷰에 입력
            holder.tv_friends_message.setText(arrayList_friends_message.get(position));                                  //이 어댑터가 선언될 때 전해받은 유저 상태메시지를 연결된 텍스트뷰에 입력
            holder.parentLayout.setOnClickListener(new View.OnClickListener() {                         //이 리사이클러뷰 아이템을 클릭하면 이벤트 발생
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, DetailProfileActivity.class);                   //DetailProfileActivity로 이동할 인텐트 생성
//                intent.putExtra("profile_image", arrayList_friends_profile_image.get(position));               //클릭된 항목의 프로필 이미지를 DetailProfileActivity로 전달
//                intent.putExtra("background_image", arrayList_friends_background_image.get(position));               //클릭된 항목의 배경 이미지를 DetailProfileActivity로 전달
                    intent.putExtra("arrayList_friends_uid", arrayList_friends_uid.get(position));
                    intent.putExtra("arrayList_friends_IDs", arrayList_friends_IDs.get(position));                                    //클릭된 항목의 아이디를 DetailProfileActivity로 전달
                    intent.putExtra("arrayList_friends_message", arrayList_friends_message.get(position));
                    intent.putExtra("arrayList_friends_background_image", arrayList_friends_background_image.get(position));
                    intent.putExtra("arrayList_friends_profile_image", arrayList_friends_profile_image.get(position));
                    context.startActivity(intent);                                                      //DetailProfileActivity 생성
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "getItemViewType() called with: position = [" + position + "]");
        Log.d(TAG, "getItemViewType() called with: arrayList_friends_uid.size = [" + arrayList_friends_uid.size() + "]");
        if(arrayList_friends_uid.size() <= position) {
            return 3;
        } else {
            if (arrayList_friends_uid.get(position).equals(MainActivity.uid)) { //내 uid라면
                return 0;
            } else { //내 uid가 아니라면
                return 1;
            }
        }
    }

    @Override
    public int getItemCount() {         //리사이클러뷰의 아이템의 갯수를 알려줄 메소드
        return arrayList_friends_IDs.size();           //userID 리스트의 사이즈를 리턴
    }

    public class ViewHolder extends RecyclerView.ViewHolder{        //profile_item.xml의 내부 변수들을 연결시켜서 데이터를 변경할 수 있게 해줄 ViewHolder 클래스

        CircleImageView my_profile_image;
        TextView tv_my_message;
        TextView tv_my_id;
        ConstraintLayout layout_my_info; //내 프로필 정보가 담기는 레이아웃

        CircleImageView friends_profile_image;              //사용자의 이미지를 저장할 이미지뷰(이미지를 동그랗게 잘라줌)
        TextView tv_friends_id;                //사용자의 아이디를 저장할 텍스트뷰
        TextView tv_friends_message;           //사용자의 메시지를 입력할 텍스트뷰
        TextView tv_range; //친구가 나와 떨어진 거리를 나타낼 텍스트뷰
        ConstraintLayout parentLayout;      //아이템들이 담기는 부모 레이아웃

        public ViewHolder(View itemView) {
            super(itemView);

            my_profile_image = itemView.findViewById(R.id.iv_my_image); //my_profile_item.xml의 이미지뷰와 연결
            tv_my_message = itemView.findViewById(R.id.tv_my_message); //my_profile_item.xml의 텍스트뷰와 연결
            tv_my_id = itemView.findViewById(R.id.tv_my_id); //my_profile_item.xml의 텍스트뷰와 연결
            layout_my_info = itemView.findViewById(R.id.layout_my_info); //my_profile_item.xml의 레이아웃과 연결

            friends_profile_image = itemView.findViewById(R.id.friends_profile_image);                          //profile_item.xml파일의 이미지뷰와 연결
            tv_friends_id = itemView.findViewById(R.id.tv_friends_id);                //profile_item.xml파일의 유저 아이디를 나타낼 텍스트뷰와 연결
            tv_friends_message = itemView.findViewById(R.id.tv_friends_message);      //profile_item.xml파일의 유저 상태메시지를 나타낼 텍스트뷰와 연결
            tv_range = itemView.findViewById(R.id.tv_range); //profile_item.xml파일의 거리를 나타낼 텍스트뷰와 연결
            parentLayout = itemView.findViewById(R.id.parent_layout);           //profile_item.xml파일의 부모 레이아웃과 연결
        }
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1609.344;

        return (dist);
    }

    // This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

}