package sample.cafekiosk.spring.domain.order;

import org.aspectj.weaver.ast.Or;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sample.cafekiosk.spring.domain.product.Product;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    @DisplayName("주문 생성 시 상품 리스트에서 주문의 총 금액을 계산한다")
    @Test
    void calculateTotalPrice(){
        //given
        LocalDateTime now = LocalDateTime.now();

        Product p1 = getProduct("아메리카노", 4000);
        Product p2 = getProduct("카라멜 마끼아또", 6000);

        //when
        Order order = Order.create(List.of(p1, p2), now);

        //then
        Assertions.assertThat(order.getTotalPrice()).isEqualTo(10000);
    }

    @DisplayName("주문 생성 시 주문 상태는 INIT이다.")
    @Test
    void init(){
        //given
        LocalDateTime now = LocalDateTime.now();

        Product p1 = getProduct("아메리카노", 4000);
        Product p2 = getProduct("카라멜 마끼아또", 6000);
        //when
        Order order = Order.create(List.of(p1, p2), now);

        //then
        Assertions.assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.INIT);

    }

    @DisplayName("주문 생성 시 주문 등록 시간을 기록한다.")
    @Test
    void registerDateTime(){
        //given
        LocalDateTime now = LocalDateTime.now();

        Product p1 = getProduct("아메리카노", 4000);
        Product p2 = getProduct("카라멜 마끼아또", 6000);

        //when
        Order order = Order.create(List.of(p1, p2), now);

        //then
        Assertions.assertThat(order.getRegisteredDateTime()).isEqualTo(now);
    }
    private static Product getProduct(String productNumber, int price) {
        return Product.builder().productNumber(productNumber).price(price).build();
    }
}