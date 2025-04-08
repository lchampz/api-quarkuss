package org.acme.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Column
    public int capacidade;
    @Nullable
    @OneToMany(mappedBy = "escola")
    @JsonIgnore
    private List<Matricula> matricula;

    public Escola() {
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
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Nullable
    public List<Matricula> getMatricula() {
        return matricula;
    }

    public void setMatriculas(@Nullable List<Matricula> matricula) {
        this.matricula = matricula;
    }

    public Escola(String nome, List<Matricula> matricula) {
        this.nome = nome;
        this.matricula = matricula.isEmpty() ? Collections.emptyList() : matricula;
    }

    public Escola(String nome) {
        this.nome = nome;
        matricula = Collections.emptyList();
    }


}