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
@Table(name = "matriculas", uniqueConstraints = {
        // Garante que um aluno não pode ter múltiplas matrículas ATIVAS na mesma escola simultaneamente.
        // Pode ser necessário ajustar a lógica se um aluno puder se rematricular após um período.
        // Esta constraint pode ser muito restritiva dependendo das regras de negócio.
        // @UniqueConstraint(columnNames = {"aluno_id", "escola_id", "status"})
        // Uma constraint mais comum seria apenas para aluno_id e escola_id se apenas uma matrícula (independente do status) é permitida
        // @UniqueConstraint(columnNames = {"aluno_id", "escola_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Matricula extends PanacheEntity {

    @NotNull(message = "O aluno é obrigatório")
    @ManyToOne(fetch = FetchType.EAGER) // LAZY é geralmente melhor para performance
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @NotNull(message = "A escola é obrigatória")
    @ManyToOne(fetch = FetchType.EAGER) // LAZY é geralmente melhor para performance
    @JoinColumn(name = "escola_id", nullable = false)
    private Escola escola;

    @NotNull(message = "A data da matrícula é obrigatória")
    @Column(name = "data_matricula", nullable = false)
    private LocalDateTime dataMatricula;

    @NotNull(message = "A data de início é obrigatória")
    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim; // Preenchida quando a matrícula é CANCELADA ou CONCLUIDA

    @NotNull(message = "O status da matrícula é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false) // Nome da coluna alterado para 'status' para clareza
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
            dataInicio = this.dataMatricula;
        }
        if (status == null) {
            status = StatusMatricula.ATIVA;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    public Matricula(Aluno aluno, Escola escola) {
        this.aluno = aluno;
        this.escola = escola;

    }


    public enum StatusMatricula {
        ATIVA,      // Aluno está atualmente matriculado e frequentando
        PENDENTE,   // Matrícula solicitada, aguardando confirmação/documentação
        CANCELADA,  // Matrícula foi interrompida (pelo aluno ou escola)
        SUSPENSA,   // Matrícula temporariamente interrompida
        CONCLUIDA   // Aluno completou o ciclo/curso associado a esta matrícula
    }
}
