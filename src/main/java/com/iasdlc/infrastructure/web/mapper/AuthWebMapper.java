package com.iasdlc.infrastructure.web.mapper;

import com.iasdlc.application.view.AuthResult;
import com.iasdlc.application.view.UserProfile;
import com.iasdlc.domain.model.User;
import com.iasdlc.infrastructure.web.dto.AuthDtos.AuthResponse;
import com.iasdlc.infrastructure.web.dto.AuthDtos.MembershipResponse;
import com.iasdlc.infrastructure.web.dto.AuthDtos.UserResponse;
import com.iasdlc.infrastructure.web.dto.AuthDtos.UserSummaryResponse;

public final class AuthWebMapper {

    private AuthWebMapper() {
    }

    public static AuthResponse toResponse(AuthResult result) {
        return new AuthResponse(result.token(), toResponse(result.profile()));
    }

    public static UserResponse toResponse(UserProfile profile) {
        User user = profile.user();
        return new UserResponse(user.id(), user.fullName(), user.email(),
                profile.memberships().stream()
                        .map(m -> new MembershipResponse(m.squadId(), m.squadName(), m.role(), m.substitute()))
                        .toList());
    }

    public static UserSummaryResponse toSummary(User user) {
        return new UserSummaryResponse(user.id(), user.fullName(), user.email());
    }
}
