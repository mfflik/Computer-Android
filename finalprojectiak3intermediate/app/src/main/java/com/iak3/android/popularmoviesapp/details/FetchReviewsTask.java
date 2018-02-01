package com.iak3.android.popularmoviesapp.details;

import android.os.AsyncTask;
import android.util.Log;

import com.iak3.android.popularmoviesapp.BuildConfig;
import com.iak3.android.popularmoviesapp.network.MovieDatabaseService;
import com.iak3.android.popularmoviesapp.network.Review;
import com.iak3.android.popularmoviesapp.network.Reviews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.util.Log.e;
import static com.iak3.android.popularmoviesapp.BuildConfig.THE_MOVIE_DATABASE_API_KEY;
import static retrofit2.Retrofit.Builder;
import static retrofit2.converter.gson.GsonConverterFactory.create;

/**
 * Encapsulates fetching the movie's reviews from the movie db api.
 */
public class FetchReviewsTask extends AsyncTask<Long, Void, List<Review>> {

    @SuppressWarnings("unused")
    public static String LOG_TAG = FetchReviewsTask.class.getSimpleName();
    private final Listener mListener;

    /**
     * Interface definition for a callback to be invoked when reviews are loaded.
     */
    interface Listener {
        void onReviewsFetchFinished(List<Review> reviews);
    }

    public FetchReviewsTask(Listener listener) {
        mListener = listener;
    }

    @Override
    protected List<Review> doInBackground(Long... params) {
        // If there's no movie id, there's nothing to look up.
        if (params.length == 0) {
            return null;
        }
        long movieId = params[0];

        Retrofit retrofit = new Builder()
                .baseUrl("http://api.themoviedb.org/")
                .addConverterFactory(create())
                .build();

        MovieDatabaseService service = retrofit.create(MovieDatabaseService.class);
        Call<Reviews> call = service.findReviewsById(movieId,
                THE_MOVIE_DATABASE_API_KEY);
        try {
            Response<Reviews> response = call.execute();
            Reviews reviews = response.body();
            return reviews.getReviews();
        } catch (IOException e) {
            e(LOG_TAG, "Kesalahan pada API Movie database", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Review> reviews) {
        if (reviews != null) {
            mListener.onReviewsFetchFinished(reviews);
        } else {
            mListener.onReviewsFetchFinished(new ArrayList<Review>());
        }
    }
}
