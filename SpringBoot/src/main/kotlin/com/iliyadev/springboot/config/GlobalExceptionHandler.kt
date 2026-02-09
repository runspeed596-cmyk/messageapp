package com.iliyadev.springboot.config

import com.iliyadev.springboot.models.ErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        // Log the warning (business logic error)
        println("BAD_REQUEST: ${e.message}")
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                success = false,
                message = e.message ?: "Invalid request"
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<ErrorResponse> {
        // Log the error
        e.printStackTrace()
        return ResponseEntity.internalServerError().body(
            ErrorResponse(
                success = false,
                message = "Internal Server Error: ${e.message}"
            )
        )
    }
}
