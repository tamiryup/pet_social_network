package com.tamir.followear.services;

import com.google.common.collect.Lists;
import com.tamir.followear.entities.Store;
import com.tamir.followear.repositories.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class StoreService {

    @Autowired
    StoreRepository storeRepo;

    public Store create(Store store) {
        return storeRepo.save(store);
    }

    public Store findById(long id) {
        Optional<Store> store = storeRepo.findById(id);
        if(!store.isPresent()) {
            return null;
        }
        return store.get();
    }

    public Map<Long, Store> makeMapFromIds(List<Long> ids) {
        List<Store> storeList = Lists.newArrayList(storeRepo.findAllById(ids));

        Map<Long, Store> storeMap = storeList.stream()
                .collect(Collectors.toMap(store -> store.getId(), store -> store));

        return storeMap;
    }
}
