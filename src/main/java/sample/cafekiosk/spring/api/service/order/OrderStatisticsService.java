package sample.cafekiosk.spring.api.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sample.cafekiosk.spring.api.service.mail.MailService;
import sample.cafekiosk.spring.domain.order.Order;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.order.OrderStatus;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderStatisticsService {

    private final OrderRepository orderRepository;
    private final MailService mailService;

    //메일전송의 로직의 경우 Transaction을 안거는 것이 좋음
    //긴 네트워크를 타거나 작업 소요 시간이 긴데 트랜잭션이 필요없는 경우 안거는 것이 좋음
    public boolean sendOrderStatisticsMail(LocalDate orderDate, String email){
        //해당 일자에 결제완료된 주문들을 가져와서 총 매출합계를 계산하고 메일전송
        List<Order> orders = orderRepository.findOrdersBy(
                //주문 일시로 조회
                orderDate.atStartOfDay(),
                orderDate.plusDays(1).atStartOfDay(),
                OrderStatus.PAYMENT_COMPLETED
                );

        //총 매출합계를 계산
        int totalAmount = orders.stream()
                .mapToInt(Order::getTotalPrice)
                .sum();

        //메일 전송
        //직접 구현은 아니고 비슷하게
        boolean result = mailService.sendMail(
                "no-reply@ciosk.com",
                email,
                String.format("[매출통계] %s", orderDate),
                String.format("총 매출 합계는 %s 원입니다.", totalAmount));

        if(!result){
            throw new IllegalArgumentException("매출 통계 메일 전송에 실패했습니다.");
        }
        return true;
    }
}
