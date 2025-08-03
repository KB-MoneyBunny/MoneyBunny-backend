package org.scoula.security.util;

public class PasswordValidator {
    /**
     * 영문 + 숫자 + 특수문자 포함, 최소 8자 이상 검사
     */
    public static boolean isValid(String password) {
        String regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+{}\\[\\]|\\\\;:'\",.<>/?]).{8,}$";
        return password != null && password.matches(regex);
    }
}
