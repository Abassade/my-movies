package com.example.eric.mymovies.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.eric.mymovies.MyEndlessRVScrollListener;
import com.example.eric.mymovies.R;
import com.example.eric.mymovies.Webservices.ConfigurationService;
import com.example.eric.mymovies.Webservices.MovieService;
import com.example.eric.mymovies.models.ConfigurationResponse;
import com.example.eric.mymovies.models.ImageOptions;
import com.example.eric.mymovies.models.Movie;
import com.example.eric.mymovies.models.MoviesResponse;
import com.example.eric.mymovies.utils.MyJsonResponseUtils;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by eric on 1/4/17.
 */

public class MovieSearchFragment extends Fragment implements MyEndlessRVScrollListener.OnScrollEndListener {
    private static final String ARG_QUERY = "ARG_QUERY";

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    private String mQuery = "big";
    private MyEndlessRVScrollListener mEndlessScrollListener;
    private int mCurrentPage = 1;
    private int mTotalPageCount;
    private MovieSearchAdapter mAdapter;
    private String mListItemImageSize;
    private MovieSearchFragmentCallback mListener;

    public static MovieSearchFragment newInstance(String query) {

        Bundle args = new Bundle();
        args.putString(ARG_QUERY, query);

        MovieSearchFragment fragment = new MovieSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String tmp = getArguments().getString(ARG_QUERY);
        if (!TextUtils.isEmpty(tmp)) mQuery = tmp;
        mAdapter = new MovieSearchAdapter();
        fetchConfiguration();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_search, container, false);
        ButterKnife.bind(this, view);
        initViews();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MovieSearchFragmentCallback) {
            mListener = (MovieSearchFragmentCallback) context;
        }
    }

    private void fetchConfiguration() {
        Call<ConfigurationResponse> call = ConfigurationService.service.fetchConfiguration();
        call.enqueue(new Callback<ConfigurationResponse>() {
            @Override
            public void onResponse(Call<ConfigurationResponse> call,
                                   Response<ConfigurationResponse> response) {
                onFetchedConfiguration(response);
            }

            @Override
            public void onFailure(Call<ConfigurationResponse> call, Throwable t) {
                Logger.e( "onFailure", t);
                onErrorConfiguration();
            }
        });
    }

    private void onFetchedConfiguration(Response<ConfigurationResponse> response) {
        ImageOptions options = response.body().getImages();
        // Second smallest poster size is optimal here, now it's 154px, even if it changes, should be around there
        mListItemImageSize = options.getPosterSizes().get(2);
        mAdapter.setImageConfig(options.getBaseUrl(), mListItemImageSize);
    }

    private void initViews() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        mEndlessScrollListener = new MyEndlessRVScrollListener(layoutManager, this);
        recyclerView.addOnScrollListener(mEndlessScrollListener);
        recyclerView.setAdapter(mAdapter);
    }

    private void fetchMovies() {
        Logger.i("fetchMovies");
        Call<MoviesResponse> call = MovieService.service.searchMovies(mQuery, mCurrentPage);
        call.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call,
                                   Response<MoviesResponse> response) {
                onFetchedMovies(response);
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Logger.e( "onFailure", t);
                onErrorMovies();
            }
        });
    }

    private void onErrorConfiguration() {
    }

    private void onFetchedMovies(Response<MoviesResponse> response) {
        Logger.i("onFetchedMovies");
        setLoading(false);
        if (mListener != null) mListener.onFetchedMovies();
        if (response.isSuccessful()) {
            MoviesResponse tmp = response.body();
            ArrayList<Movie> movies = tmp.getResults();
            mCurrentPage = tmp.getPage();
            mTotalPageCount = tmp.getTotalPages();
            render(movies);
        } else {
            String errorMessage = MyJsonResponseUtils.extractErrMsg(response.errorBody());
            onErrorMovies(errorMessage);
        }
    }

    private void render(ArrayList<Movie> movies) {
        mAdapter.add(movies);
    }

    private void onErrorMovies(String errorMessage) {
        Logger.w("onErrorMovies %s", errorMessage);
    }

    private void onErrorMovies() {
        Logger.w("onErrorMovies");
    }

    /**
     * Fetch the next page of movies immediately for smoother scrolling
     */
    @Override
    public void onScrollEnd() {
        Logger.i("onScrollEnd");
        if (mCurrentPage < mTotalPageCount) {
            mCurrentPage++;
            Logger.i("fetching movies page number %d", mCurrentPage);
            fetchMovies();
        } else {
            recyclerView.removeOnScrollListener(mEndlessScrollListener);
        }
    }

    void setLoading(boolean b) {
        Logger.i("setLoading " + b);
        if (mEndlessScrollListener != null) {
            mEndlessScrollListener.setLoading(b);
        }
    }

    @Override
    public void onDestroyView() {
        recyclerView.removeOnScrollListener(mEndlessScrollListener);
        super.onDestroyView();
    }

    public void search(String query) {
        this.mQuery = query;
        mCurrentPage = 1;
        mAdapter.clear();
        fetchMovies();
    }

    public interface MovieSearchFragmentCallback {
        void onFetchedMovies();
    }
}