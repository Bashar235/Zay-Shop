package com.example.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import java.util.stream.Collectors;

@Controller
public class CustomErrorController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("jakarta.servlet.error.message");
        String exceptionType = (String) request.getAttribute("jakarta.servlet.error.exception_type");

        logger.error("Error occurred: Status={}, Message={}, Exception={}", statusCode, errorMessage, exceptionType);

        model.addAttribute("status", statusCode != null ? statusCode : 500);
        model.addAttribute("error", errorMessage != null && !errorMessage.isEmpty() ? errorMessage : "Internal Server Error");
        model.addAttribute("message", "An unexpected error occurred. Please try again or contact support.");
        return "error";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundException(NoResourceFoundException ex, Model model) {
        logger.error("Resource not found: {}", ex.getMessage());
        if (ex.getResourcePath().equals("logout")) {
            return "redirect:/login?logout";
        }
        model.addAttribute("status", 404);
        model.addAttribute("error", "Resource not found: " + ex.getResourcePath());
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingParams(MissingServletRequestParameterException ex, Model model, RedirectAttributes redirectAttributes) {
        logger.error("Missing parameter: {}", ex.getMessage());
        String paramName = ex.getParameterName();
        redirectAttributes.addFlashAttribute("error", "Required parameter '" + paramName + "' is missing");
        if (paramName.equals("recaptchaToken")) {
            return "redirect:/user/register";
        }
        return "redirect:/error";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException ex, RedirectAttributes redirectAttributes) {
        logger.error("Illegal state: {}", ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/user/register";
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String handleConstraintViolation(ConstraintViolationException ex, RedirectAttributes redirectAttributes) {
        logger.error("Validation error: {}", ex.getMessage(), ex);
        String errorMessage = ex.getConstraintViolations().stream()
                .map(cv -> cv.getMessage())
                .collect(Collectors.joining(", "));
        redirectAttributes.addFlashAttribute("error", errorMessage);
        return "redirect:/user/register";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, RedirectAttributes redirectAttributes) {
        logger.error("Runtime error: {}", ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/admin/dashboard";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        model.addAttribute("status", 500);
        model.addAttribute("error", "An unexpected error occurred. Please try again.");
        model.addAttribute("message", ex.getMessage());
        return "error";
    }
}