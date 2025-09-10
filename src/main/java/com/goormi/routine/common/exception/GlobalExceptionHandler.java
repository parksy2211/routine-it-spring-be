package com.goormi.routine.common.exception;

import com.goormi.routine.domain.calendar.exception.CalendarAlreadyConnectedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(basePackages = "com.goormi.routine")
public class GlobalExceptionHandler {

    /**
     * @Valid 검증 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        log.error("Validation error: {}", ex.getMessage());
        
        String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(errorMessage));
    }

    /**
     * JSON 파싱 실패 시 발생하는 예외 처리 (LocalTime 파싱 에러 포함)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        
        log.error("JSON parsing error: {}", ex.getMessage());
        
        String errorMessage = "잘못된 요청 형식입니다.";
        
        // LocalTime 파싱 에러인지 확인
        if (ex.getMessage() != null && ex.getMessage().contains("LocalTime")) {
            errorMessage = "시간 형식이 올바르지 않습니다. HH:mm 형식으로 입력해주세요. (예: 09:30)";
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(errorMessage));
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        log.error("IllegalArgument error: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(ex.getMessage()));
    }

    /**
     * 캘린더 이미 연동된 예외 처리
     */
    @ExceptionHandler(CalendarAlreadyConnectedException.class)
    public ResponseEntity<ErrorResponse> handleCalendarAlreadyConnectedException(
            CalendarAlreadyConnectedException ex) {
        
        log.error("Calendar already connected error: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse.of(ex.getMessage()));
    }

    /**
     * 캘린더 관련 일반 런타임 예외 처리
     */
    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<ErrorResponse> handleCalendarRuntimeException(
            RuntimeException ex) {
        
        // 카카오 API 관련 에러인 경우
        if (ex.getMessage() != null && ex.getMessage().contains("캘린더")) {
            log.error("Calendar API error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ErrorResponse.of("캘린더 서비스 연동 중 오류가 발생했습니다."));
        }
        
        log.error("Runtime error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of("처리 중 오류가 발생했습니다."));
    }

    /**
     * 기타 모든 예외 처리
     */
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
//
//        log.error("Unexpected error: ", ex);
//
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//            .body(ErrorResponse.of("서버 내부 오류가 발생했습니다."));
//    }
}
