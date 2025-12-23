package com.example.bankcards.job;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class CardTasks {

    @Autowired
    CardRepository cardRepository;

    /**
     * <p>
     * Background task to set expired cards.
     * </p>
     * <p>
     * Runs according to a schedule at an unbusy time of day.
     * </p>
     */
    @Transactional(readOnly = true)
    @Scheduled(cron = "${update.expired-cards.job.cron}")
    public void jobSetExpired() {
        log.info("Starting update expired cards");
        cardRepository.findAllIdWithExpiredDate(LocalDate.now(), CardStatus.EXPIRED)
                .forEach(id -> {
                    try {
                        cardRepository.updateStatus(id, CardStatus.EXPIRED);
                    } catch (Exception e) {
                        log.error("Error in update expired card: id = {} , exception = {}", id, e);
                    }
                });
        log.info("Finished update expired cards");
    }

}
