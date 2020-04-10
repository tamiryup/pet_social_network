package com.tamir.followear.jpaKeys;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
public class LikeKey implements Serializable {

    private long userId;
    private long postId;

}
