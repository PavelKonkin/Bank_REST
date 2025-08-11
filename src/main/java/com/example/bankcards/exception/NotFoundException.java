package com.example.bankcards.exception;

/**
 * Исключение, выбрасываемое, когда запрашиваемый ресурс (например, объект в базе данных) не найден.
 * <p>
 * Наследуется от {@link RuntimeException}, что делает его непроверяемым (unchecked) исключением.
 * Это позволяет использовать его для обозначения бизнес-логических ошибок, связанных с отсутствием данных,
 * без необходимости явной обработки {@code throws} в сигнатурах методов (хотя его можно перехватить,
 * если требуется специфическая обработка).
 * <p>
 * Обычно сопровождается HTTP-статусом {@code 404 Not Found} в REST API.
 */
public class NotFoundException extends RuntimeException {
    /**
     * Создает новый экземпляр {@code NotFoundException} с указанным подробным сообщением.
     *
     * @param message Подробное сообщение, описывающее причину отсутствия ресурса.
     *                Это сообщение будет доступно через метод {@link Throwable#getMessage()}.
     */
    public NotFoundException(String message) {
        super(message);
    }
}