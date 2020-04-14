package com.tamir.followear.helpers;

import io.getstream.core.models.Activity;
import io.getstream.core.models.FollowRelation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreamHelper {

    public static List<Long> extractActorsFromActivites(List<Activity> activities){
        List<Long> actors = new ArrayList<>();
        for(Activity activity : activities){
            actors.add(Long.parseLong(activity.getActor()));
        }
        return actors;
    }

    public static List<Long> extractObjectsFromActivities(List<Activity> activities){
        List<Long> objects = new ArrayList<>();
        for(Activity activity : activities){
            objects.add(Long.parseLong(activity.getObject()));
        }
        return objects;
    }

    /**
     * extracts a map of the target and feed from a {@link FollowRelation} object
     * THE MAP:
     * "slave":id - the id of the following feed (FeedFollow.source)
     * "master:id - the id of the followed feed (FeedFollow.target)
     *
     * @param followRelation
     * @return a map mapping keywords to the user ids in the database
     */
    public static Map<String, Long> generateMapFeedFollow(FollowRelation followRelation){
        String mastertString = followRelation.getTarget();
        String slaveString = followRelation.getSource();
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
