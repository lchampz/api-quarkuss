package org.acme.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
public class Aluno extends PanacheEntity {
    public Aluno() {
    }
    @Column
    public String nome;
    @Nullable
    @JsonIgnore
    @OneToMany(mappedBy = "aluno")
    private List<Matricula> matricula;

    public Aluno(String nome, @Nullable List<Matricula> matricula) {
        this.nome = nome;
        this.matricula = matricula;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Nullable
    public List<Matricula> getMatricula() {
        return matricula;
    }

    public void setMatricula(@Nullable List<Matricula> matricula) {
        this.matricula = matricula;
    }

}