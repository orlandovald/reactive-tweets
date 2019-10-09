package com.orlandovald.twitter.springapi;

import com.orlandovald.twitter.support.Tweet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Profile("!annotation-based")
@Configuration
public class TwitterSseFunctional {

    private final TweetRepository repo;

    public TwitterSseFunctional(TweetRepository repo) {
        this.repo = repo;
    }

    @Bean
    RouterFunction<ServerResponse> getTweetsAsSse() {
        return route(GET("/api/tweet"),
                req -> ok()
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(repo.findBy(), Tweet.class));
    }

}
