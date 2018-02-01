package com.iak3.android.popularmoviesapp.details;

import android.os.AsyncTask;
import android.util.Log;

import com.iak3.android.popularmoviesapp.BuildConfig;
import com.iak3.android.popularmoviesapp.network.MovieDatabaseService;
import com.iak3.android.popularmoviesapp.network.Trailer;
import com.iak3.android.popularmoviesapp.network.Trailers;

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
 * Encapsulates fetching the movie's trailers from the movie db api.
 */
public class FetchTrailersTask extends AsyncTask<Long, Void, List<Trailer>> {

    @SuppressWarnings("unused")
    public static String LOG_TAG = FetchTrailersTask.class.getSimpleName();
    private final Listener mListener;

    /**
     * Interface definition for a callback to be invoked when trailers are loaded.
     */
    interface Listener {
        void onFetchFinished(List<Trailer> trailers);
    }

    public FetchTrailersTask(Listener listener) {
        mListener = listener;
    }

    @Override
    protected List<Trailer> doInBackground(Long... params) {
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
        Call<Trailers> call = service.findTrailersById(movieId,
                THE_MOVIE_DATABASE_API_KEY);
        try {
            Response<Trailers> response = call.execute();
            Trailers trailers = response.body();
            return trailers.getTrailers();
        } catch (IOException e) {
            e(LOG_TAG, "Kesalahan pada API Movie database", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Trailer> trailers) {
        if (trailers != null) {
            mListener.onFetchFinished(trailers);
        } else {
            mListener.onFetchFinished(new ArrayList<Trailer>());
        }
    }
}
