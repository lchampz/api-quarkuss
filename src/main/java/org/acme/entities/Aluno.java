package org.acme.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "alunos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aluno extends PanacheEntity {

    @NotBlank(message = "O nome do aluno é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    @Column(nullable = false)
    private String nome;

    @NotNull(message = "A idade é obrigatória")
    @Min(value = 3, message = "A idade mínima é 3 anos")
    @Max(value = 18, message = "A idade máxima é 18 anos")
    @Column(nullable = false)
    private Integer idade;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "nome_responsavel")
    private String nomeResponsavel;

    @Column(name = "telefone_responsavel")
    private String telefoneResponsavel;

    @Email(message = "Email inválido")
    @Column(name = "email_responsavel")
    private String emailResponsavel;

    @Column(name = "endereco")
    private String endereco;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;


    @Nullable
    @JsonIgnore
    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Matricula> matricula; // Relação com Matricula

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        ativo = true; // Garante que o aluno é criado como ativo
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    // Construtor simplificado
    public Aluno(String nome, Integer idade) {
        this.nome = nome;
        this.idade = idade;
    }


    public boolean getAtivo() {
        return this.ativo;
    }


    public Serializable getId() {
        return this.id;
     }
}
