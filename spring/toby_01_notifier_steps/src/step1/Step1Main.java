package step1;

/*
 * 단계 1: 상속 (토비 1.2 — 템플릿 메서드 / 팩토리 메서드)
 *
 * 구조: 서비스가 추상 클래스, 알림 부분(send)을 추상 메서드로 비워 두고
 *       발송 방식마다 자식 클래스가 채운다.
 *
 * [카카오 추가 기록]
 * | 고친 파일                  | 없음 (부모 OrderService, PaymentService 그대로)          |
 * | 서비스 코드에서 고친 줄 수 | 0                                                          |
 * | 새로 만든 클래스 수        | 2 (KakaoOrderService, KakaoPaymentService)                |
 * | 반복된 발송 코드           | 카카오 발송 코드가 두 자식에 똑같이 들어감 (문자도 마찬가지) |
 * | 이 단계의 단점             | 서비스 종류 × 발송 방식만큼 자식 클래스가 생기고, 발송 코드를 나눠 쓸 수 없다 |
 */
public class Step1Main {

    static final String STEP = "[단계 1]";
    static final String RECIPIENT = "고객";
    static final String ORDER_DONE = "주문이 완료되었습니다";
    static final String PAYMENT_DONE = "결제가 완료되었습니다";
    static final String SMS = "[문자]";
    static final String KAKAO = "[카카오]";

    public static void main(String[] args) {
        // 고객사 A: 문자. 어느 자식 클래스를 만드느냐로 발송 방식이 정해진다.
        new SmsOrderService().complete(RECIPIENT);
        new SmsPaymentService().pay(RECIPIENT);

        // 고객사 B: 카카오. 부모는 안 고치고 자식 클래스 두 개를 새로 만들었다.
        new KakaoOrderService().complete(RECIPIENT);
        new KakaoPaymentService().pay(RECIPIENT);
    }

    static void print(String channel, String to, String message) {
        System.out.println(STEP + " " + channel + " " + to + " / " + message);
    }
}

// ---- 우리가 파는 코드 (고객사는 못 고침) ----

abstract class OrderService {
    void complete(String customer) {
        // 주문 처리 (생략)
        send(customer, Step1Main.ORDER_DONE);
    }

    protected abstract void send(String to, String message);
}

abstract class PaymentService {
    void pay(String customer) {
        // 결제 처리 (생략)
        send(customer, Step1Main.PAYMENT_DONE);
    }

    protected abstract void send(String to, String message);
}

// ---- 고객사 A가 작성 (문자) ----

class SmsOrderService extends OrderService {
    @Override
    protected void send(String to, String message) {
        Step1Main.print(Step1Main.SMS, to, message);          // 문자 발송 코드
    }
}

class SmsPaymentService extends PaymentService {
    @Override
    protected void send(String to, String message) {
        Step1Main.print(Step1Main.SMS, to, message);          // 위와 똑같은 문자 발송 코드
    }
}

// ---- 고객사 B가 작성 (카카오) ----

class KakaoOrderService extends OrderService {
    @Override
    protected void send(String to, String message) {
        Step1Main.print(Step1Main.KAKAO, to, message);        // 카카오 발송 코드
    }
}

class KakaoPaymentService extends PaymentService {
    @Override
    protected void send(String to, String message) {
        Step1Main.print(Step1Main.KAKAO, to, message);        // 위와 똑같은 카카오 발송 코드
    }
}
