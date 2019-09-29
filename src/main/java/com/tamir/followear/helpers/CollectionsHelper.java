package com.tamir.followear.helpers;

import java.util.ArrayList;
import java.util.List;

public class CollectionsHelper {

    public static <T> List<T> mergeListsAlternativelyNoDuplicates(List<T> list1, List<T> list2) {
        int c1 = 0, c2 = 0;
        List<T> resultList = new ArrayList<>();

        while (c1 < list1.size() || c2 < list2.size()) {

            if (c1 < list1.size()) {
                if (!resultList.contains(list1.get(c1))) {
                    resultList.add(list1.get(c1));
                }
                c1++;
            }
            if (c2 < list2.size()) {
                if (!resultList.contains(list2.get(c2))) {
                    resultList.add(list2.get(c2));
                }
                c2++;
            }

        }

        return resultList;
    }
}
