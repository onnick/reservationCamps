package com.onnick.reservationcamps.api;

import com.onnick.reservationcamps.api.dto.CreateCampRequest;
import com.onnick.reservationcamps.api.dto.CreateSessionRequest;
import com.onnick.reservationcamps.api.dto.IdResponse;
import com.onnick.reservationcamps.service.CampService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/camps")
public class CampController {
    private final CampService campService;
    private final ActorResolver actorResolver;

    public CampController(CampService campService, ActorResolver actorResolver) {
        this.campService = campService;
        this.actorResolver = actorResolver;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse createCamp(@RequestHeader HttpHeaders headers, @Valid @RequestBody CreateCampRequest request) {
        var actor = actorResolver.resolve(headers);
        var camp = campService.createCamp(actor, request.name(), request.basePriceCents());
        return new IdResponse(camp.getId());
    }

    @PostMapping("/{campId}/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse createSession(
            @RequestHeader HttpHeaders headers,
            @PathVariable UUID campId,
            @Valid @RequestBody CreateSessionRequest request) {
        var actor = actorResolver.resolve(headers);
        var session =
                campService.createSession(actor, campId, request.startDate(), request.endDate(), request.capacity());
        return new IdResponse(session.getId());
    }
}

