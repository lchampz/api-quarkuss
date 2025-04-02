package org.acme.repositories;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entities.Aluno;
import org.acme.entities.Escola;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AlunoRepository implements PanacheRepository<Aluno> {

    public Aluno findById(UUID id) {
        return find("id", id).firstResult();
    }


    public List<Aluno> findByEscola(Escola escola) {
        return find("escola", escola).list();
    }

    public Aluno addAluno(Aluno aluno) {
        persist(aluno);
        return aluno;
    }

    public Aluno removeFromId(UUID id) {
        Aluno aluno = this.findById(id);
        aluno.delete();
        return aluno;
    }

    public Aluno removeAluno(Aluno aluno) {
        aluno.delete();
        return aluno;
    }
}