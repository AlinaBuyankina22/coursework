package ru.kurs.sqlidemo.user;

import java.util.Optional;

public record UnsafeLoginResult(String sql, Optional<UserView> user) {
}

