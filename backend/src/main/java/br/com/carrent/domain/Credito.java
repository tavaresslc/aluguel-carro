package br.com.carrent.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "credito")
@Getter
@Setter
@NoArgsConstructor
public class Credito {

    public enum StatusCredito {
        APROVADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false, unique = true)
    private ContratoAluguel contrato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "banco_id", nullable = false)
    private Agente banco;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "taxa_juros", nullable = false, precision = 7, scale = 4)
    private BigDecimal taxaJuros;

    @Column(name = "quantidade_parcelas", nullable = false)
    private Integer quantidadeParcelas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCredito status;

    public void aprovar() {
        this.status = StatusCredito.APROVADO;
    }

    public BigDecimal calcularParcelas() {
        BigDecimal n = BigDecimal.valueOf(quantidadeParcelas);
        if (taxaJuros.signum() == 0) {
            return valor.divide(n, 2, RoundingMode.HALF_UP);
        }
        BigDecimal i = taxaJuros.divide(BigDecimal.valueOf(100), MathContext.DECIMAL64);
        BigDecimal fator = BigDecimal.ONE.add(i).pow(quantidadeParcelas, MathContext.DECIMAL64);
        return valor.multiply(i).multiply(fator)
                .divide(fator.subtract(BigDecimal.ONE), MathContext.DECIMAL64)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
