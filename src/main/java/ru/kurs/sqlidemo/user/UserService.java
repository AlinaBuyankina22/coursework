package ru.kurs.sqlidemo.user;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
	private final UserRepository users;

	public UserService(UserRepository users) {
		this.users = users;
	}

	
	public Optional<UserView> loginSafeOrm(String username, String password) {
		return users.findByUsername(username)
				.filter(u -> u.getPassword().equals(password))
				.map(u -> new UserView(u.getId(), u.getUsername()));
	}

	
	public List<UserView> searchSafeOrm(String q) {
		return users.findByUsernameContainingIgnoreCase(q).stream()
				.map(u -> new UserView(u.getId(), u.getUsername()))
				.toList();
	}
}

