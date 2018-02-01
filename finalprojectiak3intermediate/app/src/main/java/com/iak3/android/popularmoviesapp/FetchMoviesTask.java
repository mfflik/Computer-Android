package com.iak3.android.popularmoviesapp;

import android.content.AsyncTaskLoader;
import android.os.AsyncTask;
import android.support.annotation.StringDef;
import android.util.Log;

import com.iak3.android.popularmoviesapp.network.Movie;
import com.iak3.android.popularmoviesapp.network.MovieDatabaseService;
import com.iak3.android.popularmoviesapp.network.Movies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.util.Log.e;
import static com.iak3.android.popularmoviesapp.BuildConfig.THE_MOVIE_DATABASE_API_KEY;
import static com.iak3.android.popularmoviesapp.FetchMoviesTask.Listener;
import static retrofit2.Retrofit.Builder;
import static retrofit2.converter.gson.GsonConverterFactory.create;

/**
 * Encapsulates fetching the movies list from the movie db api.
 */
public class FetchMoviesTask extends AsyncTask<Void, Void, List<Movie>> {

    @SuppressWarnings("unused")
    public static String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    public final static String MOST_POPULAR = "popular";
    public final static String TOP_RATED = "top_rated";
    public final static String FAVORITES = "favorites";

    // FetchMoviesTask cannot load favorites movies now, it's done by loaders (especially for two pane is
    // comfortable - without force updating left pane on removing/adding a favorite movie. Another
    // case when we simple returns from detail - list of favorites also will be updated, if needed).
    @StringDef({MOST_POPULAR, TOP_RATED, FAVORITES})
    public @interface SORT_BY {
    }

    /**
     * Will be called in {@link FetchMoviesTask#onPostExecute(List)} to notify subscriber to about
     * task completion.
     */
    private final NotifyAboutTaskCompletionCommand mCommand;
    private
    @SORT_BY
    String mSortBy = MOST_POPULAR;

    /**
     * Interface definition for a callback to be invoked when movies are loaded.
     */
    interface Listener {
        void onFetchFinished(Command command);
    }

    /**
     * Possible good idea to use {@link AsyncTaskLoader}, which by default is tied
     * to lifecycle method, but this approach has its own limitations
     * (i. e. publish progress is not possible in this case).
     * <p/>
     * Idea is to use AsyncTasks in combination with non-UI retained fragment and Command pattern.
     * It helps save calls which we cannot execute immediately for later.
     */
    public static class NotifyAboutTaskCompletionCommand implements Command {
        private Listener mListener;
        // The result of the task execution.
        private List<Movie> mMovies;

        public NotifyAboutTaskCompletionCommand(Listener listener) {
            mListener = listener;
        }

        @Override
        public void execute() {
            mListener.onFetchFinished(this);
        }

        public List<Movie> getMovies() {
            return mMovies;
        }
    }

    public FetchMoviesTask(@SORT_BY String sortBy, NotifyAboutTaskCompletionCommand command) {
        mCommand = command;
        mSortBy = sortBy;
    }

    @Override
    protected void onPostExecute(List<Movie> movies) {
        if (movies != null) {
            mCommand.mMovies = movies;
        } else {
            mCommand.mMovies = new ArrayList<>();
        }
        mCommand.execute();
    }

    @Override
    protected List<Movie> doInBackground(Void... params) {

        Retrofit retrofit = new Builder()
                .baseUrl("http://api.themoviedb.org/")
                .addConverterFactory(create())
                .build();

        MovieDatabaseService service = retrofit.create(MovieDatabaseService.class);
        Call<Movies> call = service.discoverMovies(mSortBy,
                THE_MOVIE_DATABASE_API_KEY);
        try {
            Response<Movies> response = call.execute();
            Movies movies = response.body();
            return movies.getMovies();

        } catch (IOException e) {
            e(LOG_TAG, "A problem occurred talking to the movie db ", e);
        }
        return null;
    }
}
