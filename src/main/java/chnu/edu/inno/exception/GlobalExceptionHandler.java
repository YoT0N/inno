package chnu.edu.inno.exception;

import chnu.edu.inno.model.dto.response.ApiResponse;
import chnu.edu.inno.model.dto.response.BaseMetaData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Наші бізнес-помилки (404, 400, 409 ...)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<BaseMetaData, Void>> handleApiException(ApiException ex) {
        return build(ex.getStatus(), ex.getMessage());
    }

    // Проблеми з MongoDB
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<BaseMetaData, Void>> handleDatabase(DataAccessException ex) {
        log.error("Database error", ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Database is unavailable, try again later");
    }

    // Все інше, чого не передбачили
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<BaseMetaData, Void>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
    }

    // Стандартні помилки Spring MVC віддаємо в нашому форматі
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        String message = (body instanceof ProblemDetail pd && pd.getDetail() != null)
                ? pd.getDetail()
                : ex.getMessage();
        BaseMetaData meta = new BaseMetaData(statusCode.value(), false, message);
        return ResponseEntity.status(statusCode)
                .headers(headers)
                .body(new ApiResponse<BaseMetaData, Void>(meta));
    }

    private static ResponseEntity<ApiResponse<BaseMetaData, Void>> build(HttpStatusCode status, String message) {
        BaseMetaData meta = new BaseMetaData(status.value(), false, message);
        return ResponseEntity.status(status).body(new ApiResponse<>(meta));
    }
}