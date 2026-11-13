package com.connectly.app;

import com.connectly.application.UseCases.ModerationOps;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodično skida istekle privremene zabrane i šalje obaveštenje korisniku.
 * Ovo je samo dopuna: AccountGuard već pri svakom zahtevu tretira isteklu zabranu kao neaktivnu.
 */
@Component
class BanExpiryJob {
    private final ModerationOps moderation;

    BanExpiryJob(ModerationOps moderation) { this.moderation = moderation; }

    @Scheduled(fixedDelayString = "${connectly.ban-expiry-check-ms:60000}")
    void run() { moderation.expireBans(); }
}
