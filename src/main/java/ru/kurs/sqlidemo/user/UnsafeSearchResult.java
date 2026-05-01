package ru.kurs.sqlidemo.user;

import java.util.List;

public record UnsafeSearchResult(String sql, List<UserLeakRow> rows) {
}

