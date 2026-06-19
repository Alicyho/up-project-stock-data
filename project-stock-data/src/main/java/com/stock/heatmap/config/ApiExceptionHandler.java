package com.stock.heatmap.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler({org.springframework.transaction.TransactionException.class, DataAccessException.class})
    public ResponseEntity<Map<String, Object>> handleDataAccess(Exception ex) {
        log.error("Database error", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorBody(
                "database_error",
                "Database is unavailable or sp500_top300 table is missing. Check Supabase connection and schema.",
                ex
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unhandled API error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody(
                "internal_error",
                ex.getMessage() != null ? ex.getMessage() : "Unexpected server error",
                ex
        ));
    }

    private static Map<String, Object> errorBody(String code, String message, Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", code);
        body.put("message", message);
        body.put("detail", ex.getClass().getSimpleName() + ": " + String.valueOf(ex.getMessage()));
        return body;
    }
}
