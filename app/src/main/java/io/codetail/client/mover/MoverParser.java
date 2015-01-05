package io.codetail.client.mover;


import android.os.Bundle;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.codetail.client.IParser;
import io.codetail.client.models.Channel;
import io.codetail.client.models.Comment;
import io.codetail.client.models.Video;

public abstract class MoverParser<T extends Mover> implements IParser<T> {


    final Locale sLocaleRU = new Locale("ru", "RU");
    final static String PROFILE_FORMAT = "d MMMM yyyy";
    final static String COMMENT_FORMAT = "d MMMM yyyy, HH:mm";


    /**
     * Parses HTMLElement to {@link java.util.ArrayList} of {@link io.codetail.client.models.Video}
     *
     * @param element HTMLElement object, reduced circle of elements
     *                where to find video elements
     *
     * @return list of videos found in object
     */
    public ArrayList<Video> findVideos(Element element){
        return findVideos(element.select("div.video"));
    }

    /**
     * Parses HTMLElement to {@link java.util.ArrayList} of {@link io.codetail.client.models.Video}
     *
     * @param elements HTMLElements object, reduced circle of elements
     *                where to find video elements
     *
     * @return list of videos found in objects
     */
    public ArrayList<Video> findVideos(Elements elements){
        ArrayList<Video> videos = new ArrayList<>();

        for(Element element : elements){

            Element link = element.select("a.image").first();
            Element info = element.select("div.info").first();

            Video video = new Mover.MoverVideo();
            video.setId( getVideoId(link.attr("href")) );
            video.setTitle(link.attr("title"));
            video.setViewsCount( getViewCount(element, info) );
            video.setDuration(link.select("span.length").first().text());

            String user = info.select("p.owner a").first().text();
            Channel channel = new Channel();
            channel.setUsername(user);

            video.setOwner(channel);
            videos.add(video);
        }
        return videos;
    }

    public ArrayList<Video> findVideosInChannel(Elements elements){
        ArrayList<Video> videos = new ArrayList<>();

        for(Element element : elements){

            Element link = element.select("a.image").first();
            Element info = element.select("div.info").first();

            //FIXME
            Video video = new Mover.MoverVideo(); //new Video();
            video.setId( getVideoId(link.attr("href")) );
            video.setTitle(link.attr("title"));
            video.setViewsCount( getViewCount(element, info) );
            video.setDuration(link.select("span.length").first().text());

            videos.add(video);
        }
        return videos;
    }

    public int getLastNavigationPage(Document document){
        Elements elements = document.select("div.pagination .digits .ut a");
        if(elements.size() > 0){
            return internalGetIntegers(elements.last().text());
        }
        return -1;
    }

    /**
     * Parses HTMLElement to {@link java.util.ArrayList} of {@link io.codetail.client.models.Video}
     *
     * @param element HTMLElement object, reduced circle of elements
     *                where to find comment elements
     *
     * @return list of comment found in object
     */
    public ArrayList<Comment> findComments(Element element){
        return findComments(element.select("ul#listComment li"));
    }

    /**
     * Parses HTMLElement to {@link java.util.ArrayList} of {@link io.codetail.client.models.Video}
     *
     * @param elements HTMLElements object, reduced circle of elements
     *                where to find video elements
     *
     * @return list of videos found in object
     */
    public ArrayList<Comment> findComments(Elements elements){
        ArrayList<Comment> commentList = new ArrayList<>();
        for(Element element : elements){
            Comment comment = new Comment();
            Channel channel = new Channel();
            channel.setPicture(element.select("a.userpic img").first().attr("src"));
            channel.setUsername(element.select("a.author").text());
            comment.setUser(channel);
            comment.setTime(parseRussianFormat(COMMENT_FORMAT, element.select("span.date").text()));
            comment.setComment(element.select("p").text());
            commentList.add(comment);
        }
        return commentList;
    }

    public Channel getChannelExpandedInfo(Element element){
        Channel channel = new Channel();
        return getChannelExpandedInfo(element, channel);
    }

    public Channel getChannelExpandedInfo(Element element, Channel channel){
        Elements channelBox = element.select("div#channel-box");

        // Parsed user picture source link
        String userPicture = channelBox.select("a.userpic img").first().attr("src");
        channel.setPicture(userPicture);

        // Channel Display Name
        String displayName = channelBox.select("div.info div.user").first().text();
        channel.setDisplayName(displayName);

        String videosCount = channelBox.select("div.info div.videos").first().text();
        channel.setVideosCount(internalGetIntegers(videosCount));

        List<TextNode> dataNodes = channelBox.select("div.data")
                .first().textNodes();

        // Magic 1 is to get only registrationDate text information
        // Here is HARD CORE NEVER REPEAT THIS CODE CUT
        String registrationDate = dataNodes.get(1).text()
                .replace("Регистрация:", "").trim();

        channel.setRegistrationDate(parseRussianFormat(PROFILE_FORMAT, registrationDate));

        String profileViewsCount = dataNodes.get(2).text();
        channel.setProfileViewsCount(internalGetIntegers(profileViewsCount));
        return channel;
    }

    /**
     * @param pattern of russian date
     * @param object search
     * @return parse russian date and transform it to java
     *  long format
     */
    long parseRussianFormat(String pattern, String object){
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, sLocaleRU);

