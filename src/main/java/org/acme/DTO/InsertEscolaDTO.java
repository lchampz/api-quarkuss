package org.acme.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsertEscolaDTO {
    
    @NotBlank(message = "O nome da escola é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @NotNull(message = "A capacidade é obrigatória")
    @Min(value = 1, message = "A capacidade deve ser maior que zero")
    @Max(value = 1000, message = "A capacidade não pode ser maior que 1000")
    private Integer capacidade;

    @Size(max = 200, message = "O endereço não pode ter mais que 200 caracteres")
    private String endereco;

    @Pattern(regexp = "^\\+?[1-9][0-9]{10,14}$", message = "Telefone inválido")
    private String telefone;

    @Email(message = "Email inválido")
    private String email;

    @Size(max = 100, message = "O nome do diretor não pode ter mais que 100 caracteres")
    private String diretor;

    private Boolean ativo;
}
