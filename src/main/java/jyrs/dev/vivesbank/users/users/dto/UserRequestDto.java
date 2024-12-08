package jyrs.dev.vivesbank.users.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dto para las request de create y update de usuarios.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto{
        @Email(regexp = ".*@.*\\..*", message = "User name debe ser válido")
        @NotBlank(message = "El nombre de usuario no puede estar vacío")
        String username;
        @NotBlank(message = "La contraseña no puede estar vacía")
        String password;
        @NotBlank(message = "La ruta de la imagen no puede estar vacía")
        String fotoPerfil;
        Boolean isDeleted;
}
