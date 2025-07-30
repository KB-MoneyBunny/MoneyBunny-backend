package org.scoula.codef.util;

import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;


public class AesUtil {
    public static String KEY;
    private static final String ALGORITHM = "AES";

    static {
        try (InputStream in = AesUtil.class.getClassLoader().getResourceAsStream("application-dev.properties")) {
            Properties prop = new Properties();
            prop.load(in);
            KEY = prop.getProperty("aes.key");
            if (KEY == null) throw new RuntimeException("aes.key가 properties에 없음!");
        } catch (Exception e) {
            throw new RuntimeException("AesUtil KEY 초기화 실패!", e);
        }
    }

    public static String encrypt(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("암호화 실패", e);
        }
    }

    public static String decrypt(String cipherText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("복호화 실패", e);
        }
    }
}
