package org.example.Agencies;

import org.greenrobot.eventbus.EventBus;

public class Agencies {
    public static void main(String[] args) {
        EventBus eventBus = EventBus.getDefault();

        // Create domains
        Domain sports = new Domain("Sports");
        Domain politics = new Domain("Politics");
        Domain culture = new Domain("Culture");

        // Create news agencies
        NewsAgency agency1 = new NewsAgency("Agency1");
        agency1.addDomain(sports);
        agency1.addDomain(politics);

        NewsAgency agency2 = new NewsAgency("Agency2");
        agency2.addDomain(culture);

        // Create subscribers
        Subscriber subscriber1 = new Subscriber("Alice");
        Subscriber subscriber2 = new Subscriber("Bob");

        eventBus.register(subscriber1); //add them to the event-bus list to can listen events
        eventBus.register(subscriber2);

        // Subscribe to domains
        subscriber1.subscribeToDomain(sports);
        subscriber1.subscribeToDomain(culture);

        subscriber2.subscribeToDomain(politics);

        // Publish news
        agency1.publishNews("Sports event happening!", sports);
        agency1.publishNews("Political debate tonight!", politics);
        agency2.publishNews("Art exhibition opening!", culture);

        // Unsubscribe and test
        subscriber1.unsubscribeFromDomain(culture);
        agency2.publishNews("New sport festival announced!", sports);


        // Unregister subscribers
        EventBus.getDefault().unregister(subscriber1);
        EventBus.getDefault().unregister(subscriber2);
    }
}