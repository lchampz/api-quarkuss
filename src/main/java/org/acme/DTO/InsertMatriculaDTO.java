package org.acme.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsertMatriculaDTO {
    
    @NotNull(message = "O ID do aluno é obrigatório")
    private Long alunoId;
    
    @NotNull(message = "O ID da escola é obrigatório")
    private Long escolaId;
    
    @NotNull(message = "A data de início é obrigatória")
    private LocalDate dataInicio;
    
    private LocalDate dataFim;
    
    private String observacoes;
    
    private Boolean ativo;

    // Getters e Setters
    public Long getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }

    public Long getEscolaId() {
        return escolaId;
    }

    public void setEscolaId(Long escolaId) {
        this.escolaId = escolaId;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
