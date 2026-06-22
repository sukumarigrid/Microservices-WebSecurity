package org.example;

import java.util.Set;

public record UserAccount(String username, String passwordHash, Set<String> authorities) {

    public UserAccount {
        if (authorities == null) {
            authorities = Set.of();
        } else {
            authorities = Set.copyOf(authorities);
        }
    }

    public boolean hasAuthority(String authority) {
        return authorities.contains(authority);
    }
}
