package com.iasdlc.infrastructure.web.dto;

import java.util.UUID;

/** Referencias cortas que se repiten en varias respuestas. */
public final class CommonDtos {

    private CommonDtos() {
    }

    /** { id, fullName } */
    public record PersonRef(UUID id, String fullName) {
    }

    /** { id, name } */
    public record NamedRef(UUID id, String name) {
    }
}
