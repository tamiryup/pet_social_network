package com.tamir.followear.services;

public interface NotificationService {

    void sendFollowNotification(long masterId, long slaveId);

    void sendLikeNotification(long likingUserId, long likedPostId);
}
