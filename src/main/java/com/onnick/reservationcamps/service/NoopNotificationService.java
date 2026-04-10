package com.onnick.reservationcamps.service;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class NoopNotificationService implements NotificationPort {
    @Override
    public void reservationConfirmed(UUID reservationId) {}

    @Override
    public void reservationPaid(UUID reservationId) {}
}

