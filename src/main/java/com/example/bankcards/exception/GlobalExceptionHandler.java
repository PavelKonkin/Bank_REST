package com.example.bankcards.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений для REST API.
 * <p>
 * Этот класс перехватывает различные типы исключений, возникающих в приложении,
 * и преобразует их в стандартизированные объекты {@link ApiError}, которые затем
 * отправляются клиенту в качестве ответа с соответствующим HTTP-статусом.
 * Это обеспечивает единообразный формат ошибок и упрощает обработку ошибок на стороне клиента.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * Константа, представляющая общее сообщение о причине ошибки "Bad request" (Неверный запрос).
     * Используется для единообразия в ответах {@link ApiError}.
     */
    private static final String BAD_REQUEST = "Bad request";

    /**
     * Обрабатывает исключения, связанные с валидацией входных данных запроса
     * (например, ошибки {@code @Valid} или некорректный формат JSON).
     * <p>
     * Перехватывает:
     * <ul>
     *     <li>{@link MethodArgumentNotValidException} - когда аргумент метода, помеченный {@code @Valid}, не проходит валидацию.</li>
     *     <li>{@link BindException} - общие ошибки привязки данных.</li>
     *     <li>{@link HttpMessageNotReadableException} - когда тело HTTP-запроса не может быть прочитано (например, некорректный JSON).</li>
     * </ul>
     * Возвращает HTTP-статус {@code 400 Bad Request}.
     *
     * @param ex Исключение {@code BindException}, содержащее информацию о валидации.
     *           {@code MethodArgumentNotValidException} является подклассом {@code BindException}.
     *           {@code HttpMessageNotReadableException} также обрабатывается этим методом.
     * @return Объект {@link ApiError}, содержащий детализированные сообщения об ошибках валидации полей.
     */
    @ExceptionHandler({MethodArgumentNotValidException.class,
            BindException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(BindException ex) {
        log.info("Validation exception {}", ex.getMessage());
        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        String message = fieldErrors.stream()
                .map(item -> "Field: "
                        + item.getField() + " Error: "
                        + item.getDefaultMessage()
                        + ". Value: "
                        + item.getRejectedValue())
                .collect(Collectors.joining("\n "));
        List<String> errors = getStackTrace(ex);
        return new ApiError(errors,
                message,
                BAD_REQUEST,
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now());
    }

    /**
     * Обрабатывает общие исключения, указывающие на некорректный запрос или неверные аргументы.
     * <p>
     * Перехватывает:
     * <ul>
     *     <li>{@link IllegalArgumentException} - когда метод вызван с незаконным или неподходящим аргументом.</li>
     *     <li>{@link InvalidDataAccessApiUsageException} - при некорректном использовании API доступа к данным.</li>
     *     <li>{@link ConstraintViolationException} - когда нарушаются ограничения валидации (часто на уровне сервиса/БД).</li>
     * </ul>
     * Возвращает HTTP-статус {@code 400 Bad Request}.
     *
     * @param ex Перехваченное исключение.
     * @return Объект {@link ApiError}, содержащий сообщение об ошибке некорректного запроса.
     */
    @ExceptionHandler({IllegalArgumentException.class,
            InvalidDataAccessApiUsageException.class,
            ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(Throwable ex) {
        log.info("Validation exception {}", ex.getMessage());
        List<String> errors = getStackTrace(ex);
        return new ApiError(errors,
                ex.getMessage(),
                BAD_REQUEST,
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now());
    }

    /**
     * Обрабатывает исключения, связанные с нарушением ограничений целостности данных в базе данных.
     * <p>
     * Перехватывает:
     * <ul>
     *     <li>{@link DataIntegrityViolationException} - когда операция с базой данных нарушает ограничения целостности (например, уникальности, внешнего ключа).</li>
     * </ul>
     * Возвращает HTTP-статус {@code 409 Conflict}.
     *
     * @param ex Исключение {@code DataIntegrityViolationException}.
     * @return Объект {@link ApiError}, содержащий сообщение о нарушении целостности данных.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintViolationException(DataIntegrityViolationException ex) {
        log.info("Data integrity violation exception {}", ex.getMessage());
        List<String> errors = getStackTrace(ex);
        return new ApiError(errors,
                ex.getMessage(),
                "Integrity constraint has been violated.",
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now());
    }

    /**
     * Обрабатывает исключения, возникающие при несоответствии типов аргументов метода контроллера.
     * Это происходит, когда Spring не может преобразовать строковое представление параметра запроса
     * в ожидаемый тип данных метода (например, передача "abc" для числового ID).
     * <p>
     * Перехватывает:
     * <ul>
     *     <li>{@link MethodArgumentTypeMismatchException} - при ошибке преобразования типа аргумента.</li>
     * </ul>
     * Возвращает HTTP-статус {@code 400 Bad Request}.
     *
     * @param ex Исключение {@code MethodArgumentTypeMismatchException}.
     * @return Объект {@link ApiError}, содержащий сообщение об ошибке несоответствия типов аргументов.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleNumberFormatException(MethodArgumentTypeMismatchException ex) {
        log.info("Method argument type mismatch exception {}", ex.getMessage());
        List<String> errors = getStackTrace(ex);
        return new ApiError(errors,
                ex.getMessage(),
                BAD_REQUEST,
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now());
    }

    /**
     * Обрабатывает любые необработанные исключения ({@code Throwable}), которые не были
     * перехвачены более специфическими обработчиками.
     * <p>
     * Возвращает ответ с HTTP-статусом 500 (Internal Server Error) и подробной
     * информацией об ошибке, включая стек вызовов.
     *
     * @param ex Исключение {@code Throwable}, которое произошло.
     * @return Объект {@link ApiError}, представляющий внутреннюю ошибку сервера.
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Throwable ex) {
        List<String> errors = getStackTrace(ex);
        log.error(errors.get(0), ex);

        return new ApiError(errors,
                ex.getMessage(),
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                LocalDateTime.now());
    }

    /**
     * Обрабатывает исключения типа {@link NotFoundException}, которые возникают,
     * когда запрашиваемый ресурс не найден.
     * <p>
     * Возвращает ответ с HTTP-статусом 404 (Not Found).
     *
     * @param ex Исключение {@link NotFoundException}, которое произошло.
     * @return Объект {@link ApiError}, представляющий ошибку "ресурс не найден".
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException ex) {
        log.info("Object not found " + ex.getMessage());
        List<String> errors = getStackTrace(ex);
        return new ApiError(errors,
                ex.getMessage(),
                "Required object was not found",
                HttpStatus.NOT_FOUND.name(),
                LocalDateTime.now());

    }

/**
 * Обрабатывает исключения, связанные с неудачной аутентификацией, такие как
 * неверные учетные данные ({@link BadCredentialsException}) или отказ в авторизации
 * ({@link AuthorizationDeniedException}).
 * <p>
 * Возвращает ответ с HTTP-статусом 401 (Unauthorized) без деталей стека вызовов
 * из соображений безопасности.
 * @param ex Исключение {@link BadCredentialsException} или {@link AuthorizationDeniedException}.
 * @return Объект {@link ApiError}, представляющий ошибку аутентификации.
 */
    @ExceptionHandler({BadCredentialsException.class, AuthorizationDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return new ApiError(Collections.emptyList(), "Неверный логин или пароль",
                "Ошибка аутентификации", HttpStatus.UNAUTHORIZED.name(), LocalDateTime.now());
    }

    /**
     * Вспомогательный метод для получения полного стека вызовов (stack trace)
     * исключения в виде списка строк. Используется для включения подробной
     * информации об ошибке в ответ {@link ApiError}.
     *
     * @param ex Исключение, стек вызовов которого необходимо получить.
     * @return Список строк, содержащий полный стек вызовов исключения.
     */
    private List<String> getStackTrace(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String stackTrace = sw.toString();

        return Collections.singletonList(stackTrace);
    }
}
