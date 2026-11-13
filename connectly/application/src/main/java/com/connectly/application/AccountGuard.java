package com.connectly.application;

import com.connectly.application.UseCases.ResolveAccount;
import com.connectly.domain.enums.AccountStatus;
import com.connectly.domain.model.Ban;
import com.connectly.domain.model.User;
import com.connectly.domain.port.BanRepository;
import com.connectly.domain.port.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Jedino mesto gde se odlučuje da li nalog sme da koristi sistem.
 * Istekla privremena zabrana se ovde automatski skida, bez čekanja na periodični job.
 */
@Transactional
public class AccountGuard implements ResolveAccount {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm 'UTC'").withZone(ZoneOffset.UTC);

    private final UserRepository users;
    private final BanRepository bans;

    public AccountGuard(UserRepository users, BanRepository bans) {
        this.users = users; this.bans = bans;
    }

    @Override
    public Optional<User> resolve(Long userId) {
        return users.findById(userId).map(this::refresh).filter(User::isActive);
    }

    /** Ako je nalog suspendovan a zabrana je istekla (ili ne postoji), vraća ga u ACTIVE. */
    public User refresh(User user) {
        if (user.status() != AccountStatus.SUSPENDED) return user;
        Optional<Ban> ban = bans.findActiveByUserId(user.id());
        if (ban.isPresent() && !ban.get().isExpiredAt(Instant.now())) return user;
        bans.deactivateAllForUser(user.id());
        return users.save(user.withStatus(AccountStatus.ACTIVE));
    }

    /** Poruka koju korisnik vidi pri pokušaju prijave na blokiran nalog. */
    public String describeBlock(User user) {
        return switch (user.status()) {
            case SUSPENDED -> {
                Optional<Ban> ban = bans.findActiveByUserId(user.id());
                String until = ban.map(Ban::endsAt).map(FMT::format).orElse("daljeg");
                String reason = ban.map(Ban::reason).orElse("nije navedeno");
                yield "Nalog je suspendovan do " + until + ". Razlog: " + reason;
            }
            case BANNED -> {
                String reason = bans.findActiveByUserId(user.id()).map(Ban::reason).orElse("nije navedeno");
                yield "Nalog je trajno blokiran. Razlog: " + reason;
            }
            case DEACTIVATED -> "Nalog je deaktiviran.";
            case ACTIVE -> "";
        };
    }
}
