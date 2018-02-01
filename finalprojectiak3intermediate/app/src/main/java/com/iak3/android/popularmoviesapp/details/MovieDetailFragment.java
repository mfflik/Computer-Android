package com.iak3.android.popularmoviesapp.details;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.iak3.android.popularmoviesapp.data.MovieContract;
import com.iak3.android.popularmoviesapp.network.Movie;
import com.iak3.android.popularmoviesapp.MovieListActivity;
import com.iak3.android.popularmoviesapp.R;
import com.iak3.android.popularmoviesapp.network.Review;
import com.iak3.android.popularmoviesapp.network.Trailer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.EXTRA_SUBJECT;
import static android.content.Intent.EXTRA_TEXT;
import static android.graphics.Bitmap.Config;
import static android.graphics.Bitmap.Config.RGB_565;
import static android.net.Uri.parse;
import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static android.support.v4.view.MenuItemCompat.getActionProvider;
import static android.support.v7.widget.LinearLayoutManager.HORIZONTAL;
import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static butterknife.ButterKnife.bind;
import static com.iak3.android.popularmoviesapp.R.drawable;
import static com.iak3.android.popularmoviesapp.R.drawable.ic_star_black_24dp;
import static com.iak3.android.popularmoviesapp.R.drawable.ic_star_half_black_24dp;
import static com.iak3.android.popularmoviesapp.R.id;
import static com.iak3.android.popularmoviesapp.R.id.button_mark_as_favorite;
import static com.iak3.android.popularmoviesapp.R.id.button_remove_from_favorites;
import static com.iak3.android.popularmoviesapp.R.id.button_watch_trailer;
import static com.iak3.android.popularmoviesapp.R.id.movie_backdrop;
import static com.iak3.android.popularmoviesapp.R.id.movie_overview;
import static com.iak3.android.popularmoviesapp.R.id.movie_poster;
import static com.iak3.android.popularmoviesapp.R.id.movie_release_date;
import static com.iak3.android.popularmoviesapp.R.id.movie_title;
import static com.iak3.android.popularmoviesapp.R.id.movie_user_rating;
import static com.iak3.android.popularmoviesapp.R.id.rating_fifth_star;
import static com.iak3.android.popularmoviesapp.R.id.rating_first_star;
import static com.iak3.android.popularmoviesapp.R.id.rating_fourth_star;
import static com.iak3.android.popularmoviesapp.R.id.rating_second_star;
import static com.iak3.android.popularmoviesapp.R.id.rating_third_star;
import static com.iak3.android.popularmoviesapp.R.id.review_list;
import static com.iak3.android.popularmoviesapp.R.id.share_trailer;
import static com.iak3.android.popularmoviesapp.R.id.toolbar_layout;
import static com.iak3.android.popularmoviesapp.R.id.trailer_list;
import static com.iak3.android.popularmoviesapp.R.layout;
import static com.iak3.android.popularmoviesapp.R.layout.movie_detail;
import static com.iak3.android.popularmoviesapp.R.menu;
import static com.iak3.android.popularmoviesapp.R.string;
import static com.iak3.android.popularmoviesapp.R.string.user_rating_movie;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_BACKDROP_PATH;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_ID;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_TITLE;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.CONTENT_URI;
import static com.iak3.android.popularmoviesapp.details.FetchTrailersTask.Listener;
import static com.iak3.android.popularmoviesapp.details.TrailerListAdapter.Callbacks;
import static com.squareup.picasso.Picasso.with;
import static java.lang.Float.valueOf;
import static java.lang.Math.round;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment implements Listener,
        Callbacks, FetchReviewsTask.Listener, ReviewListAdapter.Callbacks {

    @SuppressWarnings("unused")
    public static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    /**
     * The fragment argument representing the movie that this fragment
     * represents.
     */
    public static final String ARG_MOVIE = "ARG_MOVIE";
    public static final String EXTRA_TRAILERS = "EXTRA_TRAILERS";
    public static final String EXTRA_REVIEWS = "EXTRA_REVIEWS";

    private Movie mMovie;
    private TrailerListAdapter mTrailerListAdapter;
    private ReviewListAdapter mReviewListAdapter;
    private ShareActionProvider mShareActionProvider;

    @Bind(trailer_list)
    RecyclerView mRecyclerViewForTrailers;
    @Bind(review_list)
    RecyclerView mRecyclerViewForReviews;

    @Bind(movie_title)
    TextView mMovieTitleView;
    @Bind(movie_overview)
    TextView mMovieOverviewView;
    @Bind(movie_release_date)
    TextView mMovieReleaseDateView;
    @Bind(movie_user_rating)
    TextView mMovieRatingView;
    @Bind(movie_poster)
    ImageView mMoviePosterView;

    @Bind(button_watch_trailer)
    Button mButtonWatchTrailer;
    @Bind(button_mark_as_favorite)
    Button mButtonMarkAsFavorite;
    @Bind(button_remove_from_favorites)
    Button mButtonRemoveFromFavorites;

    @Bind({rating_first_star, rating_second_star, rating_third_star,
            rating_fourth_star, rating_fifth_star})
    List<ImageView> ratingStarViews;

    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_MOVIE)) {
            mMovie = getArguments().getParcelable(ARG_MOVIE);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout)
                activity.findViewById(toolbar_layout);
        if (appBarLayout != null && activity instanceof MovieDetailActivity) {
            appBarLayout.setTitle(mMovie.getTitle());
        }

        ImageView movieBackdrop = ((ImageView) activity.findViewById(movie_backdrop));
        if (movieBackdrop != null) {
            with(activity)
                    .load(mMovie.getBackdropUrl(getContext()))
                    .config(RGB_565)
                    .into(movieBackdrop);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(movie_detail, container, false);
        bind(this, rootView);

        mMovieTitleView.setText(mMovie.getTitle());
        mMovieOverviewView.setText(mMovie.getOverview());
        mMovieReleaseDateView.setText(mMovie.getReleaseDate(getContext()));

        with(getContext())
                .load(mMovie.getPosterUrl(getContext()))
                .config(RGB_565)
                .into(mMoviePosterView);

        updateRatingBar();
        updateFavoriteButtons();

        // For horizontal list of trailers
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), HORIZONTAL, false);
        mRecyclerViewForTrailers.setLayoutManager(layoutManager);
        mTrailerListAdapter = new TrailerListAdapter(new ArrayList<Trailer>(), this);
        mRecyclerViewForTrailers.setAdapter(mTrailerListAdapter);
        mRecyclerViewForTrailers.setNestedScrollingEnabled(false);

        // For vertical list of reviews
        mReviewListAdapter = new ReviewListAdapter(new ArrayList<Review>(), this);
        mRecyclerViewForReviews.setAdapter(mReviewListAdapter);

        // Fetch trailers only if savedInstanceState == null
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_TRAILERS)) {
            List<Trailer> trailers = savedInstanceState.getParcelableArrayList(EXTRA_TRAILERS);
            mTrailerListAdapter.add(trailers);
            mButtonWatchTrailer.setEnabled(true);
        } else {
            fetchTrailers();
        }

        // Fetch reviews only if savedInstanceState == null
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_REVIEWS)) {
            List<Review> reviews = savedInstanceState.getParcelableArrayList(EXTRA_REVIEWS);
            mReviewListAdapter.add(reviews);
        } else {
            fetchReviews();
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Trailer> trailers = mTrailerListAdapter.getTrailers();
        if (trailers != null && !trailers.isEmpty()) {
            outState.putParcelableArrayList(EXTRA_TRAILERS, trailers);
        }

        ArrayList<Review> reviews = mReviewListAdapter.getReviews();
        if (reviews != null && !reviews.isEmpty()) {
            outState.putParcelableArrayList(EXTRA_REVIEWS, reviews);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(menu.movie_detail_fragment, menu);
        MenuItem shareTrailerMenuItem = menu.findItem(share_trailer);
        mShareActionProvider = (ShareActionProvider) getActionProvider(shareTrailerMenuItem);
    }

    @Override
    public void watch(Trailer trailer, int position) {
        startActivity(new Intent(ACTION_VIEW, parse(trailer.getTrailerUrl())));
    }

    @Override
    public void read(Review review, int position) {
        startActivity(new Intent(ACTION_VIEW,
                parse(review.getUrl())));
    }

    @Override
    public void onFetchFinished(List<Trailer> trailers) {
        mTrailerListAdapter.add(trailers);
        mButtonWatchTrailer.setEnabled(!trailers.isEmpty());

        if (mTrailerListAdapter.getItemCount() > 0) {
            Trailer trailer = mTrailerListAdapter.getTrailers().get(0);
            updateShareActionProvider(trailer);
        }
    }

    @Override
    public void onReviewsFetchFinished(List<Review> reviews) {
        mReviewListAdapter.add(reviews);
    }

    private void fetchTrailers() {
        FetchTrailersTask task = new FetchTrailersTask(this);
        task.executeOnExecutor(THREAD_POOL_EXECUTOR, mMovie.getId());
    }

    private void fetchReviews() {
        FetchReviewsTask task = new FetchReviewsTask(this);
        task.executeOnExecutor(THREAD_POOL_EXECUTOR, mMovie.getId());
    }

    public void markAsFavorite() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (!isFavorite()) {
                    ContentValues movieValues = new ContentValues();
                    movieValues.put(COLUMN_MOVIE_ID,
                            mMovie.getId());
                    movieValues.put(COLUMN_MOVIE_TITLE,
                            mMovie.getTitle());
                    movieValues.put(COLUMN_MOVIE_POSTER_PATH,
                            mMovie.getPoster());
                    movieValues.put(COLUMN_MOVIE_OVERVIEW,
                            mMovie.getOverview());
                    movieValues.put(COLUMN_MOVIE_VOTE_AVERAGE,
                            mMovie.getUserRating());
                    movieValues.put(COLUMN_MOVIE_RELEASE_DATE,
                            mMovie.getReleaseDate());
                    movieValues.put(COLUMN_MOVIE_BACKDROP_PATH,
                            mMovie.getBackdrop());
                    getContext().getContentResolver().insert(
                            CONTENT_URI,
                            movieValues
                    );
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updateFavoriteButtons();
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    public void removeFromFavorites() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (isFavorite()) {
                    getContext().getContentResolver().delete(CONTENT_URI,
                            COLUMN_MOVIE_ID + " = " + mMovie.getId(), null);

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updateFavoriteButtons();
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    private void updateRatingBar() {
        if (mMovie.getUserRating() != null && !mMovie.getUserRating().isEmpty()) {
            String userRatingStr = getResources().getString(user_rating_movie,
                    mMovie.getUserRating());
            mMovieRatingView.setText(userRatingStr);

            float userRating = valueOf(mMovie.getUserRating()) / 2;
            int integerPart = (int) userRating;

            // Fill stars
            for (int i = 0; i < integerPart; i++) {
                ratingStarViews.get(i).setImageResource(ic_star_black_24dp);
            }

            // Fill half star
            if (round(userRating) > integerPart) {
                ratingStarViews.get(integerPart).setImageResource(
                        ic_star_half_black_24dp);
            }

        } else {
            mMovieRatingView.setVisibility(GONE);
        }
    }

    private void updateFavoriteButtons() {
        // Needed to avoid "skip frames".
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                return isFavorite();
            }

            @Override
            protected void onPostExecute(Boolean isFavorite) {
                if (isFavorite) {
                    mButtonRemoveFromFavorites.setVisibility(VISIBLE);
                    mButtonMarkAsFavorite.setVisibility(GONE);
                } else {
                    mButtonMarkAsFavorite.setVisibility(VISIBLE);
                    mButtonRemoveFromFavorites.setVisibility(GONE);
                }
            }
        }.executeOnExecutor(THREAD_POOL_EXECUTOR);

        mButtonMarkAsFavorite.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        markAsFavorite();
                    }
                });

        mButtonWatchTrailer.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mTrailerListAdapter.getItemCount() > 0) {
                            watch(mTrailerListAdapter.getTrailers().get(0), 0);
                        }
                    }
                });

        mButtonRemoveFromFavorites.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeFromFavorites();
                    }
                });
    }

    private boolean isFavorite() {
        Cursor movieCursor = getContext().getContentResolver().query(
                CONTENT_URI,
                new String[]{COLUMN_MOVIE_ID},
                COLUMN_MOVIE_ID + " = " + mMovie.getId(),
                null,
                null);

        if (movieCursor != null && movieCursor.moveToFirst()) {
            movieCursor.close();
            return true;
        } else {
            return false;
        }
    }

    private void updateShareActionProvider(Trailer trailer) {
        Intent sharingIntent = new Intent(ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(EXTRA_SUBJECT, mMovie.getTitle());
        sharingIntent.putExtra(EXTRA_TEXT, trailer.getName() + ": "
                + trailer.getTrailerUrl());
        mShareActionProvider.setShareIntent(sharingIntent);
    }
}
