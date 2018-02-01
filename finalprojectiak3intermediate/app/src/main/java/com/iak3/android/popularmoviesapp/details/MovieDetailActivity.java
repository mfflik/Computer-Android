package com.iak3.android.popularmoviesapp.details;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.iak3.android.popularmoviesapp.MovieListActivity;
import com.iak3.android.popularmoviesapp.R;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.R.id.home;
import static android.graphics.Color.TRANSPARENT;
import static android.os.Build.VERSION;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static butterknife.ButterKnife.bind;
import static com.iak3.android.popularmoviesapp.R.id;
import static com.iak3.android.popularmoviesapp.R.id.detail_toolbar;
import static com.iak3.android.popularmoviesapp.R.id.movie_detail_container;
import static com.iak3.android.popularmoviesapp.R.layout;
import static com.iak3.android.popularmoviesapp.R.layout.activity_movie_detail;
import static com.iak3.android.popularmoviesapp.details.MovieDetailFragment.ARG_MOVIE;

/**
 * An activity representing a single Movie detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MovieListActivity}.
 */
public class MovieDetailActivity extends AppCompatActivity {

    @Bind(detail_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_movie_detail);
        bind(this);

        setSupportActionBar(mToolbar);

        if (SDK_INT >= LOLLIPOP) {
            getWindow().setStatusBarColor(TRANSPARENT);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(ARG_MOVIE,
                    getIntent().getParcelableExtra(ARG_MOVIE));
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(movie_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
