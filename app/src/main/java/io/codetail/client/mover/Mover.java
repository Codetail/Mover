package io.codetail.client.mover;

import android.os.Parcel;

import com.squareup.otto.Bus;

import java.util.List;

import io.codetail.Constants;
import io.codetail.client.Page;
import io.codetail.client.State;
import io.codetail.client.models.Video;

import static io.codetail.client.mover.MoverParser.CategoryParser;
import static io.codetail.client.mover.MoverParser.PagesParser;

public abstract class Mover implements Page{

    public static class Suggestion extends Mover{
        String mId;
        List<String> mAvailableQuality;

        Suggestion(List<String> qualities, String position){
            super(null);

            mId = position;
            mAvailableQuality = qualities;
        }

        public String getPosition() {
            return mId;
        }

        public List<String> getAvailableQuality() {
            return mAvailableQuality;
        }

        @Override
        public Page from(String source) {
            return null;
        }

        @Override
        public void postEvent(Bus bus) {
            bus.post(this);
        }
    }

    String mCategory;

    protected Mover(String category) {
        this.mCategory = category;
    }

    @Override
    public String getCategory() {
        return mCategory;
    }

    /**
     * Category paginated result
     */
    public static class PaginatedPage extends Mover implements Page.PaginatedPage {

        int pageNumber;
        int pagesCount;

        List<Video> videos;

        public PaginatedPage(String category, int pageNumber) {
            super(category);
            this.pageNumber = pageNumber;
        }

        public void setPagesCount(int pagesCount) {
            this.pagesCount = pagesCount;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public void setVideos(List<Video> videos) {
            this.videos = videos;
        }

        public List<Video> getVideos() {
            return videos;
        }

        @Override
        public boolean isMainPage() {
            return pageNumber == 1;
        }

        @Override
        public int getPagesCount() {
            return pagesCount;
        }

        @Override
        public boolean hasPagination() {
            return pagesCount > 1;
        }

        @Override
        public int getCurrentPageNumber() {
            return pageNumber;
        }

        @Override @SuppressWarnings("unchecked")
        public io.codetail.client.mover.Mover.PaginatedPage from(String source) {
            return new PagesParser().parse(this, source);
        }

        @Override
        public void postEvent(Bus bus) {
            bus.post(new State.OnPaginatedPageResponseEvent(this));
        }
    }

    public static class SearchPage extends PaginatedPage{

        String mQuery;

        public boolean hasResult(){
            return getVideos() != null && getVideos().size() > 0;
        }

        public SearchPage(String query, int pageNumber) {
            super(null, pageNumber);
            mQuery = query;
        }

        public String getQuery(){
            return mQuery;
        }

        @Override
        public String getCategory() {
            throw new UnsupportedOperationException("Search doesn't provide category");
        }

        @Override
        public void postEvent(Bus bus) {
            bus.post(new State.OnSearchResponseEvent(this));
        }
    }

    /**
     * Main page of category
     */
    public static class CategoryPage extends Mover {

        List<Video> recommends;
        List<Video> popular;
        List<Video> videos;

        int pagesCount;

        public CategoryPage(String category) {
            super(category);
        }

        public void setRecommends(List<Video> recommends) {
            this.recommends = recommends;
        }

        public void setPopular(List<Video> popular) {
            this.popular = popular;
        }

        public void setPagesCount(int pagesCount) {
            this.pagesCount = pagesCount;
        }

        public int getPagesCount() {
            return pagesCount;
        }

        public void setVideos(List<Video> videos) {
            this.videos = videos;
        }

        public List<Video> getVideos() {
            return videos;
        }

        public List<Video> getRecommends() {
            return recommends;
        }

        public List<Video> getPopular() {
            return popular;
        }

        @Override
        public CategoryPage from(String source) {
            return new CategoryParser(mCategory.equals("")).parse(this, source);
        }

        @Override
        public void postEvent(Bus bus) {
            bus.post(new State.OnPageResponseEvent(this));
        }
    }

    public static class MoverVideo extends Video{

        public MoverVideo() {
            super(Constants.MOVER_VIDEO_TYPE);
        }

        @Override
        public String getLinkForShare() {
            return makeWatchLink(getId());
        }

        public MoverVideo(Parcel in, int type) {
            super(in, type);
        }

        @Override
        public String getDirectLink(String quality) {
            return createVideoLink(getId(), quality);
        }

        @Override
        public String getThumbnail() {
            return createImageLink(getId(), "s");
        }

        /**
         * Generates url to movie first frame generated in
         * mover.uz system
         *
         * @param id movie identification
         *
         * @return generated url to movie frame
         */
        private static String makeWatchLink(String id){
            return "http://mover.uz/watch/{id}"
                    .replace("{id}", id);
        }

        /**
         * Generates url to movie first frame generated in
         * mover.uz system
         *
         * @param id movie identification
         * @param quality of picture
         *
         * @return generated url to movie frame
         */
        public static String createVideoLink(String id, String quality){
            return "http://v.mover.uz/{id}_{qq}.mp4"
                    .replace("{id}", id)
                    .replace("{qq}", quality);
        }

        /**
         * Generates url to movie first frame generated in
         * mover.uz system
         *
         * @param id movie identification
         * @param quality of picture
         *
         * @return generated url to movie frame
         */
        public static String createImageLink(String id, String quality){
            return "http://i.mover.uz/{id}_{qq}1.jpg"
                    .replace("{id}", id)
                    .replace("{qq}", quality);
        }
    }

}
