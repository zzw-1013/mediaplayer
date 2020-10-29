package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    //主线程创建handler，在子线程中通过handler的post(Runnable)方法更新UI信息。
    private Handler myHandler = new Handler();
    private static final String TAG = "MainActivity";
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar seekBar;
    private TextView timeTextView;
    private TextView idTextView;
    private ListView List;
    private int prenum;
    private boolean isplay=false;
    private boolean isorder=true;
    //进度条下面的当前进度文字，将毫秒化为m:ss格式
    private SimpleDateFormat time = new SimpleDateFormat("m:ss");

    private int i;//当前歌曲序号

    File pFile = Environment.getExternalStorageDirectory();//SD卡根目录
    //歌曲路径
    private String[] musicPath = new String[]{
            pFile + "/qqmusic/song/周杰伦 - Mojito [mqms2].mp3",
            pFile + "/qqmusic/song/周杰伦 - 告白气球 [mqms2].mp3",
            pFile + "/qqmusic/song/周杰伦 - 天涯过客 [mqms2].mp3",
            pFile + "/qqmusic/song/周杰伦 - 说好不哭with 五月天阿信 [mqms2].mp3",
            pFile + "/qqmusic/song/周杰伦 - 不该 (with aMEI) [mqms2].mp3",
            pFile + "/qqmusic/song/周杰伦 - 床边故事 [mqms2].mp3",
            pFile + "/qqmusic/song/周杰伦 - 手写的从前 [mqms2].mp3",
            pFile + "/qqmusic/song/太妍 (태연) - Happy [mqms2].mp3",
            pFile + "/qqmusic/song/太妍 (태연) - 사계 (Four Seasons) (四季) [mqms2].mp3",
            pFile + "/qqmusic/song/薛之谦 - 天外来物 [mqms2].mp3",
            pFile + "/qqmusic/song/薛之谦 - 彩券 [mqms2].mp3"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageButton order = findViewById(R.id.order);
        final ImageButton play = findViewById(R.id.play);
        ImageButton nextMusic = findViewById(R.id.next);
        ImageButton preMusic = findViewById(R.id.previous);

        final ArrayList<String> musicList = new ArrayList<String>();
        for(int h = 0; h<musicPath.length; h++){
            musicList.add(musicPath[h].substring(33));
        }

        seekBar = findViewById(R.id.seekbar);
        timeTextView = findViewById(R.id.text1);
        idTextView = findViewById(R.id.musicid);
        List = findViewById(R.id.musiclist);

        //运行时权限处理，动态申请WRITE_EXTERNAL_STORAGE权限
        //PackageManager.PERMISSION_GRANTED 表示有权限， PackageManager.PERMISSION_DENIED 表示无权限
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            //这里会调用后面的onRequestPermissionResult
        }else{
            initMediaPlayer(0);
        }


        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                if(isorder){
                    playNextMusic();
                    play.setImageDrawable(getResources().getDrawable(R.drawable.zanting));
                }else{
                    randomMusic();
                    play.setImageDrawable(getResources().getDrawable(R.drawable.zanting));
                }
            }
        });

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,R.layout.musiclist_item,musicList);
        List.setAdapter(arrayAdapter);
        List.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int postion, long l) {
                mediaPlayer.reset();
                i = postion;
                if(mediaPlayer != null && i >= 0 && i < 11) {
                    initMediaPlayer(i);
                    Log.d("selectmusic"+i,"play");
                    if(!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        play.setImageDrawable(getResources().getDrawable(R.drawable.zanting));
                    }
                }
            }
        });

        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isorder){
                    order.setImageDrawable(getResources().getDrawable(R.drawable.suiji));
                    isorder = false;
                }else{
                    order.setImageDrawable(getResources().getDrawable(R.drawable.shunxu));
                    isorder = true;
                }
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    play.setImageDrawable(getResources().getDrawable(R.drawable.zanting));
                }else {
                    mediaPlayer.pause();
                    play.setImageDrawable(getResources().getDrawable(R.drawable.bofang));
                }
            }
        });
        nextMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isorder){
                    playNextMusic();
                    play.setImageDrawable(getResources().getDrawable(R.drawable.zanting));
                }else{
                    randomMusic();
                    play.setImageDrawable(getResources().getDrawable(R.drawable.zanting));
                }
            }
        });
        preMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isorder) {
                    playPreMusic();
                    play.setImageDrawable(getResources().getDrawable(R.drawable.zanting));
                }else {
                    randomMusic();
                    play.setImageDrawable(getResources().getDrawable(R.drawable.zanting));
                }
            }
        });

        myHandler.post(updateUI);
    }

    private void initMediaPlayer(int musicIndex){
        try {
            //File file = new File(pFile,"Adele-Someone Like You.ape");
            mediaPlayer.setDataSource(musicPath[musicIndex]);//指定音频文件路径
            mediaPlayer.prepare();//让MediaPlayer进入到准备状态
            Log.d("play:"+musicPath[musicIndex],"play");
            String str = musicPath[musicIndex].substring(33);
            idTextView.setText(str);
        }catch(Exception e){
            e.printStackTrace();
        }

        //这个要放在指定音频文件路径之后
        seekBar.setMax(mediaPlayer.getDuration());
        //拖动进度条时应该发生的事情
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //如果不判断是否来自用户操作进度条，会不断执行下面语句块里面的逻辑
                if(fromUser){
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    //拒绝权限获取则直接关闭程序
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initMediaPlayer(0);
                }else{
                    Toast.makeText(this,"拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }
            default:
                break;
        }
    }


    private void playNextMusic(){
        if(mediaPlayer != null && i < 11 && i >=0){
            mediaPlayer.reset();//没有reset会报IllegalStateException

            if(i==10){
                initMediaPlayer(0);
                i=0;
            }else{
                initMediaPlayer(i+1);
                i = i + 1;
            }
//            i = prenum;
//
//            if(i == 6){
//                i = 0;
//                initMediaPlayer(0);
//            }else{
//                i = i + 1;
//                initMediaPlayer(i+1);
//            }

            Log.d("nextmusic="+i,"play");
            if(!mediaPlayer.isPlaying()){
                mediaPlayer.start();
            }
        }
    }

    private void randomMusic(){
        if(mediaPlayer != null && i < 11 && i >= 0) {
            mediaPlayer.reset();
//            i = prenum;
            i = new Random().nextInt(11);
            initMediaPlayer(i);

            Log.d("randommusic="+i,"play");
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }
    }

//    private void randomPreMusic(){
//        if(mediaPlayer != null && i < 4 && i >= 0) {
//            mediaPlayer.reset();
//            initMediaPlayer(prenum);
//            Log.d("randompremusic="+prenum,"play");
//            if (!mediaPlayer.isPlaying()) {
//                mediaPlayer.start();
//            }
//        }
//    }

    private void playPreMusic(){
        if(mediaPlayer != null && i < 11 && i >=0){
            mediaPlayer.reset();//没有reset会报IllegalStateException
//            switch (i){
//                case 1: case 2: case 3: case 4: case 5: case 6:
//                    initMediaPlayer(i-1);
//                    i = i - 1;
//                case 0:
//                    initMediaPlayer(6);
//                    i=6;
//            }
            if(i == 0){
                initMediaPlayer(10);
                i=10;
            }else {
                initMediaPlayer(i-1);
                i = i - 1;
            }
//            initMediaPlayer(prenum);
//            if(i == 0){
//                prenum = 6;
//            }else {
//                prenum = i - 1;
//            }
            Log.d("premusic="+i,"play");
            if(!mediaPlayer.isPlaying()){
                mediaPlayer.start();
            }
        }
    }

    //更新UI
    private Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            //获取歌曲进度并在进度条上展现
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
//            int totaltime = mediaPlayer.getDuration();
            //获取播放位置
            timeTextView.setText(time.format(mediaPlayer.getCurrentPosition()) + "s/"+time.format(mediaPlayer.getDuration())+"s");
            myHandler.postDelayed(updateUI,1000);
        }

    };


    //释放资源
    protected void onDestroy(){
        super.onDestroy();
        //handler发送是定时1000s发送的，如果不关闭，MediaPlayer release了还在getCurrentPosition就会报IllegalStateException错误
        myHandler.removeCallbacks(updateUI);
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}