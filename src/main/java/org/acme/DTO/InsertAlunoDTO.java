package org.acme.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsertAlunoDTO {
    
    @NotBlank(message = "O nome do aluno é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @NotNull(message = "A idade é obrigatória")
    @Min(value = 3, message = "A idade deve ser maior ou igual a 3 anos")
    @Max(value = 18, message = "A idade deve ser menor ou igual a 18 anos")
    private Integer idade;

    @NotNull(message = "A data de nascimento é obrigatória")
    private LocalDate dataNascimento;

    @Size(max = 100, message = "O nome do responsável não pode ter mais que 100 caracteres")
    private String nomeResponsavel;

    @Pattern(regexp = "^\\+?[1-9][0-9]{10,14}$", message = "Telefone inválido")
    private String telefoneResponsavel;
    
    @Email(message = "Email inválido")
    private String emailResponsavel;
    
    @Size(max = 200, message = "O endereço não pode ter mais que 200 caracteres")
    private String endereco;

    @Size(max = 500, message = "As observações não podem ter mais que 500 caracteres")
    private String observacoes;

    private Boolean ativo;

    @NotNull(message = "O ID da escola é obrigatório")
    private Long escolaId;
}
