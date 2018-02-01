package com.iak3.android.popularmoviesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.iak3.android.popularmoviesapp.data.MovieContract;
import com.iak3.android.popularmoviesapp.network.Movie;
import com.iak3.android.popularmoviesapp.details.MovieDetailActivity;
import com.iak3.android.popularmoviesapp.details.MovieDetailFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static butterknife.ButterKnife.bind;
import static com.iak3.android.popularmoviesapp.FetchMoviesTask.FAVORITES;
import static com.iak3.android.popularmoviesapp.FetchMoviesTask.Listener;
import static com.iak3.android.popularmoviesapp.FetchMoviesTask.MOST_POPULAR;
import static com.iak3.android.popularmoviesapp.FetchMoviesTask.NotifyAboutTaskCompletionCommand;
import static com.iak3.android.popularmoviesapp.FetchMoviesTask.TOP_RATED;
import static com.iak3.android.popularmoviesapp.MovieListAdapter.Callbacks;
import static com.iak3.android.popularmoviesapp.R.id;
import static com.iak3.android.popularmoviesapp.R.id.empty_state_container;
import static com.iak3.android.popularmoviesapp.R.id.empty_state_favorites_container;
import static com.iak3.android.popularmoviesapp.R.id.movie_detail_container;
import static com.iak3.android.popularmoviesapp.R.id.movie_list;
import static com.iak3.android.popularmoviesapp.R.id.progress;
import static com.iak3.android.popularmoviesapp.R.id.sort_by_favorites;
import static com.iak3.android.popularmoviesapp.R.id.sort_by_most_popular;
import static com.iak3.android.popularmoviesapp.R.id.sort_by_top_rated;
import static com.iak3.android.popularmoviesapp.R.id.toolbar;
import static com.iak3.android.popularmoviesapp.R.integer;
import static com.iak3.android.popularmoviesapp.R.integer.grid_number_cols;
import static com.iak3.android.popularmoviesapp.R.layout;
import static com.iak3.android.popularmoviesapp.R.layout.activity_movie_list;
import static com.iak3.android.popularmoviesapp.R.menu;
import static com.iak3.android.popularmoviesapp.R.string;
import static com.iak3.android.popularmoviesapp.R.string.title_movie_list;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.CONTENT_URI;
import static com.iak3.android.popularmoviesapp.data.MovieContract.MovieEntry.MOVIE_COLUMNS;
import static com.iak3.android.popularmoviesapp.details.MovieDetailFragment.ARG_MOVIE;

/**
 * An activity representing a grid of Movies. This activity
 * has different presentations for handset and tablet-size devices.
 */
public class MovieListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        Listener, Callbacks {

    private static final String EXTRA_MOVIES = "EXTRA_MOVIES";
    private static final String EXTRA_SORT_BY = "EXTRA_SORT_BY";
    private static final int FAVORITE_MOVIES_LOADER = 0;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private RetainedFragment mRetainedFragment;
    private MovieListAdapter mAdapter;
    private String mSortBy = MOST_POPULAR;

    @Bind(movie_list)
    RecyclerView mRecyclerView;
    @Bind(toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_movie_list);
        bind(this);

        mToolbar.setTitle(title_movie_list);
        setSupportActionBar(mToolbar);

        String tag = RetainedFragment.class.getName();
        this.mRetainedFragment = (RetainedFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (this.mRetainedFragment == null) {
            this.mRetainedFragment = new RetainedFragment();
            getSupportFragmentManager().beginTransaction().add(this.mRetainedFragment, tag).commit();
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, getResources()
                .getInteger(grid_number_cols)));
        // To avoid "E/RecyclerView: No adapter attached; skipping layout"
        mAdapter = new MovieListAdapter(new ArrayList<Movie>(), this);
        mRecyclerView.setAdapter(mAdapter);

        // For large-screen layouts (res/values-w900dp).
        mTwoPane = findViewById(movie_detail_container) != null;

