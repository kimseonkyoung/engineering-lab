package step4;

/*
 * 단계 4: 생성자로 받기 (토비 1.3.3 — 관계 설정 책임의 분리)
 *
 * 구조: 서비스는 Sender 인터페이스만 알고, 구현은 생성자로 받는다.
 *       어떤 구현을 끼울지는 제3자(main)가 정한다.
 *
 * [카카오 추가 기록]
 * | 고친 파일                  | 없음 (서비스 코드는 그대로). main의 조립 줄만 다름 |
 * | 서비스 코드에서 고친 줄 수 | 0                                                  |
 * | 새로 만든 클래스 수        | 1 (KakaoSender, Sender 구현)                        |
 * | 반복된 발송 코드           | 없음                                                |
 * | 이 단계의 단점             | 조립(누구에게 무엇을 끼울지)을 main이 직접 해야 한다.
 * |                            | 객체가 많아지면 main이 커진다 → 토비 1.4 DaoFactory, 이후 스프링 컨테이너 |
 *
 * 넣는 방법으로 생성자를 고른 이유: 발송 객체를 깜빡하면 서비스를 아예 못 만들어서,
 * 실수가 실행 중 NullPointerException이 아니라 컴파일 때 드러난다. 필드도 final로 둘 수 있다.
 */
public class Step4Main {

    static final String STEP = "[단계 4]";
    static final String RECIPIENT = "고객";
    static final String ORDER_DONE = "주문이 완료되었습니다";
    static final String PAYMENT_DONE = "결제가 완료되었습니다";
    static final String SMS = "[문자]";
    static final String KAKAO = "[카카오]";

    public static void main(String[] args) {
        // 고객사 A의 조립: 문자
        Sender smsSender = new SmsSender();
        new OrderService(smsSender).complete(RECIPIENT);
        new PaymentService(smsSender).pay(RECIPIENT);

        // 고객사 B의 조립: 카카오. 서비스는 같은 클래스, 끼우는 것만 다르다.
        Sender kakaoSender = new KakaoSender();
        new OrderService(kakaoSender).complete(RECIPIENT);
        new PaymentService(kakaoSender).pay(RECIPIENT);
    }

    static void print(String channel, String to, String message) {
        System.out.println(STEP + " " + channel + " " + to + " / " + message);
    }
}

// ---- 약속 ----

interface Sender {
    void send(String to, String message);
}

class SmsSender implements Sender {
    @Override
    public void send(String to, String message) {
        Step4Main.print(Step4Main.SMS, to, message);
    }
}

class KakaoSender implements Sender {                          // 카카오 추가: 새 클래스 1개
    @Override
    public void send(String to, String message) {
        Step4Main.print(Step4Main.KAKAO, to, message);
    }
}

// ---- 서비스: 구현 클래스 이름이 한 번도 나오지 않는다 ----

class OrderService {
    private final Sender sender;

    OrderService(Sender sender) {
        this.sender = sender;
    }

    void complete(String customer) {
        // 주문 처리 (생략)
        sender.send(customer, Step4Main.ORDER_DONE);
    }
}

class PaymentService {
    private final Sender sender;

    PaymentService(Sender sender) {
        this.sender = sender;
    }

    void pay(String customer) {
        // 결제 처리 (생략)
        sender.send(customer, Step4Main.PAYMENT_DONE);
    }
}
