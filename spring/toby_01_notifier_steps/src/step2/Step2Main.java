package step2;

/*
 * 단계 2: 클래스 분리 (토비 1.3.1 — SimpleConnectionMaker)
 *
 * 구조: 발송 코드를 SmsSender라는 별도 클래스로 빼고,
 *       서비스가 그 객체를 직접 만들어 가지고 쓴다 (포함, has-a).
 *
 * [카카오 추가 기록]
 * | 고친 파일                  | OrderService, PaymentService                                   |
 * | 서비스 코드에서 고친 줄 수 | 서비스마다 2줄 (필드 타입·new 한 줄 + 부르는 줄) = 4줄          |
 * |                            | 부르는 곳이 늘면 그만큼 더 늘어남                               |
 * | 새로 만든 클래스 수        | 1 (KakaoSender)                                                 |
 * | 반복된 발송 코드           | 없음 (발송 코드는 SmsSender, KakaoSender 한 곳씩)               |
 * | 이 단계의 단점             | 서비스가 클래스 이름과 메서드 이름을 직접 알아서, 바꾸려면 서비스를 고쳐야 한다 |
 *
 * 한 프로그램에서 두 고객사를 다 보여 주려고, 고객사 B용으로 "고친 서비스"를
 * 따로 복사해 두었다 (KakaoEditedOrderService 등). 실제로는 원래 파일을 고치는 것이다.
 */
public class Step2Main {

    static final String STEP = "[단계 2]";
    static final String RECIPIENT = "고객";
    static final String ORDER_DONE = "주문이 완료되었습니다";
    static final String PAYMENT_DONE = "결제가 완료되었습니다";
    static final String SMS = "[문자]";
    static final String KAKAO = "[카카오]";

    public static void main(String[] args) {
        // 고객사 A: 문자 (원래 코드 그대로)
        new OrderService().complete(RECIPIENT);
        new PaymentService().pay(RECIPIENT);

        // 고객사 B: 카카오 (서비스 코드를 고쳐야 했다)
        new KakaoEditedOrderService().complete(RECIPIENT);
        new KakaoEditedPaymentService().pay(RECIPIENT);
    }

    static void print(String channel, String to, String message) {
        System.out.println(STEP + " " + channel + " " + to + " / " + message);
    }
}

// ---- 발송 담당 클래스 ----

class SmsSender {
    void sendSms(String to, String message) {
        Step2Main.print(Step2Main.SMS, to, message);
    }
}

class KakaoSender {                                            // 카카오 추가: 새 클래스 1개
    void sendKakao(String to, String message) {               // 메서드 이름을 정한 규칙이 없어서 이름이 다르다
        Step2Main.print(Step2Main.KAKAO, to, message);
    }
}

// ---- 원래 서비스 (문자) ----

class OrderService {
    private final SmsSender sender = new SmsSender();

    void complete(String customer) {
        // 주문 처리 (생략)
        sender.sendSms(customer, Step2Main.ORDER_DONE);
    }
}

class PaymentService {
    private final SmsSender sender = new SmsSender();

    void pay(String customer) {
        // 결제 처리 (생략)
        sender.sendSms(customer, Step2Main.PAYMENT_DONE);
    }
}

// ---- 카카오로 바꾸려고 고친 서비스 (원래는 위 파일을 고치는 것) ----

class KakaoEditedOrderService {
    private final KakaoSender sender = new KakaoSender();      // [수정 1] 타입과 new

    void complete(String customer) {
        // 주문 처리 (생략)
        sender.sendKakao(customer, Step2Main.ORDER_DONE);      // [수정 2] 메서드 이름
    }
}

class KakaoEditedPaymentService {
    private final KakaoSender sender = new KakaoSender();      // [수정 1]

    void pay(String customer) {
        // 결제 처리 (생략)
        sender.sendKakao(customer, Step2Main.PAYMENT_DONE);    // [수정 2]
    }
}
