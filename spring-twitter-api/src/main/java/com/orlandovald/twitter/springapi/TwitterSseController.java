package com.orlandovald.twitter.springapi;

import com.orlandovald.twitter.support.Tweet;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Profile("annotation-based")
@RestController
public class TwitterSseController {

    private final TweetRepository repo;

    public TwitterSseController(TweetRepository repo) {
        this.repo = repo;
    }

    @GetMapping(path = "/api/tweet", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> streamEvents() {
        return repo.findBy();
    }

}
