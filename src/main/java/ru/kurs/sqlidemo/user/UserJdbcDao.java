package ru.kurs.sqlidemo.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class UserJdbcDao {
	private final JdbcTemplate jdbcTemplate;
	private final DataSource dataSource;

	public UserJdbcDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
		this.jdbcTemplate = jdbcTemplate;
		this.dataSource = dataSource;
	}

	private static final RowMapper<UserView> USER_VIEW_MAPPER =
			(rs, rowNum) -> new UserView(rs.getLong("id"), rs.getString("username"));

	private static final RowMapper<UserLeakRow> USER_LEAK_ROW_MAPPER =
			(rs, rowNum) -> new UserLeakRow(
					rs.getLong("id"),
					rs.getString("username"),
					rs.getString("password"),
					rs.getString("secret_note")
			);

	
	public UnsafeLoginResult loginUnsafe(String username, String password) throws SQLException {
		String sql = buildUnsafeLoginSql(username, password);
		try (Connection c = dataSource.getConnection();
		     Statement st = c.createStatement();
		     ResultSet rs = st.executeQuery(sql)) {
			if (!rs.next()) return new UnsafeLoginResult(sql, Optional.empty());
			return new UnsafeLoginResult(sql, Optional.of(new UserView(rs.getLong("id"), rs.getString("username"))));
		}
	}

	public String buildUnsafeLoginSql(String username, String password) {
		return "SELECT id, username FROM users WHERE username = '" + username + "' AND password = '" + password + "'";
	}

	public Optional<UserView> loginSafePrepared(String username, String password) {
		var rows = jdbcTemplate.query(
				"SELECT id, username FROM users WHERE username = ? AND password = ?",
				USER_VIEW_MAPPER,
				username,
				password
		);
		return rows.stream().findFirst();
	}

	
	public UnsafeSearchResult searchUnsafe(String q) {
		String sql = buildUnsafeSearchSql(q);
		return new UnsafeSearchResult(sql, jdbcTemplate.query(sql, USER_LEAK_ROW_MAPPER));
	}

	public String buildUnsafeSearchSql(String q) {
		return "SELECT id, username, password, secret_note FROM users WHERE username LIKE '%" + q + "%'";
	}

	public List<UserLeakRow> searchSafe(String q) {
		return jdbcTemplate.query(
				"SELECT id, username, password, secret_note FROM users WHERE username LIKE ?",
				USER_LEAK_ROW_MAPPER,
				"%" + q + "%"
		);
	}
}

