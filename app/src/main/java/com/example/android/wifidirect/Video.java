package com.example.android.wifidirect;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;


public class Video extends Activity{
    VideoView video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videoplay);

        Bundle bundle = getIntent().getExtras();
        String path = bundle.getString("video");

        video = (VideoView) findViewById(R.id.videoView);

        video.setVideoURI(Uri.parse(path));
        //play.setText(videopath);
        video.setMediaController(new MediaController(this));

        video.requestFocus();
        //Toast.makeText(this,path, Toast.LENGTH_LONG).show();
        video.start();


    }
}