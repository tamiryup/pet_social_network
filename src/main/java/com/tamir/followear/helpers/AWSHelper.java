package com.tamir.followear.helpers;

import com.amazonaws.services.cognitoidp.model.AttributeType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AWSHelper {

    public static Map<String, String> createMapFromAttributeTypes(List<AttributeType> attributeTypes) {
        Map<String, String> map = new HashMap<>();

        for(AttributeType attr : attributeTypes) {
            map.put(attr.getName(), attr.getValue());
        }

        return map;
    }
}
