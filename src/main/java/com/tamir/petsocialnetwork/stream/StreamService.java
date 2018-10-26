package com.tamir.petsocialnetwork.stream;

import com.tamir.petsocialnetwork.CommonBeanConfig;
import com.tamir.petsocialnetwork.exceptions.NoMoreActivitiesException;
import com.tamir.petsocialnetwork.exceptions.StreamException;
import com.tamir.petsocialnetwork.helpers.StreamHelper;
import io.getstream.client.StreamClient;
import io.getstream.client.exception.InvalidFeedNameException;
import io.getstream.client.exception.StreamClientException;
import io.getstream.client.model.beans.FeedFollow;
import io.getstream.client.model.feeds.Feed;
import io.getstream.client.model.filters.FeedFilter;
import io.getstream.client.service.FlatActivityServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class StreamService {

    @Autowired
    StreamClientProvider streamClientProvider;

    StreamClient streamClient;

    @PostConstruct
    public void init() {
        this.streamClient = streamClientProvider.getClient();
    }

    private PostActivity createPostActivity(long userId, long postId) {
        PostActivity activity = new PostActivity();
        activity.setActor("" + userId);
        activity.setVerb("post");
        activity.setObject("" + postId);
        activity.setForeignId("" + postId);
        activity.setTime(new Date());
        return activity;
    }

    //TODO: add proper exception handling to this function
    public void uploadActivity(long userId, long postId) {
        try {
            Feed feed = streamClient.newFeed("user", "" + userId);
            FlatActivityServiceImpl<PostActivity> activityService = feed.newFlatActivityService(PostActivity.class);
            PostActivity activity = createPostActivity(userId, postId);
            PostActivity response = activityService.addActivity(activity);
            System.out.println(response);
        } catch (InvalidFeedNameException e) {
            throw new StreamException(e.getDetail());
        } catch (IOException e) {
            throw new StreamException(e.getMessage());
        } catch (StreamClientException e) {
            throw new StreamException(e.getDetail());
        }
    }

    public void follow(long masterId, long slaveId) {
        try {
            Feed slaveTimeline = streamClient.newFeed("timeline", "" + slaveId);
            slaveTimeline.follow("user", "" + masterId);
        } catch (InvalidFeedNameException e) {
            throw new StreamException(e.getDetail());
        } catch (IOException e) {
            throw new StreamException(e.getMessage());
        } catch (StreamClientException e) {
            throw new StreamException(e.getDetail());
        }
    }

    public void unfollow(long masterId, long slaveId) {
        try {
            Feed slaveTimeline = streamClient.newFeed("timeline", "" + slaveId);
            slaveTimeline.unfollow("user", "" + masterId);
        } catch (InvalidFeedNameException e) {
            throw new StreamException(e.getDetail());
        } catch (IOException e) {
            throw new StreamException(e.getMessage());
        } catch (StreamClientException e) {
            throw new StreamException(e.getDetail());
        }
    }

    public List<PostActivity> getStreamTimelineFeed(long userId, int offset) {
        int limit = CommonBeanConfig.getNumPostsPerFeedRequest();
        return getFeedActivities(userId, offset, limit, "timeline");
    }

    public List<PostActivity> getStreamUserFeed(long userId, int offset) {
        int limit = CommonBeanConfig.getNumPostsPerFeedRequest();
        return getFeedActivities(userId, offset, limit, "user");
    }

    private List<PostActivity> getFeedActivities(long userId, int offset, int limit, String feedSlug) {
        try {
            Feed feed = streamClient.newFeed(feedSlug, "" + userId);
            FlatActivityServiceImpl<PostActivity> activityService = feed.newFlatActivityService(PostActivity.class);
            FeedFilter filter = new FeedFilter.Builder().withLimit(limit).withOffset(offset).build();
            List<PostActivity> activities = activityService.getActivities(filter).getResults();
            if (activities.isEmpty())
                throw new NoMoreActivitiesException();
            return activities;
        } catch (IOException e) {
            throw new StreamException(e.getMessage());
        } catch (InvalidFeedNameException e) {
            throw new StreamException(e.getDetail());
        } catch (StreamClientException e) {
            throw new StreamException(e.getDetail());
        }
    }

    public List<Long> getUserFollowers(long userId, int offset) {
        int limit = CommonBeanConfig.getReadFollowersRequestLimit();
        try {
            Feed feed = streamClient.newFeed("user", "" + userId);
            FeedFilter filter = new FeedFilter.Builder().withLimit(limit).withOffset(offset).build();
            List<FeedFollow> followers = feed.getFollowers(filter);

            List<Long> ids = new ArrayList<>();
            for(FeedFollow follow : followers){
                ids.add(StreamHelper.generateMapFeedFollow(follow).get("slave"));
            }

            return ids;
        } catch (IOException e) {
            throw new StreamException(e.getMessage());
        } catch (InvalidFeedNameException e) {
            throw new StreamException(e.getDetail());
        } catch (StreamClientException e) {
            throw new StreamException(e.getDetail());
        }
    }

    public List<Long> getUserFollowing(long userId, int offset){
        int limit = CommonBeanConfig.getReadFollowersRequestLimit();
        try {
            Feed feed = streamClient.newFeed("timeline", "" + userId);
            FeedFilter filter = new FeedFilter.Builder().withLimit(limit).withOffset(offset).build();
            List<FeedFollow> followers = feed.getFollowing(filter);

            List<Long> ids = new ArrayList<>();
            for(FeedFollow follow : followers){
                ids.add(StreamHelper.generateMapFeedFollow(follow).get("master"));
            }

            return ids;
        } catch (IOException e) {
            throw new StreamException(e.getMessage());
        } catch (InvalidFeedNameException e) {
            throw new StreamException(e.getDetail());
        } catch (StreamClientException e) {
            throw new StreamException(e.getDetail());
        }
    }


}
