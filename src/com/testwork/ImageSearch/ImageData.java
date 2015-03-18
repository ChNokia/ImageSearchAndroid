package com.testwork.ImageSearch;

public class ImageData {
    private String title;
    private String url;
    private String thumbUrl;

    public String getThumbUrl()
    {
        return thumbUrl;
    }

    public String getTitle()
    {
        return title;
    }

    public String getUrl()
    {
        return url;
    }

    public void setThumbUrl(String thumbUrl)
    {
        this.thumbUrl = thumbUrl;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }
}
