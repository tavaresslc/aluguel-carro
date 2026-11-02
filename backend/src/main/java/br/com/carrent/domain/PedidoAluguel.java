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
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pedido_aluguel")
@Getter
@Setter
@NoArgsConstructor
public class PedidoAluguel {

    public static final ZoneId FUSO_HORARIO = ZoneId.of("America/Sao_Paulo");

    public enum StatusPedido {
        PENDENTE, APROVADO, REPROVADO, CANCELADO, CONTRATADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "automovel_id", nullable = false)
    private Automovel automovel;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDate dataSolicitacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status = StatusPedido.PENDENTE;

    @Column(length = 1000)
    private String observacoes;

    public boolean podeSerModificado() {
        return status != StatusPedido.CANCELADO && status != StatusPedido.CONTRATADO;
    }

    public void modificar(Automovel novoAutomovel, String novasObservacoes) {
        if (!podeSerModificado()) {
            throw new IllegalStateException("Pedido " + status + " não pode ser modificado.");
        }
        if (novoAutomovel != null && !novoAutomovel.getId().equals(automovel.getId())) {
            this.automovel = novoAutomovel;
            if (status == StatusPedido.APROVADO || status == StatusPedido.REPROVADO) {
                this.status = StatusPedido.PENDENTE;
            }
        }
        this.observacoes = novasObservacoes;
    }

    public void cancelar() {
        if (!podeSerModificado()) {
            throw new IllegalStateException("Pedido " + status + " não pode ser cancelado.");
        }
        this.status = StatusPedido.CANCELADO;
    }

    public void atualizarStatus(StatusPedido novoStatus) {
        this.status = novoStatus;
    }
}
