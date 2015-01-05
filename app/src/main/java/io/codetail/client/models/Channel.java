package io.codetail.client.models;


import android.os.Parcel;
import android.os.Parcelable;

public class Channel implements Parcelable{

    public final static String NAME = "username";
    public final static String PICTURE = "picture";

    String username;
    String password;
    String picture;
    String displayName;

    int videosCount;
    int profileViewsCount;

    long registrationDate;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getVideosCount() {
        return videosCount;
    }

    public void setVideosCount(int videosCount) {
        this.videosCount = videosCount;
    }

    public int getProfileViewsCount() {
        return profileViewsCount;
    }

    public void setProfileViewsCount(int profileViewsCount) {
        this.profileViewsCount = profileViewsCount;
    }

    public long getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(long registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", picture='" + picture + '\'' +
                ", displayName='" + displayName + '\'' +
                ", videosCount=" + videosCount +
                ", profileViewsCount=" + profileViewsCount +
                ", registrationDate=" + registrationDate +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.username);
        dest.writeString(this.password);
        dest.writeString(this.picture);
        dest.writeString(this.displayName);

        dest.writeInt(this.videosCount);
        dest.writeInt(this.profileViewsCount);

        dest.writeLong(this.registrationDate);
    }

    public Channel() {
    }

    private Channel(Parcel in) {
        this.username = in.readString();
        this.password = in.readString();
        this.picture = in.readString();
        this.displayName = in.readString();

        this.videosCount = in.readInt();
        this.profileViewsCount = in.readInt();

        this.registrationDate = in.readLong();
    }

    public static final Creator<Channel> CREATOR = new Creator<Channel>() {
        public Channel createFromParcel(Parcel source) {
            return new Channel(source);
        }

        public Channel[] newArray(int size) {
            return new Channel[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Channel.class != o.getClass()) return false;

        Channel channel = (Channel) o;
        return username.equals(channel.username);

    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
