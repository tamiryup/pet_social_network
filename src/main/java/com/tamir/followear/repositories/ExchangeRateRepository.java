package com.tamir.followear.repositories;

import com.tamir.followear.entities.ExchangeRate;
import com.tamir.followear.jpaKeys.ExchangeRateId;
import org.springframework.data.repository.CrudRepository;

public interface ExchangeRateRepository extends CrudRepository<ExchangeRate, ExchangeRateId> {
}
