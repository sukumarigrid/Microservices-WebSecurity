package org.example;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class UserAccountRepository {

    private final ConcurrentMap<String, UserAccount> accounts = new ConcurrentHashMap<>();

    public Optional<UserAccount> findByUsername(String username) {
        return Optional.ofNullable(accounts.get(username));
    }

    public UserAccount save(UserAccount account) {
        accounts.put(account.username(), account);
        return account;
    }

    public List<UserAccount> findAll() {
        return accounts.values().stream()
                .sorted(Comparator.comparing(UserAccount::username))
                .toList();
    }

    public long count() {
        return accounts.size();
    }

    public boolean exists(String username) {
        return accounts.containsKey(username);
    }

    public Collection<UserAccount> allValues() {
        return List.copyOf(accounts.values());
    }
}
