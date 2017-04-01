package com.example.eric.mymovies.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eric.mymovies.R;
import com.example.eric.mymovies.models.Movie;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by eric on 1/4/17.
 */

class MovieSearchAdapter extends RecyclerView.Adapter<MovieSearchAdapter.MovieViewHolder> {
    private List<Movie> mMovies = new ArrayList<>();
    private String mImageSize;
    private String mBaseImageUrl;

    public MovieSearchAdapter() {
    }

    public void add(ArrayList<Movie> movies) {
        int oldSize = mMovies.size();
        mMovies.addAll(movies);
        notifyItemRangeChanged(oldSize, movies.size());
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.list_item_movie, parent, false);
        return new MovieViewHolder(item);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        Movie item = mMovies.get(position);
        String imageSrc = null;
        if (!TextUtils.isEmpty(mImageSize) && !TextUtils.isEmpty(mBaseImageUrl)) {
            imageSrc = String.format("%s%s%s", mBaseImageUrl, mImageSize, item.getPosterPath());
        }
        if (!TextUtils.isEmpty(imageSrc)) {
            Picasso.with(holder.itemView.getContext()).load(imageSrc).into(holder.imageV);
        }
        holder.titleV.setText(item.getTitle());
        if (!TextUtils.isEmpty(item.getOverview())) {
            holder.descV.setText(item.getOverview());
        }
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    public void setImageConfig(String baseUrl, String imageSize) {
        this.mImageSize = imageSize;
        this.mBaseImageUrl = baseUrl;
        notifyDataSetChanged();
    }

    public void clear() {
        mMovies = new ArrayList<>();
        notifyDataSetChanged();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_title)
        TextView titleV;
        @BindView(R.id.image_poster)
        ImageView imageV;
        @BindView(R.id.text_desc)
        TextView descV;

        MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}