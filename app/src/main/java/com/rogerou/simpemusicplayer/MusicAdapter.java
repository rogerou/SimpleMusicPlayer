package com.rogerou.simpemusicplayer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;
import java.util.Locale;

/**
 * Created by Seven on 2016/7/12.
 * <p/>
 */

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

    final List<SongList.ShowapiResBodyBean.PagebeanBean.SonglistBean> mSonglist;

    final Context mContext;

    public MusicAdapter(List<SongList.ShowapiResBodyBean.PagebeanBean.SonglistBean> list, Context context) {
        mSonglist = list;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_fm_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SongList.ShowapiResBodyBean.PagebeanBean.SonglistBean bean = mSonglist.get(position);
        Glide.with(mContext).load(bean.getAlbumpic_big()).crossFade().diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.iv_cover);
        holder.tv_nick.setText(bean.getSingername());

        holder.tv_title.setText(bean.getSongname());

        holder.tv_duration.setText(String.format(Locale.CHINESE, "%d:%d", bean.getSeconds() / 60, bean.getSeconds() % 60));
    }


    @Override
    public int getItemCount() {
        return mSonglist.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_cover;

        TextView tv_duration;

        TextView tv_title;

        TextView tv_nick;

        public ViewHolder(View itemView) {
            super(itemView);
            iv_cover = (ImageView) itemView.findViewById(R.id.iv_cover);
            tv_duration = (TextView) itemView.findViewById(R.id.tv_duration);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            tv_nick = (TextView) itemView.findViewById(R.id.tv_nick);
        }

    }


}
