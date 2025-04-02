package org.acme.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entities.Aluno;
import org.acme.entities.Escola;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class EscolaRepository implements PanacheRepository<Escola> {

    public Escola findById(UUID id) {
        return find("id", id).firstResult();
    }

    public List<Escola> findByName(String nome) {
        return find("escola", nome).list();
    }

    public Escola addEscola(Escola escola) {
        persist(escola);
        return escola;
    }

    public Escola removeFromId(UUID id) {
        Escola escola = this.findById(id);
        escola.delete();
        return escola;
    }

    public Escola removeEscola(Escola escola) {
        escola.delete();
        return escola;
    }
}