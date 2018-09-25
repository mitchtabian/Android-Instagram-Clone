package tabian.com.instagramclone2.models;

/**
 * Created by User on 1/7/2018.
 */

public class Story {

    private String user_id;
    private String timestamp;
    private String image_url;
    private String video_url;
    private String story_id;
    private String views;
    private String duration;

    public Story(String user_id, String timestamp, String image_url, String video_url, String story_id, String views, String duration) {
        this.user_id = user_id;
        this.timestamp = timestamp;
        this.image_url = image_url;
        this.video_url = video_url;
        this.story_id = story_id;
        this.views = views;
        this.duration = duration;
    }

    public Story() {

    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getStory_id() {
        return story_id;
    }

    public void setStory_id(String story_id) {
        this.story_id = story_id;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Story{" +
                ", image_url='" + image_url + '\'' +
                ", video_url='" + video_url + '\'' +
                ", story_id='" + story_id + '\'' ;
    }
}
