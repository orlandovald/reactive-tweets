package com.orlandovald.twitter.springapi;

import com.orlandovald.twitter.support.Tweet;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface TweetRepository extends ReactiveCrudRepository<Tweet, String> {

    @Tailable
    Flux<Tweet> findBy();

}
