package com.onnick.reservationcamps.service;

import java.util.UUID;

public interface NotificationPort {
    void reservationConfirmed(UUID reservationId);

    void reservationPaid(UUID reservationId);
}

