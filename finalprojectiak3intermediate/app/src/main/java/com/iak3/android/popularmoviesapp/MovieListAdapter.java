package com.iak3.android.popularmoviesapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iak3.android.popularmoviesapp.data.MovieContract;
import com.iak3.android.popularmoviesapp.network.Movie;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.graphics.Bitmap.Config;
import static android.graphics.Bitmap.Config.RGB_565;
import static android.view.LayoutInflater.from;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static butterknife.ButterKnife.bind;
import static com.iak3.android.popularmoviesapp.R.id;
import static com.iak3.android.popularmoviesapp.R.id.thumbnail;
import static com.iak3.android.popularmoviesapp.R.id.title;
import static com.iak3.android.popularmoviesapp.R.integer;
import static com.iak3.android.popularmoviesapp.R.integer.grid_number_cols;
import static com.iak3.android.popularmoviesapp.R.layout;
import static com.iak3.android.popularmoviesapp.R.layout.movie_list_content;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COL_MOVIE_BACKDROP_PATH;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COL_MOVIE_ID;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COL_MOVIE_OVERVIEW;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COL_MOVIE_POSTER_PATH;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COL_MOVIE_RELEASE_DATE;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COL_MOVIE_TITLE;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COL_MOVIE_VOTE_AVERAGE;
import static com.iak3.android.popularmoviesapp.network.Movie.POSTER_ASPECT_RATIO;
import static com.squareup.picasso.Picasso.with;

public class MovieListAdapter
        extends RecyclerView.Adapter<MovieListAdapter.ViewHolder> {

    @SuppressWarnings("unused")
    private final static String LOG_TAG = MovieListAdapter.class.getSimpleName();

    private final ArrayList<Movie> mMovies;
    private final Callbacks mCallbacks;

    public interface Callbacks {
        void open(Movie movie, int position);
    }

    public MovieListAdapter(ArrayList<Movie> movies, Callbacks callbacks) {
        mMovies = movies;
        this.mCallbacks = callbacks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = from(parent.getContext())
                .inflate(movie_list_content, parent, false);
        final Context context = view.getContext();

        int gridColsNumber = context.getResources()
                .getInteger(grid_number_cols);

        view.getLayoutParams().height = (int) (parent.getWidth() / gridColsNumber *
                POSTER_ASPECT_RATIO);

        return new ViewHolder(view);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.cleanUp();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Movie movie = mMovies.get(position);
        final Context context = holder.mView.getContext();

        holder.mMovie = movie;
        holder.mTitleView.setText(movie.getTitle());

        String posterUrl = movie.getPosterUrl(context);
        // Warning: onError() will not be called, if url is null.
        // Empty url leads to app crash.
        if (posterUrl == null) {
            holder.mTitleView.setVisibility(VISIBLE);
        }

        with(context)
                .load(movie.getPosterUrl(context))
                .config(RGB_565)
                .into(holder.mThumbnailView,
                        new Callback() {
                            @Override
                            public void onSuccess() {
                                if (holder.mMovie.getId() != movie.getId()) {
                                    holder.cleanUp();
                                } else {
                                    holder.mThumbnailView.setVisibility(VISIBLE);
                                }
                            }

                            @Override
                            public void onError() {
                                holder.mTitleView.setVisibility(VISIBLE);
                            }
                        }
                );

        holder.mView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.open(movie, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        @Bind(thumbnail)
        ImageView mThumbnailView;
        @Bind(title)
        TextView mTitleView;
        public Movie mMovie;

        public ViewHolder(View view) {
            super(view);
            bind(this, view);
            mView = view;
        }

        public void cleanUp() {
            final Context context = mView.getContext();
            with(context).cancelRequest(mThumbnailView);
            mThumbnailView.setImageBitmap(null);
            mThumbnailView.setVisibility(INVISIBLE);
            mTitleView.setVisibility(GONE);
        }

    }

    public void add(List<Movie> movies) {
        mMovies.clear();
        mMovies.addAll(movies);
        notifyDataSetChanged();
    }

    public void add(Cursor cursor) {
        mMovies.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(COL_MOVIE_ID);
                String title = cursor.getString(COL_MOVIE_TITLE);
                String posterPath = cursor.getString(COL_MOVIE_POSTER_PATH);
                String overview = cursor.getString(COL_MOVIE_OVERVIEW);
                String rating = cursor.getString(COL_MOVIE_VOTE_AVERAGE);
                String releaseDate = cursor.getString(COL_MOVIE_RELEASE_DATE);
                String backdropPath = cursor.getString(COL_MOVIE_BACKDROP_PATH);
                Movie movie = new Movie(id, title, posterPath, overview, rating, releaseDate, backdropPath);
                mMovies.add(movie);
            } while (cursor.moveToNext());
        }
        notifyDataSetChanged();
    }

    public ArrayList<Movie> getMovies() {
        return mMovies;
    }
}
