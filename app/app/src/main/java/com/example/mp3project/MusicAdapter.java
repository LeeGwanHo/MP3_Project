package com.example.mp3project;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.CustomViewHolder> {
    private Context context;
    private ArrayList<MusicData> musicList;
    //2-2.이벤트 리스너를 만든다
    private OnItemClickListener mListener=null;

    //2.생성자를 만든다.
    public MusicAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //3. 화면 객체를 가져와서 뷰홀더에 저장한다.
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item, viewGroup, false);
        CustomViewHolder viewHolder=new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder customViewHolder, int position) {

        SimpleDateFormat sdf =new SimpleDateFormat("mm:ss");

        try{
            //앨범이미지를 비트맵으로 만들기
            Bitmap albumImg = getAlbumImg(context, Long.parseLong(musicList.get(position).getAlbumArt()), 100);
            if(albumImg != null){
                customViewHolder.albumArt.setImageBitmap(albumImg);
            customViewHolder.title.setText(musicList.get(position).getTitle());
            customViewHolder.artist.setText(musicList.get(position).getArtist());
            customViewHolder.duration.setText(sdf.format(Integer.parseInt(musicList.get(position).getDuration())));
            }
        }catch (Exception e){

        }
    }

    //앨범아트 가져오는 함수
    public Bitmap getAlbumImg(Context context, long albumArt, int imgMaxSize) {
        /*컨텐트 프로바이더(Content Provider)는 앱 간의 데이터 공유를 위해 사용됨.
        특정 앱이 다른 앱의 데이터를 직접 접근해서 사용할 수 없기 때문에
        무조건 컨텐트 프로바이더를 통해 다른 앱의 데이터를 사용해야만 한다.
        다른 앱의 데이터를 사용하고자 하는 앱에서는 Uri를 이용하여 컨텐트 리졸버(Content Resolver)를 통해
        다른 앱의 컨텐트 프로바이더에게 데이터를 요청하게 되는데
        요청받은 컨텐트 프로바이더는 Uri를 확인하고 내부에서 데이터를 꺼내어 컨텐트 리졸버에게 전달한다.
        */
        BitmapFactory.Options options=new BitmapFactory.Options();
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri=Uri.parse("content://media/external/audio/albumart/"+albumArt);

        if(uri != null){
            ParcelFileDescriptor fd = null;
            try {
                 fd = contentResolver.openFileDescriptor(uri, "r");

                 //메모리할당을 하지 않으면서 해당된 정보를 읽어올수 있음.
                 options.inJustDecodeBounds = true;
                 int scale = 0;

                if(options.outHeight > imgMaxSize || options.outWidth > imgMaxSize){
                    scale = (int)Math.pow(2,(int) Math.round(Math.log(imgMaxSize /
                            (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
                }

                //비트맵을 위해서 메모리를 할당하겠다.
                options.inJustDecodeBounds = false;
                options.inSampleSize = scale;

                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(),null,options);

                if(bitmap != null){
                    if(options.outWidth != imgMaxSize || options.outHeight != imgMaxSize){
                        Bitmap tmp = Bitmap.createScaledBitmap(bitmap, imgMaxSize, imgMaxSize,true);
                        bitmap.recycle();
                        bitmap = tmp;
                    }
                }

                return  bitmap;

            } catch (FileNotFoundException e) {
                Log.d("MusicAdapter","컨텐트 리졸버 에러발생");
            } finally {
                if(fd != null) {
                    try {
                        fd.close();
                    } catch (IOException e) {

                    }
                }
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return (musicList != null)? musicList.size() : 0;
    }

    //1.내부클래스 뷰홀더를 만든다.
    public class CustomViewHolder extends RecyclerView.ViewHolder {
        private ImageView albumArt;
        private TextView title;
        private TextView artist;
        private TextView duration;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            albumArt=itemView.findViewById(R.id.d_ivAlbum);
            title=itemView.findViewById(R.id.d_tvTitle);
            artist=itemView.findViewById(R.id.d_tvArtist);
            duration=itemView.findViewById(R.id.d_tvDuration);

            //4.
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position=getAdapterPosition();
                    if(position!=RecyclerView.NO_POSITION){
                        mListener.onItemCLick(view,position);
                    }
                }
            });
        }
    }

    //2-1.내부 인터페이스를 정의한다
    public interface  OnItemClickListener{
        //추상화 메소드를 구현
        void onItemCLick(View v, int position);

    }
    //2-3.내부인터페이스 멤버변수에 대한 setter를 만든다.
    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener=listener;
    }
    public void setMusicList(ArrayList<MusicData> musicList) {
        this.musicList = musicList;
    }
}
