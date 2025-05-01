package org.example.Agencies;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class NewsAgency {
    private String name;
    private List<Domain> domains;

    public NewsAgency(String name) {
        this.name = name;
        this.domains = new ArrayList<>();
    }

    public void addDomain(Domain domain) {
        domains.add(domain);
    }

    public void publishNews(String news, Domain domain) {
        if (domains.contains(domain)) {
            System.out.println(name + " published news in " + domain.getName() + ": " + news);
            EventBus.getDefault().post(new NewsEvent(news, domain));
        } else {
            System.out.println("Domain not supported by this agency.");
        }
    }
}
