package com.example.bankcards.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Класс {@code ApiError} представляет собой стандартизированный ответ об ошибке
 * для REST API. Он используется для предоставления подробной структурированной информации
 * о возникшей ошибке клиенту.
 * <p>
 * Этот класс обычно сериализуется в JSON и отправляется в теле HTTP-ответа
 * с соответствующим кодом состояния (например, 400 Bad Request, 404 Not Found, 500 Internal Server Error).
 */
@Data
@RequiredArgsConstructor
public class ApiError {
    /**
     * Список более детализированных сообщений об ошибках, которые могут быть связаны
     * с валидацией полей, бизнес-логикой или другими специфическими проблемами.
     * Может быть пустым, если ошибка является общей.
     */
    private final List<String> errors;
    /**
     * Краткое, общее сообщение об ошибке, предназначенное для пользователя или разработчика.
     * Описывает суть произошедшего.
     */
    private final String message;
    /**
     * Более подробное описание причины возникновения ошибки. Часто содержит
     * техническую информацию или контекст, который помогает понять, почему произошла ошибка.
     */
    private final String reason;
    /**
     * HTTP-статус код ошибки в виде строки (например, "BAD_REQUEST", "NOT_FOUND", "INTERNAL_SERVER_ERROR").
     * Соответствует HTTP-коду состояния, отправленному в заголовке ответа.
     */
    private final String status;

    /**
     * Метка времени, указывающая, когда произошла ошибка.
     * При сериализации в JSON форматируется как строка в формате "yyyy-MM-dd HH:mm:ss".
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;
}
