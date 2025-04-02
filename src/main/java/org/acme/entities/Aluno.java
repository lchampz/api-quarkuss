package org.acme.entities;

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
    public UUID matricula;
    @Column
    public String nome;
    @ManyToOne
    public Escola escola;

    public Aluno(String nome, Escola escola) {
        matricula = UUID.randomUUID();
        this.nome = nome;
        this.escola = escola;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Escola getEscola() {
        return escola;
    }

    public void setEscola(Escola escola) {
        this.escola = escola;
        matricula = UUID.randomUUID();
    }
}