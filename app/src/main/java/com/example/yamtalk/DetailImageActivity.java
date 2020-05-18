package com.example.yamtalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

public class DetailImageActivity extends AppCompatActivity {

    ImageView imageView_detail_image;
    public RequestManager mGlideRequestManager; //이미지를 그려줄 글라이드 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_image);

        mGlideRequestManager = Glide.with(this);

        imageView_detail_image = (ImageView)findViewById(R.id.detail_image);

        Intent intent = getIntent();
        String image_uri = intent.getStringExtra("image_uri");

        mGlideRequestManager.load(image_uri).into(imageView_detail_image);
    }
}
