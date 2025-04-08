package org.acme.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.entities.Escola;
import org.acme.entities.Matricula;
import org.acme.exceptions.EscolaException;

@ApplicationScoped
public class MatriculaRepository implements PanacheRepository<Matricula> {

    @Inject
    EscolaRepository escolaRepository;

    public long countByEscola(Escola escola) {
        return count("escola", escola);
    }

    public void addMatricula(Matricula matricula) throws EscolaException {
        Escola escola = escolaRepository.findById(matricula.escola.id);
        if (escola == null) {
            throw new EscolaException("Escola não encontrada");
        }

        if (escola.capacidade < countByEscola(escola) + 1) {
            throw new EscolaException("A escola " + escola.nome + " já está lotada.");
        }

        persistAndFlush(matricula);
    }

}