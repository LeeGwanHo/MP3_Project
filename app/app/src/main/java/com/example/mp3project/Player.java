package com.example.mp3project;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Player extends Fragment implements View.OnClickListener {

    private ImageView ivAlbum;
    private TextView tvArtist, tvTitle, tvCurrentTime, tvDuration, tvPlayCount;
    private SeekBar seekBar;
    private ImageButton ibPlay1, ibPrevious, ibNext, ibLike;

    private MainActivity mainActivity;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    private int index;
    private MusicData musicData = new MusicData();
    private ArrayList<MusicData> likeArrayList = new ArrayList<>();
    private ArrayList<MusicData> musicList = new ArrayList<>();
    private MusicAdapter musicAdapter;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.player, container, false);

        // 뷰 아이디
        findViewByIdFunc(view);

        // 어댑터 가져옴
        musicAdapter = mainActivity.getMusicAdapter_like();
        // 좋아요 리스트 가져오기
        likeArrayList = mainActivity.getMusicLikeArrayList();

        musicAdapter.setMusicList(likeArrayList);

        seekBarChangeMethod();

        return view;
    }

    private void findViewByIdFunc(View view) {

        ivAlbum = view.findViewById(R.id.ivAlbum);
        tvArtist = view.findViewById(R.id.tvArtist);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime);
        tvDuration = view.findViewById(R.id.tvDuration);
        tvPlayCount = view.findViewById(R.id.tvPlayCount);
        seekBar = view.findViewById(R.id.seekBar);
        ibPlay1 = view.findViewById(R.id.ibPlay1);
        ibPrevious = view.findViewById(R.id.ibPrevious);
        ibNext = view.findViewById(R.id.ibNext);
        ibLike = view.findViewById(R.id.ibLike);

        ibPlay1.setOnClickListener(this);
        ibPrevious.setOnClickListener(this);
        ibNext.setOnClickListener(this);
        ibLike.setOnClickListener(this);
    }

    //버튼 클릭 이벤트처리 함수
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.ibPlay1:
                if (ibPlay1.isActivated()) {
                    mediaPlayer.pause();
                    ibPlay1.setActivated(false);
                } else {
                    mediaPlayer.start();
                    ibPlay1.setActivated(true);
                    setSeekBarThread();
                }
                break;
            case R.id.ibPrevious:
                mediaPlayer.stop();
                mediaPlayer.reset();
                try {
                    if (index == 0) {
                        index = mainActivity.getMusicDataArrayList().size();
                    }
                    index--;
                    setPlayerData(index, true);

                } catch (Exception e) {
                    Log.d("뒤로가기 오류", e.getMessage());
                }
                break;
            case R.id.ibNext:
                try {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    if (index == mainActivity.getMusicDataArrayList().size() - 1) {
                        index = -1;
                    }
                    index++;
                    setPlayerData(index, true);

                } catch (Exception e) {
                    Log.d("앞으로가기 오류", e.getMessage());
                }
                break;
            case R.id.ibLike:

                if (ibLike.isActivated()) {
                    ibLike.setActivated(false);
                    musicData.setLiked(0);
                    likeArrayList.remove(musicData);
                    musicAdapter.notifyDataSetChanged();
                    Toast.makeText(mainActivity, "cancel like", Toast.LENGTH_SHORT).show();
                } else {
                    ibLike.setActivated(true);
                    musicData.setLiked(1);
                    likeArrayList.add(musicData);
                    musicAdapter.notifyDataSetChanged();
                    Toast.makeText(mainActivity, "like", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }


    //시크바 변경에 관한 함수
    private void seekBarChangeMethod() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean b) {
                // 사용자가 움직였을시, seekbar 이동
                if (b) {
                    mediaPlayer.seekTo(position);
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


    //시크바 스레드 에 관한 함수
    private void setSeekBarThread() {
        Thread thread = new Thread(new Runnable() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

            @Override
            public void run() {
                while (mediaPlayer.isPlaying()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvCurrentTime.setText(simpleDateFormat.format(mediaPlayer.getCurrentPosition()));
                        }
                    });
                    SystemClock.sleep(200);
                }
            }
        });
        thread.start();
    }

    // 플레이어 화면 처리
    public void setPlayerData(int position, boolean flag) {
        index = position;

        mediaPlayer.stop();
        mediaPlayer.reset();

        MusicAdapter musicAdapter = new MusicAdapter(mainActivity);

        if (flag) {
            musicData = mainActivity.getMusicDataArrayList().get(position);

        } else {
            musicData = mainActivity.getMusicLikeArrayList().get(position);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

        tvTitle.setText(musicData.getTitle());
        tvArtist.setText(musicData.getArtist());
        tvPlayCount.setText(String.valueOf(musicData.getPlayCount()));
        tvDuration.setText(simpleDateFormat.format(Integer.parseInt(musicData.getDuration())));

        if (musicData.getLiked() == 1) {
            ibLike.setActivated(true);
        } else {
            ibLike.setActivated(false);
        }

        // 앨범 이미지 세팅
        Bitmap albumImg = musicAdapter.getAlbumImg(mainActivity, Long.parseLong(musicData.getAlbumArt()), 200);
        if (albumImg != null) {
            ivAlbum.setImageBitmap(albumImg);
        } else {
            ivAlbum.setImageResource(R.drawable.album_default);
        }

        // 음악 재생
        Uri musicURI = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicData.getId());
        try {
            mediaPlayer.setDataSource(mainActivity.getApplicationContext(), musicURI);
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(Integer.parseInt(musicData.getDuration()));
            ibPlay1.setActivated(true);

            setSeekBarThread();

            // 재생완료 리스너
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    musicData.setPlayCount(musicData.getPlayCount() + 1);
                    ibNext.callOnClick();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
