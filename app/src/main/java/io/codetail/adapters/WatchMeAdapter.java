package io.codetail.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import codetail.graphics.drawables.RippleDrawable;
import codetail.utils.ThemeUtils;
import codetail.utils.ViewUtils;
import io.codetail.WatchMeApplication;
import io.codetail.client.models.Video;
import io.codetail.watchme.R;
import io.codetail.widget.CardLinearLayout;

@Deprecated
public class WatchMeAdapter extends RecyclerView.Adapter<WatchMeAdapter.CardHolder>{

    public static interface OnItemClickListener{
        public void onItemClick(Video video);
    }

    public static interface OnItemLongClickListener{
        public void onItemLongClick(Video video);
    }


    View.OnClickListener mOnMoreClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            Card card = (Card) v.getTag(R.id.adapter_card);
            Picasso picasso = (Picasso) v.getTag(R.id.adapter_picasso_instance);
            CardLinearLayout layout = (CardLinearLayout) v.getParent();

            int size = card.videos.size();
            int color = layout.getResources().getColor(R.color.background_material_light);

            View viewToBind[] = layout.beginBinding(size);

            for(int index = 0; index < size; index++){
                View item = viewToBind[index];

                TextView title = ButterKnife.findById(item, R.id.card_video_item_title);
                TextView views = ButterKnife.findById(item, R.id.card_video_item_views);
                TextView author = ButterKnife.findById(item, R.id.card_video_item_author);
                ImageView thumbnail = ButterKnife.findById(item, R.id.card_video_item_thumbnails);

                Video video = card.videos.get(index);

                title.setText(video.getTitle());
                author.setText(video.getOwner().getUsername());
                views.setText(String.valueOf(video.getViewsCount()));

                item.setOnClickListener(mSupportItemClick);
                item.setOnLongClickListener(mSupportLongClick);

                picasso.load(video.getThumbnail())
                        .placeholder(new ColorDrawable(color))
                        .resizeDimen(R.dimen.thumbnail_width, R.dimen.thumbnail_height)
                        .centerCrop()
                        .into(thumbnail);
                if(!(item.getBackground() instanceof RippleDrawable)){
                    RippleDrawable.makeFor(item, mRippleColors, true);
                }
            }

