package ru.kurs.sqlidemo.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kurs.sqlidemo.user.UnsafeSearchResult;
import ru.kurs.sqlidemo.user.UserJdbcDao;
import ru.kurs.sqlidemo.user.UserLeakRow;
import ru.kurs.sqlidemo.user.UserService;
import ru.kurs.sqlidemo.user.UserView;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.sql.SQLException;
import java.util.List;

@Controller
@RequestMapping("/")
@Validated
public class DemoController {
	private final UserJdbcDao users;
	private final UserService userService;

	public DemoController(UserJdbcDao users, UserService userService) {
		this.users = users;
		this.userService = userService;
	}

	@GetMapping
	public String index(Model model) {
		model.addAttribute("loginUsername", "alice");
		model.addAttribute("loginPassword", "alice123");
		model.addAttribute("searchQ", "a");
		return "index";
	}

	@PostMapping("/login/unsafe")
	public String loginUnsafe(
			@RequestParam @NotBlank @Size(max = 64) String username,
			@RequestParam @NotBlank @Size(max = 64) String password,
			Model model
	) throws SQLException {
		var r = users.loginUnsafe(username, password);
		seedIndexDefaults(model);
		model.addAttribute("loginUsername", username);
		model.addAttribute("loginPassword", password);
		model.addAttribute("loginMode", "unsafe");
		model.addAttribute("loginSql", r.sql());
		model.addAttribute("loginResultUser", r.user().orElse(null));
		model.addAttribute("loginOk", r.user().isPresent());
		return "index";
	}

	@PostMapping("/login/safe")
	public String loginSafe(
			@RequestParam @NotBlank @Size(max = 64) String username,
			@RequestParam @NotBlank @Size(max = 64) String password,
			Model model
	) {
		var r = users.loginSafePrepared(username, password);
		seedIndexDefaults(model);
		model.addAttribute("loginUsername", username);
		model.addAttribute("loginPassword", password);
		model.addAttribute("loginMode", "safe");
		model.addAttribute("loginSql", "SELECT id, username FROM users WHERE username = ? AND password = ?");
		model.addAttribute("loginResultUser", r.orElse(null));
		model.addAttribute("loginOk", r.isPresent());
		return "index";
	}

	@PostMapping("/login/safe-orm")
	public String loginSafeOrm(
			@RequestParam @NotBlank @Size(max = 64) String username,
			@RequestParam @NotBlank @Size(max = 64) String password,
			Model model
	) {
		var r = userService.loginSafeOrm(username, password);
		seedIndexDefaults(model);
		model.addAttribute("loginUsername", username);
		model.addAttribute("loginPassword", password);
		model.addAttribute("loginMode", "safe(orm)");
		model.addAttribute("loginSql", "ORM: users.findByUsername(username) (no SQL concatenation)");
		model.addAttribute("loginResultUser", r.orElse(null));
		model.addAttribute("loginOk", r.isPresent());
		return "index";
	}

	@GetMapping("/search/unsafe")
	public String searchUnsafe(
			@RequestParam(name = "q", required = false) @Size(max = 64) String q,
			Model model
	) {
		String query = (q == null ? "" : q);
		UnsafeSearchResult r = users.searchUnsafe(query);
		seedIndexDefaults(model);
		model.addAttribute("searchMode", "unsafe");
		model.addAttribute("searchQ", query);
		model.addAttribute("searchSql", r.sql());
		model.addAttribute("searchRows", r.rows());
		return "index";
	}

	@GetMapping("/search/safe")
	public String searchSafe(
			@RequestParam(name = "q", required = false) @Size(max = 64) String q,
			Model model
	) {
		String query = (q == null ? "" : q);
		List<UserLeakRow> rows = users.searchSafe(query);
		seedIndexDefaults(model);
		model.addAttribute("searchMode", "safe(prepared)");
		model.addAttribute("searchQ", query);
		model.addAttribute("searchSql", "SELECT id, username, password, secret_note FROM users WHERE username LIKE ?");
		model.addAttribute("searchRows", rows);
		return "index";
	}

	@GetMapping("/search/safe-orm")
	public String searchSafeOrm(
			@RequestParam(name = "q", required = false)
			@Size(max = 64)
			@Pattern(regexp = "^[\\p{L}0-9_\\- ]*$", message = "Допустимы только буквы/цифры/пробел/_/-")
			String q,
			Model model
	) {
		String query = (q == null ? "" : q);
		List<UserView> rows = userService.searchSafeOrm(query);
		seedIndexDefaults(model);
		model.addAttribute("searchMode", "safe(orm)");
		model.addAttribute("searchQ", query);
		model.addAttribute("searchSql", "ORM: users.findByUsernameContainingIgnoreCase(q) (parameterized)");
		model.addAttribute("searchRowsSafeOrm", rows);
		return "index";
	}

	private static void seedIndexDefaults(Model model) {
		if (!model.containsAttribute("loginUsername")) model.addAttribute("loginUsername", "alice");
		if (!model.containsAttribute("loginPassword")) model.addAttribute("loginPassword", "alice123");
		if (!model.containsAttribute("searchQ")) model.addAttribute("searchQ", "a");
		if (!model.containsAttribute("searchRows")) model.addAttribute("searchRows", List.of());
		if (!model.containsAttribute("searchRowsSafeOrm")) model.addAttribute("searchRowsSafeOrm", List.of());
	}
}

