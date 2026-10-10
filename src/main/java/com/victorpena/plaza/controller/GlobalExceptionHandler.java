package com.victorpena.plaza.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public String denied(Model m) {
    m.addAttribute("errorMessage", "You do not have access to this record or company.");
    return "error/403";
  }

  @ExceptionHandler(ResponseStatusException.class)
  public String status(ResponseStatusException e, Model m, HttpServletResponse r) {
    r.setStatus(e.getStatusCode().value());
    m.addAttribute("errorMessage", e.getReason());
    return "error/404";
  }

  @ExceptionHandler({
    IllegalArgumentException.class,
    org.springframework.web.multipart.MaxUploadSizeExceededException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String invalid(Exception e, Model m) {
    m.addAttribute(
        "errorMessage",
        e instanceof IllegalArgumentException
            ? e.getMessage()
            : "The submitted form is invalid or the upload is too large.");
    return "error/400";
  }

  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public String conflict(Model m) {
    m.addAttribute(
        "errorMessage",
        "This record conflicts with an existing record. Reload the page and check your entries.");
    return "error/400";
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String unexpected(Exception e, Model m) {
    log.error("Request failed", e);
    m.addAttribute(
        "errorMessage",
        "The operation could not be completed. Your manager can check the application log.");
    return "error/500";
  }
}
