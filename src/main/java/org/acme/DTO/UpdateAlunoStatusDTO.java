package org.acme.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAlunoStatusDTO {
    
    @NotNull(message = "O status é obrigatório")
    private Boolean ativo;
} 