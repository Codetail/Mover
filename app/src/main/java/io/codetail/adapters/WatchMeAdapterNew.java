package io.codetail.adapters;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.telly.mrvector.MrVector;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import codetail.graphics.drawables.DrawableHelper;
import codetail.graphics.drawables.RippleDrawable;
import codetail.utils.ResourceUtils;
import codetail.utils.ThemeUtils;
import io.codetail.WatchMeApplication;
import io.codetail.client.models.Video;
import io.codetail.watchme.R;

import static android.view.ViewGroup.MarginLayoutParams;

public class WatchMeAdapterNew extends RecyclerView.Adapter<WatchMeAdapterNew.WatchMeHolder>{

    /**
     * header row with title of section !must be always first
     */
    public final static int TYPE_HEADER = 0;
    public final static int TYPE_HEADER_FIRST = 1;

    /**
     * middle content row with video of specific section
     * goes after {@link #TYPE_HEADER}
     */
    public final static int TYPE_VIDEO = 2;
    public final static int TYPE_VIDEO_LAST = 3;

    /**
     * last row with more button,
     */
    public final static int TYPE_MORE = 4;

    /**
     * Lock used to modify the content of {@link #mSections}. Any write operation
     * performed on the array should be synchronized on this lock.
     */
    private final Object mLock = new Object();

    /**
     * Contains the list of section objects that represent the data of this Adapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private ArrayList<Section> mSections;

    /**
     * Key for saving {@link #mSections} in {@link android.os.Bundle}
     *
     * @see {@link #onSaveInstanceState(android.os.Bundle)}
     */
    private String mKey;

    private LayoutInflater mFactory;

    private Drawable mPlaceholder;
    private Drawable mExpandDrawable;
    private ColorStateList mRippleColors;
    private ColorStateList mButtonsRippleColors;

    @Inject Picasso mPicasso;

    public WatchMeAdapterNew(@NonNull String saveKey) {
        mKey = saveKey;
        mSections = new ArrayList<>();
    }

    public void initResources(Context context){
        mFactory = LayoutInflater.from(context);
        mPlaceholder = new ColorDrawable(ResourceUtils.getColor(R.color.placeholder_color));
        mExpandDrawable = MrVector.inflateCompatOnly(context.getResources(), R.drawable.ic_expand_more);
        DrawableHelper.setTint(mExpandDrawable, 0xff000000);

        mRippleColors = ThemeUtils.getThemeColorStateList(context, R.attr.rippleOverlayColor);
        mButtonsRippleColors = ResourceUtils.getColorList(R.color.overlay_color);
    }

