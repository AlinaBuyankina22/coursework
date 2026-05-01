package ru.kurs.sqlidemo.user;

public record UserLeakRow(Long id, String username, String password, String secretNote) {
}

