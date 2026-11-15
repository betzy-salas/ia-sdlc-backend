package com.iasdlc.application.view;

import com.iasdlc.domain.model.User;

import java.util.List;

/** Usuario con sus membresías en squads. */
public record UserProfile(User user, List<MembershipView> memberships) {
}
