package com.example.bankcards.util;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.example.bankcards.config.StringEncryptorProperties;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Convert
public class StringEncryptor implements AttributeConverter<String, String> {

    StringEncryptorProperties properties;

    public StringEncryptor(StringEncryptorProperties properties) {
        this.properties = properties;
    }

    private SecretKeySpec getKey() {
        return new SecretKeySpec(properties.secretKey().getBytes(), properties.algorithm());
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            if (attribute == null)
                return null;

            Cipher cipher = Cipher.getInstance(properties.algorithm());
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            byte[] encrypted = cipher.doFinal(attribute.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при шифровании строки", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null)
                return null;

            Cipher cipher = Cipher.getInstance(properties.algorithm());
            cipher.init(Cipher.DECRYPT_MODE, getKey());
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(dbData));
            return new String(decrypted);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при дешифровке строки", e);
        }
    }

}
