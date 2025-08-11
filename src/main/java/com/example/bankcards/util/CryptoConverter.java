package com.example.bankcards.util;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * <p>Конвертер атрибутов JPA, предназначенный для автоматического шифрования
 * и дешифрования строковых данных при сохранении их в базу данных и извлечении из нее.</p>
 *
 * <p>Использует алгоритм шифрования AES в режиме GCM (Galois/Counter Mode)
 * с отсутствием дополнения (NoPadding), что обеспечивает конфиденциальность,
 * целостность и аутентификацию данных. Для каждого шифрования генерируется
 * уникальный Initialization Vector (IV), который хранится вместе с зашифрованными данными.</p>
 *
 * <p>Ключ шифрования извлекается из свойств приложения (например, {@code application.properties})
 * с помощью {@code @Value("${encryption.secret.key}")}. Этот ключ должен быть
 * Base64-кодированным и иметь соответствующую длину для AES (например, 32 байта для AES-256).</p>
 *
 * <p>Аннотации {@code @Converter} и {@code @Component} позволяют JPA автоматически
 * обнаруживать этот конвертер и применять его к атрибутам сущностей,
 * помеченным как {@code @Convert(converter = CryptoConverter.class)}.</p>
 */
@Converter
@Component
public class CryptoConverter implements AttributeConverter<String, String> {
    /**
     * Алгоритм шифрования, используемый для данных.
     * Формат: "Алгоритм/Режим/Дополнение" (например, "AES/GCM/NoPadding").
     */
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    /**
     * Длина Initialization Vector (IV) в байтах для режима GCM.
     * Рекомендуемая длина для GCM составляет 12 байт.
     */
    private static final int GCM_IV_LENGTH = 12;

    /**
     * Длина аутентификационного тега GCM в байтах.
     * 16 байт (128 бит) - стандартная и рекомендуемая длина.
     */
    private static final int GCM_TAG_LENGTH = 16;

    /**
     * Секретный ключ шифрования, инжектируемый из свойств приложения.
     * Ожидается, что ключ будет в формате Base64-строки.
     */
    @Value("${encryption.secret.key}")
    private String secretKey;

    /**
     * Объект {@link Key}, используемый для операций шифрования и дешифрования.
     * Инициализируется из {@code secretKey} после создания компонента.
     */
    private Key key;

    /**
     * Генератор криптографически стойких случайных чисел,
     * используемый для создания уникальных Initialization Vectors (IV).
     */
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Метод инициализации, вызываемый после создания и внедрения зависимостей.
     * Декодирует Base64-кодированный секретный ключ и создает объект {@link SecretKeySpec}
     * для использования в операциях шифрования/дешифрования.
     */
    @PostConstruct
    public void init() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    /**
     * Преобразует строковый атрибут сущности (plain text) в формат,
     * пригодный для хранения в базе данных (зашифрованный и Base64-кодированный).
     *
     * <p>Процесс шифрования:</p>
     * <ol>
     *     <li>Генерируется уникальный случайный Initialization Vector (IV).</li>
     *     <li>Инициализируется {@link Cipher} в режиме шифрования с использованием ключа и IV.</li>
     *     <li>Исходные данные шифруются.</li>
     *     <li>IV и зашифрованные данные объединяются в один массив байтов.</li>
     *     <li>Объединенные байты кодируются в Base64-строку для безопасного хранения в текстовом поле БД.</li>
     * </ol>
     *
     * @param attribute Исходная строка, которую необходимо зашифровать.
     *                  * @return Зашифрованная и Base64-кодированная строка, готовая для сохранения в БД.
     *                  * @throws IllegalStateException Если произошла ошибка во время процесса шифрования.
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);

            byte[] encryptedData = cipher.doFinal(attribute.getBytes());

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedData);

            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt attribute", e);
        }
    }

    /**
     * Преобразует данные из базы данных (зашифрованная и Base64-кодированная строка)
     * обратно в строковый атрибут сущности (plain text).
     *
     * <p>Процесс дешифрования:</p>
     * <ol>
     *     <li>Base64-кодированная строка декодируется обратно в массив байтов.</li>
     *     <li>Из массива байтов извлекается IV.</li>
     *     <li>Оставшаяся часть массива байтов считается зашифрованными данными.</li>
     *     <li>Инициализируется {@link Cipher} в режиме дешифрования с использованием ключа и извлеченного IV.</li>
     *     <li>Зашифрованные данные дешифруются.</li>
     * </ol>
     *
     * @param dbData Зашифрованная и Base64-кодированная строка, полученная из БД.
     * @return Дешифрованная строка (plain text).
     * @throws IllegalStateException Если произошла ошибка во время процесса дешифрования.
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(dbData);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedBytes);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            byte[] encryptedData = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedData);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);

            return new String(cipher.doFinal(encryptedData));

        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt attribute", e);
        }
    }
}
