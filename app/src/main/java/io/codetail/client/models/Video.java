package io.codetail.client.models;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import io.codetail.Constants;
import io.codetail.client.mover.Mover;

@SuppressWarnings("unused")
public class Video implements Parcelable{

    final int type;

    String id;
    String title;
    String description;
    String thumbnail;
    String duration;

    int likes;
    int dislikes;

    int viewsCount;
    Channel owner;
    List<Comment> comments;

    boolean isViewed;
    boolean isPinned;

    String directLink;

    protected Video(int type){
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(int viewsCount) {
        this.viewsCount = viewsCount;
    }

    public Channel getOwner() {
        return owner;
    }

    public void setOwner(Channel owner) {
        this.owner = owner;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    public boolean isViewed() {
        return isViewed;
    }

    public void setViewed(boolean isViewed) {
        this.isViewed = isViewed;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean isPinned) {
        this.isPinned = isPinned;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getDirectLink(String options){
        return directLink;
    }

    public void setDirectLink(String link) {
        directLink = link;
    }

    public String getLinkForShare(){
        return null;
    }

    @Override
    public String toString() {
        return "Video{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", duration='" + duration + '\'' +
                ", likes=" + likes +
                ", dislikes=" + dislikes +
                ", viewsCount=" + viewsCount +
                ", owner=" + owner +
                ", comments=" + comments +
                ", isViewed=" + isViewed +
                ", isPinned=" + isPinned +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Video.class != o.getClass()) return false;

        Video video = (Video) o;
        return id.equals(video.id)
                && owner.equals(video.owner)
                && title.equals(video.title);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + owner.hashCode();
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);

        int N = comments == null ? -1 : comments.size();
        dest.writeInt(N);
        for(int i=0; i<N; i++){
            Comment c = comments.get(i);
            c.writeToParcel(dest, flags);
        }

        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeString(this.thumbnail);
        dest.writeString(this.duration);
        dest.writeString(this.directLink);

        dest.writeInt(this.likes);
        dest.writeInt(this.dislikes);
        dest.writeInt(this.viewsCount);

        owner.writeToParcel(dest, flags);

        dest.writeByte(isViewed ? (byte) 1 : (byte) 0);
        dest.writeByte(isPinned ? (byte) 1 : (byte) 0);
    }

    protected Video(Parcel in, int type) {
        this.type = type;

        int N = in.readInt();
        if(N > 0) {
            comments = new ArrayList<>();
            for (int i = 0; i < N; i++) {
                comments.add(Comment.CREATOR.createFromParcel(in));
            }
        }

        this.id = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.thumbnail = in.readString();
        this.duration = in.readString();
        this.directLink = in.readString();

        this.likes = in.readInt();
        this.dislikes = in.readInt();
        this.viewsCount = in.readInt();

        this.owner = Channel.CREATOR.createFromParcel(in);

        this.isViewed = in.readByte() != 0;
        this.isPinned = in.readByte() != 0;
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        public Video createFromParcel(Parcel source) {
            int videoType = source.readInt();

            switch (videoType){

                case Constants.MOVER_VIDEO_TYPE:
                    return new Mover.MoverVideo(source, videoType);

                default:
                    throw new IllegalStateException(String.format("Unknown video format(%s)", videoType));
            }
        }

        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
}