            card.visibleItems = size;
            ViewUtils.setVisibilityWithGoneFlag(v, false);
        }


    };

    View.OnClickListener mSupportItemClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mItemClick != null){
                mItemClick.onItemClick((Video) v.getTag());
            }
        }
    };

    View.OnLongClickListener mSupportLongClick = new View.OnLongClickListener(){

        @Override
        public boolean onLongClick(View v) {
            if(mLongItemClick != null){
                mLongItemClick.onItemLongClick((Video) v.getTag());
                return true;
            }
            return false;
        }
    };

    @Inject Picasso mPicasso;

    private ArrayList<Card> mCards;
    private int mPlaceholderColor;
    private ColorStateList mRippleColors;
    private OnItemClickListener mItemClick;
    private OnItemLongClickListener mLongItemClick;

    private String mKey;

    public WatchMeAdapter(Context context) {
        mCards = new ArrayList<>();
        mPlaceholderColor = context.getResources().getColor(R.color.background_material_light);
        mRippleColors = ThemeUtils.getThemeColorStateList(context, R.attr.rippleOverlayColor);

        WatchMeApplication application = (WatchMeApplication) context.getApplicationContext();
        application.inject(this);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClick = listener;
    }
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mLongItemClick = listener;
    }

    public void setCustomSaveKey(String key){
        mKey = key;
    }

    public WatchMeAdapter(String key, Context context, Bundle savedInstanceState){
        this(context);
        mKey = key;

        if(savedInstanceState != null) {
            mCards = savedInstanceState
                    .getParcelableArrayList(mKey);
        }
    }

    @Override
    public CardHolder onCreateViewHolder(ViewGroup group, int i) {
        return new CardHolder(LayoutInflater.from(group.getContext())
                .inflate(R.layout.card_video_item, group, false));
    }

    @Override
    public void onBindViewHolder(CardHolder cardHolder, int position) {

        Card card = mCards.get(position);

        TextView cardTitle = cardHolder.mTitle;
        TextView more = cardHolder.mShowMore;
        CardLinearLayout layout = cardHolder.mCardLayout;

        cardTitle.setText(card.title);

        View[] bindViews = layout.beginBinding(card.visibleItems);
        int index = 0;

        for(View item : bindViews){
            TextView title = ButterKnife.findById(item, R.id.card_video_item_title);
            TextView views = ButterKnife.findById(item, R.id.card_video_item_views);
            TextView author = ButterKnife.findById(item, R.id.card_video_item_author);
            ImageView thumbnail = ButterKnife.findById(item, R.id.card_video_item_thumbnails);

            Video video = card.videos.get(index++);

            title.setText(video.getTitle());
            author.setText(video.getOwner().getUsername());
            views.setText(String.valueOf(video.getViewsCount()));

            item.setTag(video);
            item.setOnClickListener(mSupportItemClick);
            item.setOnLongClickListener(mSupportLongClick);

            mPicasso.load(video.getThumbnail())
                    .placeholder(new ColorDrawable(mPlaceholderColor))
                    .resizeDimen(R.dimen.thumbnail_width, R.dimen.thumbnail_height)
                    .centerCrop()
                    .into(thumbnail);

            if(!(item.getBackground() instanceof RippleDrawable)){
                RippleDrawable.makeFor(item, mRippleColors, true);
            }
        }

        more.setTag(R.id.adapter_card, card);
        more.setTag(R.id.adapter_picasso_instance, mPicasso);
        more.setOnClickListener(mOnMoreClickListener);

        ViewUtils.setVisibilityWithGoneFlag(more, card.visibleItems != card.videos.size());
    }

    @Override
    public long getItemId(int position) {
        return mCards.get(position).hashCode();
    }

    public void clear(){
        int size = mCards.size();
        mCards.clear();

        notifyDataSetChanged();
    }

    public void add(int page, CharSequence title, List<Video> videos){
        mCards.add(new Card(page, title, videos));

        notifyDataSetChanged();
    }

    public void add(int page, CharSequence title, List<Video> videos, int visible){
        mCards.add(new Card(page, title, videos, visible));

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }

    public void onSaveInstanceState(Bundle bundle){
        bundle.putParcelableArrayList(mKey, mCards);
    }

    public static class Card implements Parcelable {
        int page;
        CharSequence title;
        List<Video> videos;

        int visibleItems;

        Card(int page, CharSequence title, List<Video> videos) {
            this.page = page;
            this.title = title;
            this.videos = videos;
            this.visibleItems = videos.size();
        }

        public Card(int page, CharSequence title, List<Video> videos, int visibleItems) {
            this.page = page;
            this.title = title;
            this.videos = videos;
            this.visibleItems = visibleItems;
        }

        @Override
        public int hashCode() {
            return title.hashCode() + page;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            TextUtils.writeToParcel(title, dest, flags);
            dest.writeInt(page);
            dest.writeInt(visibleItems);
            dest.writeTypedList(videos);
        }

        private Card(Parcel in) {
            videos = new ArrayList<>();
            
            title = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
            page = in.readInt();
            visibleItems = in.readInt();
            in.readTypedList(videos, Video.CREATOR);//FIXME 11/27/14
        }

        public static final Parcelable.Creator<Card> CREATOR = new Parcelable.Creator<Card>() {
            public Card createFromParcel(Parcel source) {
                return new Card(source);
            }

            public Card[] newArray(int size) {
                return new Card[size];
            }
        };
    }

    public static class CardHolder extends RecyclerView.ViewHolder{

        @InjectView(R.id.cardLayout)
        CardLinearLayout mCardLayout;

        @InjectView(R.id.cardTitle)
        TextView mTitle;

        @InjectView(R.id.showMoreButton)
        TextView mShowMore;

        public CardHolder(View itemView) {
            super(itemView);

            ButterKnife.inject(this, itemView);
        }

        public View inflate(@LayoutRes int id){
            return LayoutInflater.from(mCardLayout.getContext())
                    .inflate(id, mCardLayout, false);
        }

        public void addView(View view){
            int position = mCardLayout.indexOfChild(mShowMore);
            mCardLayout.addView(view, position);
        }

    }

}
