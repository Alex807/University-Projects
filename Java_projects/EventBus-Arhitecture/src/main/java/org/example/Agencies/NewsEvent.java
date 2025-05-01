package org.example.Agencies;

public class NewsEvent {
    private String news;
    private Domain domain;

    public NewsEvent(String news, Domain domain) {
        this.news = news;
        this.domain = domain;
    }

    public String getNews() {
        return news;
    }

    public Domain getDomain() {
        return domain;
    }
}
