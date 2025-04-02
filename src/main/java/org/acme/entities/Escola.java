package org.acme.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
public class Escola extends PanacheEntity {
    @Column
    public String nome;
    @Nullable
    @OneToMany(mappedBy = "escola")
    public List<Aluno> alunos;

    public Escola() {
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Nullable
    public List<Aluno> getAlunos() {
        return alunos;
    }

    public void setAlunos(@Nullable List<Aluno> alunos) {
        this.alunos = alunos;
    }

    public Escola(String nome, List<Aluno> alunos) {
        this.nome = nome;
        this.alunos = alunos.isEmpty() ? Collections.emptyList() : alunos;
    }

    public Escola(String nome) {
        this.nome = nome;
        alunos = Collections.emptyList();
    }


}