package com.pingpong.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.target.Target;
import com.github.islamkhsh.CardSliderAdapter;
import com.pingpong.DetailsActivity;
import com.pingpong.GlideApp;
import com.pingpong.R;
import com.pingpong.WebViewActivity;
import com.pingpong.models.home_content.Slide;


import java.util.ArrayList;

import static com.pingpong.utils.MyAppClass.getContext;

public class SliderAdapter extends CardSliderAdapter<SliderAdapter.SliderHolder> {

    private Activity activity;
    private ArrayList<Slide> items;

    public SliderAdapter(@NonNull ArrayList<Slide> items, @NonNull Activity activity) {
        this.activity = activity;
        this.items = items;
    }

    @Override
    public void bindVH(@NonNull SliderHolder sliderHolder, int i) {
        Slide slide = items.get(i);
        sliderHolder.title.setText(slide.getTitle());
        if(activity != null && !activity.isDestroyed() && !activity.isFinishing()) {
            GlideApp.with(activity)
                    .load(slide.getImageLink())
                    .dontTransform()
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .into(sliderHolder.slideImages);
        }
        sliderHolder.slideImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (slide.getActionType().equalsIgnoreCase("tvseries") || slide.getActionType().equalsIgnoreCase("movie")) {
                    Intent intent = new Intent(getContext(), DetailsActivity.class);
                    intent.putExtra("vType", slide.getActionType());
                    intent.putExtra("id", slide.getActionId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getContext().startActivity(intent);
                } else if (slide.getActionType().equalsIgnoreCase("webview")) {
                    Intent intent = new Intent(getContext(), WebViewActivity.class);
                    intent.putExtra("url", slide.getActionUrl());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getContext().startActivity(intent);

                } else if (slide.getActionType().equalsIgnoreCase("external_browser")) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(slide.getActionUrl()));
                    browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getContext().startActivity(browserIntent);
                } else if (slide.getActionType().equalsIgnoreCase("tv")) {
                    Intent intent = new Intent(getContext(), DetailsActivity.class);
                    intent.putExtra("vType", slide.getActionType());
                    intent.putExtra("id", slide.getActionId());

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getContext().startActivity(intent);
                }
            }
        });
    }

    @NonNull
    @Override
    public SliderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item, parent, false);
        return new SliderHolder(view);
    }

    @Override
    public int getItemCount() {
        return items != null && items.size() > 0 ? items.size() : 0;
    }

    public class SliderHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView slug;
        private ImageView slideImages;

        public SliderHolder(View view) {
            super(view);
            title = view.findViewById(R.id.textView);
            slug = view.findViewById(R.id.slug);
            slideImages = view.findViewById(R.id.imageview);
        }
    }
}
