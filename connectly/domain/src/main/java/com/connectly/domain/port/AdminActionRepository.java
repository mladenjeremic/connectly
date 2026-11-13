package com.connectly.domain.port;

import com.connectly.domain.model.AdminAction;
import java.util.List;

public interface AdminActionRepository {
    AdminAction save(AdminAction action);
    List<AdminAction> findAll(int page, int size);
    List<AdminAction> findByTargetUser(Long targetUserId, int page, int size);
}
