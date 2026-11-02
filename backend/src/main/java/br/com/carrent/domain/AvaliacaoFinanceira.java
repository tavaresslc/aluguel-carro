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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "avaliacao_financeira")
@Getter
@Setter
@NoArgsConstructor
public class AvaliacaoFinanceira {

    public enum Parecer {
        APROVADO, REPROVADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoAluguel pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agente_id", nullable = false)
    private Agente agente;

    @Column(name = "data_avaliacao", nullable = false)
    private LocalDateTime dataAvaliacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Parecer parecer;

    @Column(length = 1000)
    private String justificativa;

    public void aprovar(String justificativa) {
        registrar(Parecer.APROVADO, justificativa);
    }

    public void reprovar(String justificativa) {
        registrar(Parecer.REPROVADO, justificativa);
    }

    private void registrar(Parecer parecer, String justificativa) {
        this.parecer = parecer;
        this.justificativa = justificativa;
        this.dataAvaliacao = LocalDateTime.now(PedidoAluguel.FUSO_HORARIO);
    }
}
