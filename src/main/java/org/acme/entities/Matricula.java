package org.acme.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "matriculas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Matricula extends PanacheEntity {

    @NotNull(message = "O aluno é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @NotNull(message = "A escola é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escola_id", nullable = false)
    private Escola escola;

    @Column(name = "data_matricula", nullable = false)
    private LocalDateTime dataMatricula;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Enumerated(EnumType.STRING)
    @Column(name = "ativo", nullable = false)
    private StatusMatricula status = StatusMatricula.ATIVA;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        if (dataMatricula == null) {
            dataMatricula = LocalDateTime.now();
        }
        if (dataInicio == null) {
            dataInicio = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }


    public Matricula(Aluno aluno, Escola escola) {
        this.aluno = aluno;
        this.escola = escola;
        this.dataMatricula = LocalDateTime.now();
        this.dataInicio = LocalDateTime.now();
        this.dataCriacao = LocalDateTime.now();
    }

    public Long getId() {
        return this.id;
    }


    public enum StatusMatricula {
        ATIVA,
        CANCELADA,
        SUSPENSA,
        CONCLUIDA
    }
}