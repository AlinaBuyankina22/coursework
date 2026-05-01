package ru.kurs.sqlidemo.web;

import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class ValidationErrorAdvice {
	@ExceptionHandler(ConstraintViolationException.class)
	public String onConstraintViolation(ConstraintViolationException ex, RedirectAttributes ra) {
		var msg = ex.getConstraintViolations().stream()
				.findFirst()
				.map(v -> v.getPropertyPath() + ": " + v.getMessage())
				.orElse("Некорректный ввод");
		ra.addFlashAttribute("validationError", msg);
		return "redirect:/";
	}
}

