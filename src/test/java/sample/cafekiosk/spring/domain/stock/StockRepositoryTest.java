package sample.cafekiosk.spring.domain.stock;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class StockRepositoryTest {
    @Autowired
    private StockRepository stockRepository;

    @DisplayName("상품번호 리스트로 재고를 조회한다.")
    @Test
    void test(){
        //given
        Stock stock1 = Stock.create("001", 1);
        Stock stock2 = Stock.create("002", 3);
        Stock stock3 = Stock.create("003", 2);
        stockRepository.saveAll(List.of(stock3, stock1, stock2));

        //when
        List<Stock> stocks = stockRepository.findAllByProductNumberIn(List.of("001", "003"));

        //then
        Assertions.assertThat(stocks).hasSize(2)
                .extracting("productNumber", "quantity")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("001", 1),
                        Tuple.tuple("003", 2)
                );
    }
}