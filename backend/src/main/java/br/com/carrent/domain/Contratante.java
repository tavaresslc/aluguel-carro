package br.com.carrent.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contratante")
@Getter
@Setter
@NoArgsConstructor
public class Contratante {

    public static final int MAXIMO_VINCULOS = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 20)
    private String rg;

    @Column(unique = true, length = 11)
    private String cpf;

    @Column(length = 255)
    private String endereco;

    @Column(length = 100)
    private String profissao;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "vinculo_empregaticio", joinColumns = @JoinColumn(name = "contratante_id"))
    private List<VinculoEmpregaticio> vinculos = new ArrayList<>();

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VinculoEmpregaticio {

        @Column(name = "entidade_empregadora", nullable = false, length = 150)
        private String entidadeEmpregadora;

        @Column(nullable = false, precision = 15, scale = 2)
        private BigDecimal rendimento;
    }

    public void atualizarDados(String nome, String rg, String cpf, String endereco, String profissao) {
        this.nome = nome;
        this.rg = rg;
        this.cpf = cpf;
        this.endereco = endereco;
        this.profissao = profissao;
    }

    public void definirVinculos(List<VinculoEmpregaticio> novos) {
        if (novos.size() > MAXIMO_VINCULOS) {
            throw new IllegalArgumentException(
                    "Um contratante pode possuir no máximo " + MAXIMO_VINCULOS + " vínculos.");
        }
        this.vinculos.clear();
        this.vinculos.addAll(novos);
    }

    public BigDecimal calcularRendaTotal() {
        return vinculos.stream()
                .map(VinculoEmpregaticio::getRendimento)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean possuiDadosCompletos() {
        return preenchido(nome) && preenchido(rg) && preenchido(cpf) && preenchido(endereco)
                && preenchido(profissao) && !vinculos.isEmpty();
    }

    private static boolean preenchido(String valor) {
        return valor != null && !valor.isBlank();
    }
}
