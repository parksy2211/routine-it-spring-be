package com.goormi.routine.domain.group.entity;

import lombok.Getter;

@Getter
public enum GroupType {
    FREE("자유참여"),
    REQUIRED("의무참여");

    private final String description;

    GroupType(final String description) {
        this.description = description;
    }
}