        try {
            Date date = dateFormat.parse(object);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * @param object to retrieve all integers
     * @return all ints founded and transformed to integer type
     */
    protected int internalGetIntegers(String object){
        return Integer.parseInt(object.replaceAll("[^0-9]", ""));
    }

    /**
     * @param text url to find video id
     * @return founded id
     */
    public static String getVideoId(String text){
        Pattern pattern = Pattern.compile("([\\w\\d]+){6,}");
        Matcher matcher = pattern.matcher(text);
        if(matcher.find()){
            return matcher.group();
        }
        return null;
    }

    /**
     * @param element base element
     * @param info div.info
     *
     * @return founded in info element views count and
     *  transform it to integer type
     */
    protected int getViewCount(Element element, Element info){
        String text;
        if(element.classNames().contains("main")){
            text = info.select("p.owner").first().childNodes().get(1).toString();
        }else{
            text = info.select("p.views").first().text();
        }

        return internalGetIntegers(text);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof String && canParse((String) o);
    }


    public static class DetailParserForMoverParser extends MoverParser<Mover> {


        @Override
        public Mover parse(Mover page, String source) {
            Document document = Jsoup.parse(source);
            Bundle data = new Bundle();

            // FIXME
            Video detail = new Mover.MoverVideo();//new Video();
            // getting video description without unused data

            Elements elements = document.select("div.desc p:not([class])");
            detail.setDescription(elements.outerHtml());
            detail.setViewsCount(internalGetIntegers(document.select(".fr.views strong").first().text()));

            // if user is authenticated collect data provided by
            // authentication feature

            if(isUserAuthenticated()) {
                Elements elements1 = document.select("table.r-desc");
                detail.setLikes(internalGetIntegers(elements1.select("td.like").text()));
                detail.setDislikes(internalGetIntegers(elements1.select("td.dislike").text()));
            }

//            data.putParcelableArrayList(COMMENTS, findComments(document));
//            data.putParcelable(DETAIL, detail);

            return page;
        }

        boolean isUserAuthenticated(){
            return false;
        }

        @Override
        public boolean canParse(String url) {
            try {
                URI uri = new URI(url);
                return uri.getPath().matches("/watch/([\\w\\d]+)");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return false;
        }
    }


    public static class ProfileParserForMoverParser extends MoverParser<Mover> {

        @Override
        public Mover parse(Mover page, String source) {
            Document document = Jsoup.parse(source);
            Bundle data = new Bundle();

            Channel channelInfo = new Channel();

            Elements elements = document.select("#channel-box");
            channelInfo.setUsername(elements.select("h4").first().text());
            channelInfo.setDisplayName(elements.select("div.user").first().text());
            channelInfo.setVideosCount(internalGetIntegers(elements.select("div.videos")
                    .first().text()));
//            channelInfo.setPicture(MoverService.HEAD_URL + elements.select("a.userpic img").attr("src"));
            //TODO: Need to solve problem with ViewCount and Registration Date

            //TODO: Check for empty

            Elements recs = document.select("div.video-recommended div.video");

            return page;

//            if(Objects.isNotNull(recs) && recs.size() > 0) {
//                data.putParcelableArrayList(RECOMMENDATIONS,
//                        findVideosInChannel(recs));
//            }
//
//            Elements latest = document.select(".video-list.vertical");
//            if(Objects.isNotNull(latest) && latest.size() > 0) {
//                data.putParcelableArrayList(LATEST, findVideos(
//                        document.select(".video-list.vertical").last().select("div.video")));
//            }
//
//            data.putInt(PAGINATION, getLastNavigationPage(document));
//            data.putParcelable(INFO, channelInfo);
//            return data;
        }

        @Override
        public boolean canParse(String url) {
            try {
                URI uri = new URI(url);
                return  uri.getPath().matches("/channel/([\\w\\d]+)") && uri.getQuery() == null;
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static class PagesParser extends MoverParser<Mover.PaginatedPage> {

        @Override
        public Mover.PaginatedPage parse(Mover.PaginatedPage page, String source) {
            Document document = Jsoup.parse(source);
            Elements elements = document.select(".video-list.vertical div.video");
            page.setVideos(findVideos(elements));
            page.setPagesCount(getLastNavigationPage(document));
            return page;
        }

        @Override
        public boolean canParse(String url) {
            try {
                URI uri = new URI(url);
                return uri.getPath().contains("/") && uri.getQuery() != null;
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static class CategoryParser extends MoverParser<Mover.CategoryPage> {

        boolean homePage;

        public CategoryParser(boolean homePage) {
            this.homePage = homePage;
        }

        @Override
        public Mover.CategoryPage parse(Mover.CategoryPage response, String source) {
            Document document = Jsoup.parse(source);

            Elements recommended = document.select(
                    homePage ? "div#home-recommended div.video" : "div.video-recommended div.video");

            Elements popular = document.select("#place_top_video div.video");

            response.setRecommends(findVideos(recommended));
            response.setPopular(findVideos(popular));

            Elements latest = document.select(".video-list.vertical").last()
                    .select("div.video");
            response.setVideos(findVideos(latest));
            response.setPagesCount(getLastNavigationPage(document));

            return response;
        }

        @Override
        public boolean canParse(String url) {
            try {
                URI uri = new URI(url);
                return (uri.getPath().equals("/") || uri.getPath().matches("/video/([\\w\\d]+)"))
                        && (uri.getQuery() == null);

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

}
