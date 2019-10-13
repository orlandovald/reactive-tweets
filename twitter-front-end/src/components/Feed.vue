<template>
<div>
    <div class="row z-depth-2 spaced teal lighten-5" v-for="tweet in tweets" :key="tweet.id">
        <div class="col s1">
            <img class="circle" v-bind:src="tweet.user.profile_image_url_https" />
        </div>
        <div class="col s11">
            <p><strong>{{ tweet.user.name }}</strong>&nbsp;<span class="grey-text">@{{ tweet.user.screen_name }}</span></p>
            <p>{{ tweet.text }}</p>

        </div>
    </div>
</div>
</template>

<script>
export default {
    name: 'feed',
    props: {
        tweets: Array,
    },
}
</script>

<style scoped>
.spaced {
    padding: 5px;
}
</style>