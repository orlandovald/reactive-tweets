package com.orlandovald.twitter.springconsumer;

import com.orlandovald.twitter.support.OAuth1SignatureUtil;
import com.orlandovald.twitter.support.Tweet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import static java.util.stream.Collectors.joining;

@Component
public class TwitterCommand implements ApplicationRunner {

    private final OAuth1SignatureUtil oAuth;
    private final TweetRepository repo;

    public TwitterCommand(
            @Value("${TWITTER_CONSUMER_KEY}") String consumerKey,
            @Value("${TWITTER_CONSUMER_SECRET}") String consumerSecret,
            @Value("${TWITTER_ACCESS_TOKEN}") String accessToken,
            @Value("${TWITTER_ACCESS_TOKEN_SECRET}") String accessTokenSecret, TweetRepository repo) {
        this.repo = repo;
        this.oAuth = new OAuth1SignatureUtil(accessToken, accessTokenSecret, consumerKey, consumerSecret);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String tracks = args.getOptionValues("track").stream().collect(joining(","));

        WebClient webClient = WebClient.builder()
                .baseUrl("https://stream.twitter.com/1.1")
                .filter((request, next) -> {
                    ClientRequest newReq = ClientRequest.from(request)
                            .header(HttpHeaders.AUTHORIZATION, oAuth.oAuth1Header(request.url(), request.method().name()))
                            .build();
                    return next.exchange(newReq);
                })
                .build();

        Flux<Tweet> tweets = webClient.get().uri(uriBuilder -> uriBuilder.path("/statuses/filter.json")
                                                .queryParam("track", tracks).build())
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                    throw new RuntimeException(clientResponse.statusCode().getReasonPhrase());
                })
                .bodyToFlux(Tweet.class);

        repo.saveAll(tweets).log().subscribe();

    }

}
