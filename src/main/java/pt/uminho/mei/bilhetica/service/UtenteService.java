// service/UtenteService.java
package pt.uminho.mei.bilhetica.service;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.uminho.mei.bilhetica.dto.PerfilResponse;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.enums.PerfilUtente;
import pt.uminho.mei.bilhetica.repository.UtenteRepository;

@Service
public class UtenteService implements UserDetailsService {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;

    public UtenteService(UtenteRepository utenteRepository,
                         PasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        Utente utente = utenteRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Utente não encontrado: " + email));

        return User.builder()
                .username(utente.getEmail())
                .password(utente.getPasswordHash())
                .roles("UTENTE")
                .build();
    }

    public Utente registar(String nome, String email,
                           String telemovel, String password,
                           PerfilUtente perfil) {
        if (utenteRepository.existsByEmail(email)) {
            throw new RuntimeException("Email já registado");
        }

        Utente utente = Utente.builder()
                .nome(nome)
                .email(email)
                .telemovel(telemovel)
                .passwordHash(passwordEncoder.encode(password))
                .perfil(perfil != null ? perfil : PerfilUtente.NORMAL)
                .build();

        return utenteRepository.save(utente);
    }

    public PerfilResponse perfil(String email) {
        Utente utente = utenteRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));
        return toResponse(utente);
    }

    public PerfilResponse atualizarPerfil(String email, String nome, String telemovel) {
        Utente utente = utenteRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));

        if (nome == null || nome.isBlank()) {
            throw new RuntimeException("Nome obrigatório");
        }

        utente.setNome(nome);
        utente.setTelemovel(telemovel);

        return toResponse(utenteRepository.save(utente));
    }

    public void alterarPassword(String email, String passwordAtual, String passwordNova) {
        Utente utente = utenteRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utente não encontrado"));

        if (!passwordEncoder.matches(passwordAtual, utente.getPasswordHash())) {
            throw new RuntimeException("Password atual incorreta");
        }

        if (passwordNova == null || passwordNova.length() < 6) {
            throw new RuntimeException("A nova password deve ter pelo menos 6 caracteres");
        }

        utente.setPasswordHash(passwordEncoder.encode(passwordNova));
        utenteRepository.save(utente);
    }

    private PerfilResponse toResponse(Utente utente) {
        return PerfilResponse.builder()
                .id(utente.getId())
                .nome(utente.getNome())
                .email(utente.getEmail())
                .telemovel(utente.getTelemovel())
                .perfil(utente.getPerfil())
                .saldo(utente.getSaldo())
                .build();
    }
}
