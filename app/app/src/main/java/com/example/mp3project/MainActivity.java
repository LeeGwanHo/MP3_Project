package com.example.mp3project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MusicAdapter.OnItemClickListener {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private RecyclerView recyclerViewLike;

    private LinearLayoutManager linearLayoutManager;
    private LinearLayoutManager linearLayoutManager_like;
    private MusicAdapter musicAdapter;
    private MusicAdapter musicAdapter_like;

    private MusicDBHelper musicDBHelper;

    private ArrayList<MusicData> musicDataArrayList = new ArrayList<>();

    private ArrayList<MusicData> musicLikeArrayList = new ArrayList<>();

    private Fragment player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sdcardConnectFunc();

        // DBHelper 인스턴스
        musicDBHelper = MusicDBHelper.getInstance(getApplicationContext());

        //MusicList 값 받아오기
        musicDataArrayList = musicDBHelper.selectMusicTbl();

        findViewByIdFunc();

        // 어댑터 생성
        musicAdapter = new MusicAdapter(getApplicationContext());
        musicAdapter_like = new MusicAdapter(getApplicationContext());

        // linearLayoutManager 인스턴스
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager_like = new LinearLayoutManager(getApplicationContext());


        // recyclerView에 어댑터, 매니저 세팅
        recyclerView.setAdapter(musicAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerViewLike.setAdapter(musicAdapter_like);
        recyclerViewLike.setLayoutManager(linearLayoutManager_like);

        // 음악 리스트 가져오기
        musicDataArrayList = musicDBHelper.compareArrayList();

        // 음악 DB 저장
        insertDB(musicDataArrayList);

        // 어댑터에 데이터 세팅
        recyclerViewListUpdate(musicDataArrayList);
        likeRecyclerViewListUpdate(getLikeList());

        // 프래그먼트 지정
        replaceFrag();

        // recyclerview 클릭 이벤트
        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemCLick(View v, int position) {
                // 플레이어 화면 처리
                ((Player) player).setPlayerData(position, true);
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });

        // like_recyclerview 클릭 이벤트
        musicAdapter_like.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {

            @Override
            public void onItemCLick(View v, int position) {
                ((Player) player).setPlayerData(position, false);
                drawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });

    }


    //sdcard 외부접근권한 설정
    private void sdcardConnectFunc() {

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MODE_PRIVATE);
    }

    // View 아이디 연결
    private void findViewByIdFunc() {
        drawerLayout = findViewById(R.id.drawerLayout);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewLike = findViewById(R.id.recyclerViewLike);
    }


    // DB에 mp3 삽입
    private void insertDB(ArrayList<MusicData> arrayList) {

        boolean returnValue = musicDBHelper.insertMusicDataToDB(arrayList);

        if (returnValue) {
            Toast.makeText(getApplicationContext(), "삽입 성공", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "삽입 실패", Toast.LENGTH_SHORT).show();
        }

    }

    // 좋아요 리스트 가져오기
    private ArrayList<MusicData> getLikeList() {
        musicLikeArrayList = musicDBHelper.saveLikeList();

        if (musicLikeArrayList.isEmpty()) {
            Toast.makeText(getApplicationContext(), "가져오기 실패", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "가져오기 성공", Toast.LENGTH_SHORT).show();
        }
        return musicLikeArrayList;
    }


    // 어댑터에 데이터 세팅
    private void recyclerViewListUpdate(ArrayList<MusicData> arrayList) {
        // 어댑터에 데이터리스트 세팅
        musicAdapter.setMusicList(arrayList);

        // recyclerView에 어댑터 세팅
        recyclerView.setAdapter(musicAdapter);
        musicAdapter.notifyDataSetChanged();
    }

    // like 어댑터 데이터 세팅
    private void likeRecyclerViewListUpdate(ArrayList<MusicData> arrayList) {

        // 어댑터에 데이터리스트 세팅
        musicAdapter_like.setMusicList(arrayList);

        // recyclerView에 어댑터 세팅
        recyclerViewLike.setAdapter(musicAdapter_like);
        musicAdapter_like.notifyDataSetChanged();
    }

    // 프래그먼트 지정
    private void replaceFrag() {
        player = new Player();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.frameLayout, player);
        ft.commit();
    }


    public ArrayList<MusicData> getMusicDataArrayList() {
        return musicDataArrayList;
    }

    public MusicAdapter getMusicAdapter_like() {
        return musicAdapter_like;
    }

    public ArrayList<MusicData> getMusicLikeArrayList() {
        return musicLikeArrayList;
    }

    @Override
    protected void onStop() {
        super.onStop();

        boolean returnValue = musicDBHelper.updateMusicDataToDB(musicDataArrayList);

        if (returnValue) {
            Toast.makeText(getApplicationContext(), "업뎃 성공", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "업뎃 실패", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemCLick(View view, int position) {

    }
}