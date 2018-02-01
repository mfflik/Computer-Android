package com.iak3.android.popularmoviesapp.details;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.iak3.android.popularmoviesapp.R;
import com.iak3.android.popularmoviesapp.network.Trailer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.graphics.Bitmap.Config;
import static android.graphics.Bitmap.Config.RGB_565;
import static android.util.Log.i;
import static android.view.LayoutInflater.from;
import static android.view.View.OnClickListener;
import static butterknife.ButterKnife.bind;
import static com.iak3.android.popularmoviesapp.R.dimen;
import static com.iak3.android.popularmoviesapp.R.dimen.detail_horizontal_padding;
import static com.iak3.android.popularmoviesapp.R.id;
import static com.iak3.android.popularmoviesapp.R.id.trailer_thumbnail;
import static com.iak3.android.popularmoviesapp.R.layout;
import static com.iak3.android.popularmoviesapp.R.layout.trailer_list_content;
import static com.squareup.picasso.Picasso.with;

public class TrailerListAdapter extends RecyclerView.Adapter<TrailerListAdapter.ViewHolder> {

    @SuppressWarnings("unused")
    private final static String LOG_TAG = TrailerListAdapter.class.getSimpleName();

    private final ArrayList<Trailer> mTrailers;
    private final Callbacks mCallbacks;

    public interface Callbacks {
        void watch(Trailer trailer, int position);
    }

    public TrailerListAdapter(ArrayList<Trailer> trailers, Callbacks callbacks) {
        mTrailers = trailers;
        mCallbacks = callbacks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = from(parent.getContext())
                .inflate(trailer_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Trailer trailer = mTrailers.get(position);
        final Context context = holder.mView.getContext();

        float paddingLeft = 0;
        if (position == 0) {
            paddingLeft = context.getResources().getDimension(detail_horizontal_padding);
        }

        float paddingRight = 0;
        if (position + 1 != getItemCount()) {
            paddingRight = context.getResources().getDimension(detail_horizontal_padding) / 2;
        }

        holder.mView.setPadding((int) paddingLeft, 0, (int) paddingRight, 0);

        holder.mTrailer = trailer;

        String thumbnailUrl = "http://img.youtube.com/vi/" + trailer.getKey() + "/0.jpg";
        i(LOG_TAG, "thumbnailUrl -> " + thumbnailUrl);

        with(context)
                .load(thumbnailUrl)
                .config(RGB_565)
                .into(holder.mThumbnailView);

        holder.mView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.watch(trailer, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTrailers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        @Bind(trailer_thumbnail)
        ImageView mThumbnailView;
        public Trailer mTrailer;

        public ViewHolder(View view) {
            super(view);
            bind(this, view);
            mView = view;
        }
    }

    public void add(List<Trailer> trailers) {
        mTrailers.clear();
        mTrailers.addAll(trailers);
        notifyDataSetChanged();
    }

    public ArrayList<Trailer> getTrailers() {
        return mTrailers;
    }
}