    public void onRestoreInstanceState(Bundle state){
        ArrayList<Section> sections = state.getParcelableArrayList(mKey);

        if(sections != null){
            mSections = sections;
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public WatchMeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case TYPE_HEADER_FIRST:
            case TYPE_HEADER:
                return new HeaderHolder(mFactory
                        .inflate(R.layout.card_header_item, parent, false), viewType == TYPE_HEADER_FIRST);

            case TYPE_VIDEO:
                return new VideoHolder(mFactory
                        .inflate(R.layout.card_video_item, parent, false));

            case TYPE_VIDEO_LAST:
                return new VideoHolder(mFactory
                        .inflate(R.layout.card_last_video_item, parent, false));

            case TYPE_MORE:
                return new MoreButtonHolder(mFactory
                        .inflate(R.layout.card_more_button_item, parent, false));

            default:
                throw new IllegalStateException(
                        String.format("Unknown view type found (%s)", viewType));
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(WatchMeHolder absHolder, int position) {
        absHolder.mObject = getItemInfo(position);

        switch (absHolder.getItemViewType()){
            case TYPE_HEADER:
            case TYPE_HEADER_FIRST:
                bindHeaderHolder((HeaderHolder) absHolder);
                return;

            case TYPE_VIDEO:
            case TYPE_VIDEO_LAST:
                bindVideoHolder((VideoHolder) absHolder);
                return;

            case TYPE_MORE:
                bindMoreButton((MoreButtonHolder) absHolder);
                break;

        }
    }

    private void bindHeaderHolder(HeaderHolder holder){
        CharSequence title = holder.getObject();

        holder.mTitle.setText(title);
    }

    private void bindMoreButton(MoreButtonHolder holder){
        holder.mShowMore.setImageDrawable(mExpandDrawable);

        if(!(holder.mShowMore.getBackground() instanceof RippleDrawable)) {
            RippleDrawable.makeFor(holder.mShowMore, mButtonsRippleColors, true);
        }
    }

    private void bindVideoHolder(VideoHolder holder){
        Video video = holder.getObject();

        holder.mTitle.setText(video.getTitle());
        holder.mThumbnail.setContentDescription(video.getTitle());
        holder.mAuthor.setText(video.getOwner().getUsername());
        holder.mViews.setText(ResourceUtils.getString(R.string.video_views_count, video.getViewsCount()));
        holder.mDuration.setText(video.getDuration());

        mPicasso.load(video.getThumbnail())
                .placeholder(mPlaceholder)
                .resizeDimen(R.dimen.thumbnail_width, R.dimen.thumbnail_height)
                .centerCrop()
                .tag(WatchMeApplication.PICASSO_INSTANCE)
                .into(holder.mThumbnail);

        if(!(holder.itemView.getBackground() instanceof RippleDrawable)) {
            //RippleDrawable.makeFor(holder.itemView, mRippleColors, true);
            RippleDrawable.makeFor(holder.itemView, mRippleColors, true);
        }
    }

    public void displayAllSectionItems(int position){
        synchronized (mLock){
            for(Section section : mSections){
                if(section.contains(position)){
                    final int startPosition = section.mPositionOffset + section.mVisibleCount - 1;
                    int itemCount = section.mVisibleCount;

                    section.mItems.remove(section.mVisibleCount - 1);
                    section.mVisibleCount = section.mItems.size();

                    itemCount = section.mVisibleCount - itemCount;

                    recomputeSectionsPositionOffsets();

                    notifyItemRangeInserted(startPosition, itemCount);
                    break;
                }
            }
        }
    }

    private void recomputeSectionsPositionOffsets(){
        int last = 0;

        for(Section section: mSections){
            section.mPositionOffset = last;
            last = section.mPositionOffset + section.mVisibleCount;
        }
    }


    /***
     * @param position The position of item data to return
     * @return item data at given position
     */
    public Object getItem(int position){
        return getItemInfo(position).getInfo();
    }

    private ItemInfo getItemInfo(int position){
        for(Section section : mSections){
            if(section.contains(position)){
                return section.getItemInfo(position);
            }
        }

        throw new IllegalStateException("Sections does not contains position = " + position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return getVisibleItemsCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemViewType(int position) {
        for(Section section : mSections){
            if(section.contains(position)){
                return section.getViewType(position);
            }
        }

        return -1;
    }

    /**
     * Return size of current visible data
     *
     * @return size of current visible data
     */
    public int getVisibleItemsCount(){
        int count = 0;
        for(Section section : mSections){
            count += section.mVisibleCount;
        }
        return count;
    }

    /**
     * Return size of all items in section
     *
     * @return size of all items in section
     */
    public int getAllItemsCount(){
        int count = 0;
        for(Section section : mSections){
            count += section.mItems.size();
        }
        return count;
    }

    public void add(CharSequence title, List<Video> videos, int visible){
        synchronized (mLock) {
            if(visible > videos.size()){
                throw new IllegalArgumentException(String.format("visible size (%s) must be less than videos size (%s)",
                        visible, videos.size()));
            }

            Section section = new Section();
            section.mVisibleCount = visible;
            section.mSize = videos.size();

            boolean firstItem = mSections.size() == 0;

            if (firstItem) {
                section.mPositionOffset = 0;
            } else {
                Section last = mSections.get(mSections.size() - 1);
                section.mPositionOffset = last.mPositionOffset + last.mVisibleCount;
            }

            section.mVisibleCount += 1; // header row
            HeaderInfo headerInfo = new HeaderInfo(title);
            if(firstItem){
                headerInfo.mViewType = TYPE_HEADER_FIRST;
            }

            section.mItems.add(headerInfo);

            for(int index = 0; index < visible; index++){
                section.mItems.add(new VideoInfo(videos.get(index)));
            }

            if(section.hasMore()){
                section.mVisibleCount += 1;// more row
                section.mItems.add(new MoreButtonInfo());
            }else{
                section.mItems.get(section.mVisibleCount - 1)
                        .mViewType = TYPE_VIDEO_LAST;
            }

            for(int index = visible; index < section.mSize; index++){
                section.mItems.add(new VideoInfo(videos.get(index)));
            }

            section.mItems.get(section.mItems.size() - 1).mViewType = TYPE_VIDEO_LAST;

            mSections.add(section);

            notifyItemRangeInserted(getVisibleItemsCount(),
                    visible);
        }
    }

    public void clear(){
        synchronized (mLock){
            mSections.clear();

            notifyDataSetChanged();
        }
    }

    public void onSaveInstanceState(Bundle outState){
        if(mSections.size() > 0) {
            outState.putParcelableArrayList(mKey, mSections);
        }
    }

    /**
     * @param object the object for we generating key
     * @return key for using at{@link #onRestoreInstanceState(android.os.Bundle)},
     * {@link #onSaveInstanceState(android.os.Bundle)}
     */
    public static String generateKey(Object object){
        return object.getClass().getName();
    }


    /**
     * Full information about section
     */
    public static class Section implements Parcelable {

        int mSize;
        int mVisibleCount;
        int mPositionOffset;

        ArrayList<ItemInfo> mItems;

        public Section(){
            mItems = new ArrayList<>();

            mSize = 0;
            mPositionOffset = 0;
            mVisibleCount = 0;
        }

        public int getViewType(int position){
            return getItemInfo(position).getViewType();
        }

        public Object getItem(int position){
            return getItemInfo(position).getInfo();
        }

        public ItemInfo getItemInfo(int position){
            return mItems.get(normalizePosition(position));
        }

        public int normalizePosition(int position){
            return position - mPositionOffset;
        }

        /**
         * Returns does section contains given position
         *
         * @param position position of item
         * @return does section contains given position
         */
        public boolean contains(int position){
            return position >= mPositionOffset &&
                   position < mVisibleCount + mPositionOffset;
        }

        /**
         * @return true if need to show more button
         */
        public boolean hasMore(){
            return mSize > mVisibleCount;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mPositionOffset);
            dest.writeInt(mVisibleCount);
            dest.writeInt(mSize);

            List<ItemInfo> list = mItems;
            int N = list.size();
            dest.writeInt(N);
            for(int i=0; i<N; i++){
                ItemInfo ii = list.get(i);
                dest.writeInt(ii.mViewType);
                ii.writeToParcel(dest, flags);
            }
        }

        private Section(Parcel source){
            this();

            mPositionOffset = source.readInt();
            mVisibleCount = source.readInt();
            mSize = source.readInt();

            int N = source.readInt();
            ArrayList<ItemInfo> list = new ArrayList<>();
            for(int i=0;i<N;i++){
                list.add(ItemInfo.CREATOR.createFromParcel(source));
            }
            mItems = list;
        }

        public static final Creator<Section> CREATOR = new Creator<Section>() {
            @Override
            public Section createFromParcel(Parcel in) {
                return new Section(in);
            }

            @Override
            public Section[] newArray(int size) {
                return new Section[size];
            }
        };
    }


    public static class ItemInfo implements Parcelable{

        int mViewType;

        protected ItemInfo(int viewType) {
            mViewType = viewType;
        }

        protected ItemInfo(int type, Parcel parcel){
            mViewType = type;
        }

        public int getViewType() {
            return mViewType;
        }

        public Object getInfo() {
            return null;
        }

        @Override
        public int hashCode(){
            return mViewType + 31;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            //dest.writeInt(mViewType);
        }

        @Override
        public int describeContents() {
            return 0;
        }


        public static final Creator<ItemInfo> CREATOR = new Creator<ItemInfo>() {
            @Override
            public ItemInfo createFromParcel(Parcel in) {
                int type = in.readInt();

                switch (type){
                    case TYPE_HEADER:
                    case TYPE_HEADER_FIRST:
                        return new HeaderInfo(type, in);

                    case TYPE_VIDEO:
                    case TYPE_VIDEO_LAST:
                        return new VideoInfo(type, in);

                    case TYPE_MORE:
                        return new MoreButtonInfo(type, in);

                    default:
                        throw new RuntimeException(String.format("Unknown ItemInfo type (%s)", type));
                }
            }

            @Override
            public ItemInfo[] newArray(int size) {
                return new ItemInfo[size];
            }
        };
    }

    public static class HeaderInfo extends ItemInfo{

        CharSequence mTitle;

        protected HeaderInfo(CharSequence title) {
            super(TYPE_HEADER);
            mTitle = title;
        }

        private HeaderInfo(int type, Parcel in) {
            super(type, in);

            mTitle = TextUtils.CHAR_SEQUENCE_CREATOR
               .createFromParcel(in);
        }

        @Override
        public Object getInfo() {
            return mTitle;
        }

        @Override
        public int hashCode() {
            return mTitle.hashCode();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            TextUtils.writeToParcel(mTitle, dest, flags);
        }

    }

    public static class VideoInfo extends ItemInfo {

        Video mVideo;

        protected VideoInfo(Video item) {
            super(TYPE_VIDEO);
            mVideo = item;
        }

        private VideoInfo(int type, Parcel in) {
            super(type, in);
            mVideo = Video.CREATOR.createFromParcel(in);
        }

        @Override
        public Object getInfo() {
            return mVideo;
        }

        @Override
        public int hashCode() {
            return mVideo.hashCode();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            mVideo.writeToParcel(dest, flags);
        }
    }

    private static class MoreButtonInfo extends ItemInfo{

        protected MoreButtonInfo() {
            super(TYPE_MORE);
        }

        private MoreButtonInfo(int type, Parcel in) {
            super(type, in);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
        }
    }


    /**
     * Extended version of {@link android.support.v7.widget.RecyclerView.ViewHolder}
     * with associating object, and know view type
     */
    public abstract static class WatchMeHolder extends RecyclerView.ViewHolder{

        private ItemInfo mObject;

        public WatchMeHolder(View itemView) {
            super(itemView);
        }

        @SuppressWarnings("unchecked")
        public <T> T getObject(){
            return (T) mObject.getInfo();
        }

    }

    public static class VideoHolder extends WatchMeHolder{

        @InjectView(R.id.card_video_item_thumbnails)
        ImageView mThumbnail;

        @InjectView(R.id.card_video_item_duration)
        TextView mDuration;

        @InjectView(R.id.card_video_item_title)
        TextView mTitle;

        @InjectView(R.id.card_video_item_author)
        TextView mAuthor;

        @InjectView(R.id.card_video_item_views)
        TextView mViews;

        public VideoHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }

    }

    public static class HeaderHolder extends WatchMeHolder{

        @InjectView(R.id.card_header_title)
        TextView mTitle;

        public HeaderHolder(View itemView, boolean firstItem) {
            super(itemView);
            ButterKnife.inject(this, itemView);

            if(firstItem){
                MarginLayoutParams params = (MarginLayoutParams) mTitle.getLayoutParams();
                params.topMargin = 0;
            }

            mTitle.setClickable(false);
        }
    }


    public static class MoreButtonHolder extends WatchMeHolder{

        @InjectView(R.id.card_show_more_button)
        ImageView mShowMore;

        public MoreButtonHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            mShowMore.setClickable(true);
        }
    }

}
