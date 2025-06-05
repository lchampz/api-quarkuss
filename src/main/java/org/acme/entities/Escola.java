package org.acme.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
    @Column(nullable = false)
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

    @OneToMany(mappedBy = "escola", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Aluno> alunos = new ArrayList<>();

    @Nullable
    @OneToMany(mappedBy = "escola")
    @JsonIgnore
    private List<Matricula> matricula;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public Escola(String nome, int capacidade, @Nullable List<Matricula> matricula) {
        this.nome = nome;
        this.capacidade = capacidade;
        this.matricula = matricula == null ? Collections.emptyList() : matricula;
        this.dataCriacao = LocalDateTime.now();
    }

    public Escola(String nome, List<Matricula> matricula) {
        this.nome = nome;
        this.matricula = matricula.isEmpty() ? Collections.emptyList() : matricula;
        this.dataCriacao = LocalDateTime.now();
    }

    public Escola(String nome) {
        this.nome = nome;
        matricula = Collections.emptyList();
        this.dataCriacao = LocalDateTime.now();
    }

    public Object getId() {
        return this.id;
    }

    public boolean temVagasDisponiveis() {
        return alunos.size() < capacidade;
    }

    public int getVagasDisponiveis() {
        return capacidade - alunos.size();
    }

    public double getPercentualOcupacao() {
        return (double) alunos.size() / capacidade * 100;
    }

    public boolean getAtivo() {
        return this.ativo;
    }
}