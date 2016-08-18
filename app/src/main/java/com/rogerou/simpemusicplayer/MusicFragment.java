package com.rogerou.simpemusicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import http.APiException;
import http.HttpSubscriber;
import http.OCRetrofitFactory;
import http.SchedulersCompat;

/**
 * Created by Seven on 2016/7/12.
 * <p/>
 */
public class MusicFragment extends Fragment {

    private RecyclerView mRecyclerView;

    MusicAdapter mAdapter;

    List<SongList.ShowapiResBodyBean.PagebeanBean.SonglistBean> mSonglistBeen;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fm, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_music);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Intent intent = new Intent(getActivity(), MusicService.class);
        getActivity().startService(intent);
        mSonglistBeen = new ArrayList<>();
        mAdapter = new MusicAdapter(mSonglistBeen, getActivity());
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 3, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        initData();
    }

    private void initData() {
        OCRetrofitFactory.getOpenComApiService().getSongList("http://route.showapi.com/213-4", 5, "22131", "13268fc63d81438780eb8095f73baee4")
                .compose(SchedulersCompat.<SongList>applyIoSchedulers())
                .subscribe(new HttpSubscriber<SongList>() {
                    @Override
                    protected void onError(APiException ex) {
                        Toast.makeText(getActivity(), ex.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(SongList songList) {
                        ArrayList<Song> songs = new ArrayList<>();
                        for (SongList.ShowapiResBodyBean.PagebeanBean.SonglistBean b :
                                songList.getShowapi_res_body().getPagebean().getSonglist()) {
                            Song s = new Song();
                            s.setAuthor_name(b.getSingername());
                            s.setDuaration(b.getSeconds());
                            s.setUrl(b.getUrl());
                            s.setSong_album(b.getAlbumpic_big());
                            s.setSong_name(b.getSongname());
                            s.setSongId(songList.getShowapi_res_body().getPagebean().getColor());

                            songs.add(s);
                        }
                        Intent intent = new Intent();
                        intent.setAction(MusicService.ACTION_PLAY_ALL_SONGS);
                        intent.putParcelableArrayListExtra("songList", songs);
                        getActivity().sendBroadcast(intent);
                        mSonglistBeen.addAll(songList.getShowapi_res_body().getPagebean().getSonglist());
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    public static MusicFragment newInstance() {
        return new MusicFragment();
    }
}
