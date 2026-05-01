package ru.kurs.sqlidemo.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 64)
	private String username;

	@Column(nullable = false, length = 128)
	private String password;

	@Column(name = "secret_note", nullable = false, length = 512)
	private String secretNote;

	protected User() {
	}

	public User(String username, String password, String secretNote) {
		this.username = username;
		this.password = password;
		this.secretNote = secretNote;
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getSecretNote() {
		return secretNote;
	}
}

