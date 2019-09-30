package com.orlandovald.twitter.springconsumer;

import com.orlandovald.twitter.support.Tweet;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TweetRepository extends ReactiveCrudRepository<Tweet, String> {
}
