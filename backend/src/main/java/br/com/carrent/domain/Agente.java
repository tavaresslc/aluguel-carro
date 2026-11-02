package br.com.carrent.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "agente")
@Getter
@Setter
@NoArgsConstructor
public class Agente extends Usuario {

    public enum TipoAgente {
        EMPRESA, BANCO
    }

    @Column(name = "nome_instituicao", nullable = false, length = 150)
    private String nomeInstituicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAgente tipo;

    @Override
    public Perfil perfil() {
        return tipo == TipoAgente.BANCO ? Perfil.BANCO : Perfil.EMPRESA;
    }

    public boolean isBanco() {
        return tipo == TipoAgente.BANCO;
    }
}
