package org.acme.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "escolas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Escola extends PanacheEntity {

    @NotBlank(message = "O nome da escola é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    @Column(nullable = false, unique = true) // Nome da escola geralmente é único
    private String nome;

    @NotNull(message = "A capacidade é obrigatória")
    @Min(value = 1, message = "A capacidade deve ser maior que zero")
    @Column(nullable = false)
    private Integer capacidade;

    @Column(name = "endereco")
    private String endereco;

    @Column(name = "telefone")
    private String telefone;

    @Column(name = "email")
    private String email;

    @Column(name = "diretor")
    private String diretor;

    @Column(name = "data_fundacao")
    private LocalDateTime dataFundacao;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Removido: @OneToMany(mappedBy = "escola", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    // private List<Aluno> alunos = new ArrayList<>();

    @Nullable
    @OneToMany(mappedBy = "escola", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore // Evitar serialização em loop e expor todas as matrículas por padrão
    private List<Matricula> matricula; // Relação com Matricula

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        ativo = true; // Garante que a escola é criada como ativa
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    public Escola(String nome, int capacidade) {
        this.nome = nome;
        this.capacidade = capacidade;
        this.matricula = Collections.emptyList();
    }


    public Escola(String nome, int capacidade, @Nullable List<Matricula> matriculas) {
        this.nome = nome;
        this.capacidade = capacidade;
        this.matricula = matriculas == null ? Collections.emptyList() : matriculas;
    }



    // public Object getId() {
    //    return this.id;
    // }

    private long getAlunosAtivosCount() {
        if (this.matricula == null) {
            return 0;
        }
        return this.matricula.stream()
                .filter(m -> m.getStatus() == Matricula.StatusMatricula.ATIVA)
                .count();
    }

    public boolean temVagasDisponiveis() {
        if (this.capacidade == null) return false; // Capacidade não definida
        return getAlunosAtivosCount() < this.capacidade;
    }

    public int getVagasDisponiveis() {
        if (this.capacidade == null) return 0; // Capacidade não definida
        return this.capacidade - (int) getAlunosAtivosCount();
    }

    public double getPercentualOcupacao() {
        if (this.capacidade == null || this.capacidade == 0) {
            return 0.0; // Evita divisão por zero e lida com capacidade não definida
        }
        return (double) getAlunosAtivosCount() / this.capacidade * 100;
    }

    public boolean getAtivo() {
        return this.ativo;
    }
}