        if (savedInstanceState != null) {
            mSortBy = savedInstanceState.getString(EXTRA_SORT_BY);
            if (savedInstanceState.containsKey(EXTRA_MOVIES)) {
                List<Movie> movies = savedInstanceState.getParcelableArrayList(EXTRA_MOVIES);
                mAdapter.add(movies);
                findViewById(progress).setVisibility(GONE);

                // For listening content updates for tow pane mode
                if (mSortBy.equals(FAVORITES)) {
                    getSupportLoaderManager().initLoader(FAVORITE_MOVIES_LOADER, null, this);
                }
            }
            updateEmptyState();
        } else {
            // Fetch Movies only if savedInstanceState == null
            fetchMovies(mSortBy);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Movie> movies = mAdapter.getMovies();
        if (movies != null && !movies.isEmpty()) {
            outState.putParcelableArrayList(EXTRA_MOVIES, movies);
        }
        outState.putString(EXTRA_SORT_BY, mSortBy);

        // Needed to avoid confusion, when we back from detail screen (i. e. top rated selected but
        // favorite movies are shown and onCreate was not called in this case).
        if (!mSortBy.equals(FAVORITES)) {
            getSupportLoaderManager().destroyLoader(FAVORITE_MOVIES_LOADER);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(menu.movie_list_activity, menu);

        switch (mSortBy) {
            case MOST_POPULAR:
                menu.findItem(sort_by_most_popular).setChecked(true);
                break;
            case TOP_RATED:
                menu.findItem(sort_by_top_rated).setChecked(true);
                break;
            case FAVORITES:
                menu.findItem(sort_by_favorites).setChecked(true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case sort_by_top_rated:
                if (mSortBy.equals(FAVORITES)) {
                    getSupportLoaderManager().destroyLoader(FAVORITE_MOVIES_LOADER);
                }
                mSortBy = TOP_RATED;
                fetchMovies(mSortBy);
                item.setChecked(true);
                break;
            case sort_by_most_popular:
                if (mSortBy.equals(FAVORITES)) {
                    getSupportLoaderManager().destroyLoader(FAVORITE_MOVIES_LOADER);
                }
                mSortBy = MOST_POPULAR;
                fetchMovies(mSortBy);
                item.setChecked(true);
                break;
            case sort_by_favorites:
                mSortBy = FAVORITES;
                item.setChecked(true);
                fetchMovies(mSortBy);
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void open(Movie movie, int position) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(ARG_MOVIE, movie);
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(movie_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(ARG_MOVIE, movie);
            startActivity(intent);
        }
    }

    @Override
    public void onFetchFinished(Command command) {
        if (command instanceof NotifyAboutTaskCompletionCommand) {
            mAdapter.add(((NotifyAboutTaskCompletionCommand) command).getMovies());
            updateEmptyState();
            findViewById(progress).setVisibility(GONE);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.add(cursor);
        updateEmptyState();
        findViewById(progress).setVisibility(GONE);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        findViewById(progress).setVisibility(VISIBLE);
        return new CursorLoader(this,
                CONTENT_URI,
                MOVIE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // Not used
    }

    private void fetchMovies(String sortBy) {
        if (!sortBy.equals(FAVORITES)) {
            findViewById(progress).setVisibility(VISIBLE);
            NotifyAboutTaskCompletionCommand command =
                    new NotifyAboutTaskCompletionCommand(this.mRetainedFragment);
            new FetchMoviesTask(sortBy, command).execute();
        } else {
            getSupportLoaderManager().initLoader(FAVORITE_MOVIES_LOADER, null, this);
        }
    }

    private void updateEmptyState() {
        if (mAdapter.getItemCount() == 0) {
            if (mSortBy.equals(FAVORITES)) {
                findViewById(empty_state_container).setVisibility(GONE);
                findViewById(empty_state_favorites_container).setVisibility(VISIBLE);
            } else {
                findViewById(empty_state_container).setVisibility(VISIBLE);
                findViewById(empty_state_favorites_container).setVisibility(GONE);
            }
        } else {
            findViewById(empty_state_container).setVisibility(GONE);
            findViewById(empty_state_favorites_container).setVisibility(GONE);
        }
    }

    /**
     * RetainedFragment with saving state mechanism.
     * The saving state mechanism helps to not lose user's progress even when app is in the
     * background state or user rotate device and also to avoid performing code which
     * will lead to "java.lang.IllegalStateException: Can not perform some actions after
     * onSaveInstanceState". As the result we have commands which we cannot execute now,
     * but we have to store it and execute later.
     *
     * @see NotifyAboutTaskCompletionCommand
     */
    public static class RetainedFragment extends Fragment implements Listener {

        private boolean mPaused = false;
        // Currently allow to wait one command, because more is not needed. In future it can be
        // extended to list etc. Using "MacroCommand" which contain includes other commands as waiting command.
        private Command mWaitingCommand = null;

        public RetainedFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        @Override
        public void onPause() {
            super.onPause();
            mPaused = true;
        }

        @Override
        public void onResume() {
            super.onResume();
            mPaused = false;
            if (mWaitingCommand != null) {
                onFetchFinished(mWaitingCommand);
            }
        }

        @Override
        public void onFetchFinished(Command command) {
            if (getActivity() instanceof Listener && !mPaused) {
                Listener listener = (Listener) getActivity();
                listener.onFetchFinished(command);
                mWaitingCommand = null;
            } else {
                // Save the command for later.
                mWaitingCommand = command;
            }
        }
    }
}
