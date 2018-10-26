package com.tamir.petsocialnetwork.helpers;

import io.getstream.client.model.activities.SimpleActivity;
import io.getstream.client.model.beans.FeedFollow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreamHelper {

    public static List<Long> extractActorsFromActivites(List<? extends SimpleActivity> activities){
        List<Long> actors = new ArrayList<>();
        for(SimpleActivity activity : activities){
            actors.add(Long.parseLong(activity.getActor()));
        }
        return actors;
    }

    public static List<Long> extractObjectsFromActivities(List<? extends SimpleActivity> activities){
        List<Long> objects = new ArrayList<>();
        for(SimpleActivity activity : activities){
            objects.add(Long.parseLong(activity.getObject()));
        }
        return objects;
    }

    /**
     * extracts a map of the target and feed from a {@link FeedFollow} object
     * THE MAP:
     * "slave":id - the id of the following feed (FeedFollow.feed)
     * "master:id - the id of the followed feed (FeedFollow.target)
     *
     * @param feedFollow
     * @return a map mapping keywords to the user ids in the database
     */
    public static Map<String, Long> generateMapFeedFollow(FeedFollow feedFollow){
        String mastertString = feedFollow.getTargetId();
        String slaveString = feedFollow.getFeedId();
        long masterId = extractIdFromFeedString(mastertString);
        long slaveId = extractIdFromFeedString(slaveString);
        Map<String, Long> map = new HashMap<>();
        map.put("master", masterId);
        map.put("slave", slaveId);
        return map;
    }

    /**
     * extract the id from a stream feed string
     * @param feedString a string in the form of "user:1" or "timeline:1" etc
     * @return the id only (just 1 instead of "user:1")
     */
    public static long extractIdFromFeedString(String feedString){
        return Long.parseLong(feedString.substring(feedString.lastIndexOf(":") + 1));
    }
}
