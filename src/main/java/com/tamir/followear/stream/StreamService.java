package com.tamir.followear.stream;

import com.tamir.followear.CommonBeanConfig;
import com.tamir.followear.entities.Post;
import com.tamir.followear.exceptions.NoMoreActivitiesException;
import com.tamir.followear.exceptions.CustomStreamException;
import com.tamir.followear.helpers.StreamHelper;
import io.getstream.client.Client;
import io.getstream.client.FlatFeed;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.Activity;
import io.getstream.core.models.FollowRelation;
import io.getstream.core.options.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class StreamService {

    private final Logger logger = LoggerFactory.getLogger(StreamService.class);

    @Autowired
    StreamClientProvider streamClientProvider;

    Client streamClient;

    @PostConstruct
    public void init() {
        this.streamClient = streamClientProvider.getClient();
    }

    private Activity createPostActivity(Post post) {
        Activity activity = Activity.builder()
                .actor("" + post.getUserId())
                .verb("post")
                .object("" + post.getId())
                .foreignID("" + post.getId())
                .time(post.getCreateDate())
                .build();
        return activity;
    }

    private Activity createSaveActivity(long userId, Post post) {
        Activity activity = Activity.builder()
                .actor("" + userId)
                .verb("save")
                .object("" + post.getId())
                .foreignID("" + post.getId())
                .time(post.getCreateDate())
                .build();
        return activity;
    }

    public void uploadActivity(Post post) {
        try {
            FlatFeed feed = streamClient.flatFeed("user", "" + post.getUserId());
            Activity activity = createPostActivity(post);
            Activity response = feed.addActivity(activity).get();
            System.out.println(response);
        } catch (StreamException e) {
            throw new CustomStreamException(e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            throw new CustomStreamException("Problem with stream 'Future' Calculation");
        }
    }

    public void saveItem(long userId, Post post) {
        try {
            FlatFeed feed = streamClient.flatFeed("saved", "" + userId);
            Activity saveActivity = createSaveActivity(userId, post);
            Activity response = feed.addActivity(saveActivity).get();
            logger.info("saved activity: " + response);
        } catch (StreamException e) {
            throw new CustomStreamException(e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            throw new CustomStreamException("Problem with stream 'Future' Calculation");
        }
    }

    public void unsaveItem(long userId, Post post) {
        try {
            FlatFeed savedFeed = streamClient.flatFeed("saved", "" + userId);
            savedFeed.removeActivityByForeignID("" + post.getId());
        } catch (StreamException e) {
            throw new CustomStreamException(e.getMessage());
        }
    }

    public void removeActivity(Post post) {
        try {
            FlatFeed userFeed = streamClient.flatFeed("user", "" + post.getUserId());
            userFeed.removeActivityByForeignID("" + post.getId());
        } catch (StreamException e) {
            throw new CustomStreamException(e.getMessage());
        }
    }

    public void hideActivity(long userId, Post post) {
        try {
            FlatFeed timelineFeed = streamClient.flatFeed("timeline", "" + userId);
            timelineFeed.removeActivityByForeignID("" + post.getId());
        } catch (StreamException e) {
            throw new CustomStreamException(e.getMessage());
        }
    }

    public void follow(long masterId, long slaveId) {
        try {
            FlatFeed slaveTimelineFeed = streamClient.flatFeed("timeline", "" + slaveId);
            FlatFeed masterUserFeed = streamClient.flatFeed("user", "" + masterId);
            slaveTimelineFeed.follow(masterUserFeed);
        } catch (StreamException e) {
            throw new CustomStreamException(e.getMessage());
        }
    }

    public void unfollow(long masterId, long slaveId) {
        try {
            FlatFeed slaveTimeline = streamClient.flatFeed("timeline", "" + slaveId);
            FlatFeed masterUserFeed = streamClient.flatFeed("user", "" + masterId);
            slaveTimeline.unfollow(masterUserFeed);
        } catch (StreamException e) {
            throw new CustomStreamException(e.getMessage());
        }
    }

    public List<Activity> getStreamTimelineFeed(long userId, int offset, int limit) {
        return getFeedActivities(userId, offset, limit, "timeline");
    }

    public List<Activity> getStreamUserFeed(long userId, int offset, int limit) {
        return getFeedActivities(userId, offset, limit, "user");
    }

    public List<Activity> getStreamSavedFeed(long userId, int offset, int limit) {
        return getFeedActivities(userId, offset, limit, "saved");
    }

    private List<Activity> getFeedActivities(long userId, int offset, int limit, String feedSlug) {
        try {
            FlatFeed feed = streamClient.flatFeed(feedSlug, "" + userId);
            Pagination pagination = new Pagination().limit(limit).offset(offset);
            List<Activity> activities = feed.getActivities(pagination).join();
            if (activities.isEmpty())
                throw new NoMoreActivitiesException();
            return activities;
        } catch (StreamException e) {
            throw new CustomStreamException(e.getMessage());
        }
    }

    public List<Long> getUserFollowers(long userId, int offset) {
        int limit = CommonBeanConfig.getReadFollowersRequestLimit();
        try {
            FlatFeed feed = streamClient.flatFeed("user", "" + userId);
            Pagination pagination = new Pagination().limit(limit).offset(offset);
            List<FollowRelation> followers = feed.getFollowers(pagination).get();

            List<Long> ids = new ArrayList<>();
            for (FollowRelation follow : followers) {
                ids.add(StreamHelper.generateMapFeedFollow(follow).get("slave"));
            }

            return ids;
        } catch (StreamException e) {
            throw new CustomStreamException(e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            throw new CustomStreamException("Problem with stream 'Future' Calculation");
        }
    }

    public List<Long> getUserFollowing(long userId, int offset) {
        int limit = CommonBeanConfig.getReadFollowersRequestLimit();
        try {
            FlatFeed feed = streamClient.flatFeed("timeline", "" + userId);
            Pagination pagination = new Pagination().limit(limit).offset(offset);
            List<FollowRelation> followers = feed.getFollowed(pagination).get();

            List<Long> ids = new ArrayList<>();
            for (FollowRelation follow : followers) {
                ids.add(StreamHelper.generateMapFeedFollow(follow).get("master"));
            }

            return ids;
        } catch (StreamException e) {
            throw new CustomStreamException(e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            throw new CustomStreamException("Problem with stream 'Future' Calculation");
        }
    }


}
