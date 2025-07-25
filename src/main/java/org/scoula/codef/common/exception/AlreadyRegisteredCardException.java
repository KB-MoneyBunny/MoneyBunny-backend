package org.scoula.codef.common.exception;

public class AlreadyRegisteredCardException extends RuntimeException {
    public AlreadyRegisteredCardException(String cardMaskedNumber) {
        super("이미 등록된 카드입니다: " + cardMaskedNumber);
    }
}
