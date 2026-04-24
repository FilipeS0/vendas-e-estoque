package com.filipe.api.config;

import com.filipe.api.domain.produto.Categoria;
import com.filipe.api.domain.produto.CategoriaRepository;
import com.filipe.api.domain.produto.Fornecedor;
import com.filipe.api.domain.produto.FornecedorRepository;
import com.filipe.api.domain.usuario.Perfil;
import com.filipe.api.domain.usuario.PerfilRepository;
import com.filipe.api.domain.usuario.Usuario;
import com.filipe.api.domain.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final FornecedorRepository fornecedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (categoriaRepository.count() == 0) {
            categoriaRepository.save(Categoria.builder().nome("Bebidas").descricao("Refrigerantes, Sucos e Cervejas").build());
            categoriaRepository.save(Categoria.builder().nome("Mercearia").descricao("Alimentos em geral").build());
        }

        if (fornecedorRepository.count() == 0) {
            fornecedorRepository.save(Fornecedor.builder().nome("Ambev S.A.").cnpj("07526557000100").build());
            fornecedorRepository.save(Fornecedor.builder().nome("Coca-Cola Indústrias Ltda.").cnpj("45997418000153").build());
        }

        if (perfilRepository.count() == 0) {
            Perfil adminPerfil = perfilRepository.save(Perfil.builder().nome("ADMIN").build());
            
            if (usuarioRepository.count() == 0) {
                usuarioRepository.save(Usuario.builder()
                        .nome("Administrador")
                        .email("admin@erp.com")
                        .senhaHash(passwordEncoder.encode("admin123"))
                        .perfil(adminPerfil)
                        .build());
            }
        }
    }
}
