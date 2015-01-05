package io.codetail.client.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Comment implements Parcelable {

    Channel user;
    String comment;

    long time;

    public Channel getUser() {
        return user;
    }

    public void setUser(Channel user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "user=" + user +
                ", comment='" + comment + '\'' +
                ", time=" + time +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.user, flags);
        dest.writeString(this.comment);
        dest.writeLong(this.time);
    }

    public Comment() {
    }

    private Comment(Parcel in) {
        this.user = Channel.CREATOR.createFromParcel(in);
        this.comment = in.readString();
        this.time = in.readLong();
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
}
