package com.backend.ticker.models.entity.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "price", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"euc_id", "fiat_symbol", "created_on"})
})
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prices_seq_generator")
    @SequenceGenerator(name = "prices_seq_generator", sequenceName = "Prices_SEQ", allocationSize = 50)
    private Long id;
    @Column(name = "euc_id", nullable = false)
    private String eucId;

    @Column(name = "fiat_symbol", nullable = false)
    private String fiatSymbol;

    @Column(name = "created_on", columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    private ZonedDateTime createdOn;

    @Column(name = "formatted_price", nullable = false)
    private String formattedPrice;

}
