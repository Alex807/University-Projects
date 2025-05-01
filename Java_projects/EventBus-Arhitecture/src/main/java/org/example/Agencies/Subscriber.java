package org.example.Agencies;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashSet;
import java.util.Set;

public class Subscriber {
    private String name;
    private Set<Domain> subscribedDomains;

    public Subscriber(String name) {
        this.name = name;
        this.subscribedDomains = new HashSet<>();

    }

    public void subscribeToDomain(Domain domain) {
        subscribedDomains.add(domain);
        System.out.println(name + " subscribed to " + domain.getName());
    }

    public void unsubscribeFromDomain(Domain domain) {
        subscribedDomains.remove(domain);
        System.out.println(name + " unsubscribed from " + domain.getName());
    }

    @Subscribe
    public void onNewsEvent(NewsEvent event) {
        if (subscribedDomains.contains(event.getDomain())) {
            System.out.println(name + " received news: " + event.getNews());
        }
    }
}
