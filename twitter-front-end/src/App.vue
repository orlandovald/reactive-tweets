<template>
  <div id="app">
    <div class="row center grey lighten-5" style="margin-bottom:20px;">
      <div class="s12">
        <span class="title">Reactive Tweets</span><br>
        Tweet with <code>#AllThingsOpen</code>
      </div>
    </div>
    <div class="container">
      <div class="row">
        <div class="col s6 offset-s3">
            <feed :tweets="tweets" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Feed from "./components/Feed.vue";
import Vue from 'vue'

export default {
  name: "app",
  components: {
    Feed
  },
  data() {
    return { 
      tweets: []
    }
  },
  methods: {
    addTweet: function(data) {
        if(this.tweets === undefined || this.tweets.length == 0) {
          this.tweets = [data];
        } else {
          this.tweets.unshift(data);
        }
    },
    connectToStream: function() {
      let es = new EventSource(process.env.VUE_APP_TWITTER_API_URL);

      es.onmessage = (event) => {
        let data = JSON.parse(event.data);
        this.addTweet(data);
      };
    },
  },
  created() {
    this.connectToStream();
  },
};
</script>

<style>
.title {
  padding: 30px;
  font-family: "Avenir", Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  font-weight: bold;
  font-size: 3.5rem;

}
#app {
  /* font-family: "Avenir", Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50; */
  /* margin-top: 60px; */
}
</style>
