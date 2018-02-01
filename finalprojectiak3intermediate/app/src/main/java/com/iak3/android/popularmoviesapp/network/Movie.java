package com.iak3.android.popularmoviesapp.network;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.iak3.android.popularmoviesapp.R;
import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.util.Log.e;
import static com.iak3.android.popularmoviesapp.R.string;
import static com.iak3.android.popularmoviesapp.R.string.release_date_missing;
import static com.iak3.android.popularmoviesapp.R.string.url_for_downloading_backdrop;
import static com.iak3.android.popularmoviesapp.R.string.url_for_downloading_poster;
import static java.text.DateFormat.getDateInstance;
import static java.util.Locale.US;

public class Movie implements Parcelable {

    public static final String LOG_TAG = Movie.class.getSimpleName();
    public static final float POSTER_ASPECT_RATIO = 1.5f;

    @SerializedName("id")
    private long mId;
    @SerializedName("original_title")
    private String mTitle;
    @SerializedName("poster_path")
    private String mPoster;
    @SerializedName("overview")
    private String mOverview;
    @SerializedName("vote_average")
    private String mUserRating;
    @SerializedName("release_date")
    private String mReleaseDate;
    @SerializedName("backdrop_path")
    private String mBackdrop;

    // Only for createFromParcel
    private Movie() {
    }

    public Movie(long id, String title, String poster, String overview, String userRating,
                 String releaseDate, String backdrop) {
        mId = id;
        mTitle = title;
        mPoster = poster;
        mOverview = overview;
        mUserRating = userRating;
        mReleaseDate = releaseDate;
        mBackdrop = backdrop;
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    public long getId() {
        return mId;
    }

    @Nullable
    public String getPosterUrl(Context context) {
        if (mPoster != null && !mPoster.isEmpty()) {
            return context.getResources().getString(url_for_downloading_poster) + mPoster;
        }
        // IllegalArgumentException: Path must not be empty. at com.squareup.picasso.Picasso.load.
        // Placeholder/Error/Title will be shown instead of a crash.
        return null;
    }

    public String getPoster() {
        return mPoster;
    }

    public String getReleaseDate(Context context) {
        String inputPattern = "yyyy-MM-dd";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, US);
        if (mReleaseDate != null && !mReleaseDate.isEmpty()) {
            try {
                Date date = inputFormat.parse(mReleaseDate);
                return getDateInstance().format(date);
            } catch (ParseException e) {
                e(LOG_TAG, "The Release data was not parsed successfully: " + mReleaseDate);
                // Return not formatted date
            }
        } else {
            mReleaseDate = context.getString(release_date_missing);
        }

        return mReleaseDate;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    @Nullable
    public String getOverview() {
        return mOverview;
    }

    @Nullable
    public String getUserRating() {
        return mUserRating;
    }

    @Nullable
    public String getBackdropUrl(Context context) {
        if (mBackdrop != null && !mBackdrop.isEmpty()) {
            return context.getResources().getString(url_for_downloading_backdrop) +
                    mBackdrop;
        }
        // Placeholder/Error/Title will be shown instead of a crash.
        return null;
    }

    public String getBackdrop() {
        return mBackdrop;
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        public Movie createFromParcel(Parcel source) {
            Movie movie = new Movie();
            movie.mId = source.readLong();
            movie.mTitle = source.readString();
            movie.mPoster = source.readString();
            movie.mOverview = source.readString();
            movie.mUserRating = source.readString();
            movie.mReleaseDate = source.readString();
            movie.mBackdrop = source.readString();
            return movie;
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(mId);
        parcel.writeString(mTitle);
        parcel.writeString(mPoster);
        parcel.writeString(mOverview);
        parcel.writeString(mUserRating);
        parcel.writeString(mReleaseDate);
        parcel.writeString(mBackdrop);
    }
}
