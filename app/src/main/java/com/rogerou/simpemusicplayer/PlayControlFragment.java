package com.rogerou.simpemusicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

/**
 * Created by Rogerou on 2016/7/12.
 * <p/>
 * 控制播放状态
 */
public class PlayControlFragment extends Fragment implements View.OnClickListener {


    public static final String RECORDING = "recording";

    public static final String PREPARE = "prepare";

    public static final String STOP = "stop";

    public static final String PAUSE = "pause";

    public static final String PLAYING = "playing";

    private ImageView iv_cover;
    private TextView tv_title;
    private TextView tv_nick_duration;
    private ImageButton ib_play;
    private RelativeLayout rl_paly_control;


    @StringDef({RECORDING, PREPARE, STOP, PAUSE, PLAYING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface STATE {

    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PLAYING:
                    play();
                    break;
                case PAUSE:
                case STOP:
                    pause();
                    pause();
                    break;

                default:
                    break;
            }
            updateController(intent);
        }
    };

    private void updateController(Intent intent) {
        final Song s = intent.getParcelableExtra("song");
        Glide.with(getContext()).load(s.getSong_album()).asBitmap().error(R.drawable.default_art).diskCacheStrategy(DiskCacheStrategy.ALL).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                iv_cover.setImageBitmap(bitmap);
                Palette.from(bitmap)
                        .generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                rl_paly_control.setBackgroundColor(palette.getDarkVibrantColor(palette.getDarkMutedColor(palette.getMutedColor(0x2bd3fa))));
                            }
                        });
            }
        });
        tv_title.setText(s.getSong_name());
        tv_nick_duration.setText(String.format(Locale.CHINESE, "%d:%d", s.getDuaration() / 60, s.getDuaration() % 60));
    }

    private void pause() {
        ib_play.setImageResource(R.drawable.ic_play_arrow_white_36dp);
    }

    private void play() {
        ib_play.setImageResource(R.drawable.ic_pause_white_36dp);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_playback_controls, container, false);

        iv_cover = (ImageView) view.findViewById(R.id.iv_cover);

        tv_title = (TextView) view.findViewById(R.id.tv_title);

        rl_paly_control = (RelativeLayout) view.findViewById(R.id.rl_play_control);

        tv_nick_duration = (TextView) view.findViewById(R.id.tv_nick_duration);
        ImageButton ib_pre = (ImageButton) view.findViewById(R.id.ib_pre);
        ib_play = (ImageButton) view.findViewById(R.id.ib_play);
        ImageButton ib_next = (ImageButton) view.findViewById(R.id.ib_next);
        ib_next.setOnClickListener(this);
        ib_play.setOnClickListener(this);
        ib_pre.setOnClickListener(this);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(PLAYING);
        filter.addAction(PAUSE);
        filter.addAction(STOP);
        getActivity().registerReceiver(mBroadcastReceiver, filter);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_pre:
                Intent intent = new Intent();
                intent.setAction(MusicService.ACTION_PREV_SONG);
                getActivity().sendBroadcast(intent);
                break;
            case R.id.ib_next:
                Intent intent1 = new Intent();
                intent1.setAction(MusicService.ACTION_NEXT_SONG);
                getActivity().sendBroadcast(intent1);

                break;
            case R.id.ib_play:
                Intent intent2 = new Intent();
                intent2.setAction(MusicService.ACTION_PAUSE_SONG);
                getActivity().sendBroadcast(intent2);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }
}
 