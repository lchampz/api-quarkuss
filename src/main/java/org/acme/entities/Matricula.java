package org.acme.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class Matricula extends PanacheEntity {
    @Column
    public String curso;
    @JsonManagedReference
    @ManyToOne
    public Escola escola;
    @ManyToOne
    @JsonManagedReference
    public Aluno aluno;

    public Matricula() {};

    public Matricula(String curso, Escola escola, Aluno aluno) {
        this.curso = curso;
        this.escola = escola;
        this.aluno = aluno;
    }
}