package step3;

/*
 * 단계 3: 인터페이스 (토비 1.3.2 — ConnectionMaker 인터페이스)
 *
 * 구조: "보낸다"는 약속을 Sender 인터페이스로 정하고, 서비스 필드 타입을 인터페이스로 바꿨다.
 *       하지만 어떤 구현을 쓸지(new)는 아직 서비스 안에서 정한다.
 *
 * [카카오 추가 기록]
 * | 고친 파일                  | OrderService, PaymentService                       |
 * | 서비스 코드에서 고친 줄 수 | 서비스마다 1줄 (new) = 2줄                          |
 * | 새로 만든 클래스 수        | 1 (KakaoSender, Sender 구현)                        |
 * | 반복된 발송 코드           | 없음                                                |
 * | 이 단계의 단점             | 구현을 고르는 new가 서비스 안에 남아 있어, 바꾸려면 여전히 서비스를 고쳐야 한다 |
 *
 * 단계 2와 같은 이유로 고객사 B용 "고친 서비스"를 따로 복사해 두었다.
 */
public class Step3Main {

    static final String STEP = "[단계 3]";
    static final String RECIPIENT = "고객";
    static final String ORDER_DONE = "주문이 완료되었습니다";
    static final String PAYMENT_DONE = "결제가 완료되었습니다";
    static final String SMS = "[문자]";
    static final String KAKAO = "[카카오]";

    public static void main(String[] args) {
        // 고객사 A: 문자
        new OrderService().complete(RECIPIENT);
        new PaymentService().pay(RECIPIENT);

        // 고객사 B: 카카오 (서비스마다 new 한 줄을 고쳤다)
        new KakaoEditedOrderService().complete(RECIPIENT);
        new KakaoEditedPaymentService().pay(RECIPIENT);
    }

    static void print(String channel, String to, String message) {
        System.out.println(STEP + " " + channel + " " + to + " / " + message);
    }
}

// ---- 약속: 보내는 것은 반드시 send(받는 사람, 내용)으로 ----

interface Sender {
    void send(String to, String message);
}

class SmsSender implements Sender {
    @Override
    public void send(String to, String message) {
        Step3Main.print(Step3Main.SMS, to, message);
    }
}

class KakaoSender implements Sender {                          // 카카오 추가: 새 클래스 1개
    @Override
    public void send(String to, String message) {              // 이름이 send로 강제된다 (sendKakao로 지으면 컴파일 에러)
        Step3Main.print(Step3Main.KAKAO, to, message);
    }
}

// ---- 원래 서비스 (문자) ----

class OrderService {
    private final Sender sender = new SmsSender();             // 타입은 인터페이스, 고르는 건 아직 여기서

    void complete(String customer) {
        // 주문 처리 (생략)
        sender.send(customer, Step3Main.ORDER_DONE);
    }
}

class PaymentService {
    private final Sender sender = new SmsSender();

    void pay(String customer) {
        // 결제 처리 (생략)
        sender.send(customer, Step3Main.PAYMENT_DONE);
    }
}

// ---- 카카오로 바꾸려고 고친 서비스 (원래는 위 파일을 고치는 것) ----

class KakaoEditedOrderService {
    private final Sender sender = new KakaoSender();           // [수정] new 한 줄만

    void complete(String customer) {
        // 주문 처리 (생략)
        sender.send(customer, Step3Main.ORDER_DONE);           // 그대로
    }
}

class KakaoEditedPaymentService {
    private final Sender sender = new KakaoSender();           // [수정] new 한 줄만

    void pay(String customer) {
        // 결제 처리 (생략)
        sender.send(customer, Step3Main.PAYMENT_DONE);         // 그대로
    }
}
