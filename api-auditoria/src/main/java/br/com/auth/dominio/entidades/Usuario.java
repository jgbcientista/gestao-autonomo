package br.com.auth.dominio.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
@EntityListeners(AuditingEntityListener.class)
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail deve ser válido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Column(nullable = false)
    private String senha;

    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_perfis", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "perfil")
    @Builder.Default
    private Set<String> perfis = new HashSet<>(Set.of("USUARIO_PADRAO"));

    @Column(name = "ultimo_login_ip", length = 45)
    private String ultimoLoginIp;

    @Column(name = "ultimo_login_localizacao", length = 100)
    private String ultimoLoginLocalizacao;

    @Column(name = "ultimo_login_dispositivo", length = 200)
    private String ultimoLoginDispositivo;

    @Column(name = "ultimo_login_data")
    private LocalDateTime ultimoLoginData;

    @Column(name = "tentativas_login_falhadas")
    @Builder.Default
    private Integer tentativasLoginFalhadas = 0;

    @Column(name = "conta_bloqueada")
    @Builder.Default
    private Boolean contaBloqueada = false;

    @Column(name = "conta_bloqueada_ate")
    private LocalDateTime contaBloqueadaAte;

    @CreatedDate
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @LastModifiedDate
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @Column(name = "ultima_mudanca_senha")
    private LocalDateTime ultimaMudancaSenha;

    @Column(name = "autenticacao_dois_fatores_habilitada")
    @Builder.Default
    private Boolean autenticacaoDoisFatoresHabilitada = false;

    @Column(name = "segredo_dois_fatores", length = 64)
    private String segredoDoisFatores;

    @Column(name = "lock_reason", length = 255)
    private String lockReason;

    @Column(name = "status_conta", length = 40)
    @Builder.Default
    private String statusConta = "PENDENTE_APROVACAO_GESTOR";

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return perfis.stream()
                .map(perfil -> new SimpleGrantedAuthority("ROLE_" + perfil))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (contaBloqueada == null) return true;
        if (!contaBloqueada) return true;
        if (contaBloqueadaAte == null) return false;
        return LocalDateTime.now().isAfter(contaBloqueadaAte);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        if (ultimaMudancaSenha == null) return true;
        return LocalDateTime.now().minusDays(90).isBefore(ultimaMudancaSenha);
    }

    @Override
    public boolean isEnabled() {
        return !contaBloqueada;
    }

    // Métodos auxiliares para compatibilidade com código existente
    public String getName() {
        return nome;
    }

    public void setName(String nome) {
        this.nome = nome;
    }

    public String getLastLoginIp() {
        return ultimoLoginIp;
    }

    public void setLastLoginIp(String ip) {
        this.ultimoLoginIp = ip;
    }

    public String getLastLoginLocation() {
        return ultimoLoginLocalizacao;
    }

    public void setLastLoginLocation(String localizacao) {
        this.ultimoLoginLocalizacao = localizacao;
    }

    public String getLastLoginDevice() {
        return ultimoLoginDispositivo;
    }

    public void setLastLoginDevice(String dispositivo) {
        this.ultimoLoginDispositivo = dispositivo;
    }

    public LocalDateTime getLastLoginTime() {
        return ultimoLoginData;
    }

    public void setLastLoginTime(LocalDateTime data) {
        this.ultimoLoginData = data;
    }

    public Integer getFailedLoginAttempts() {
        return tentativasLoginFalhadas;
    }

    public void setFailedLoginAttempts(Integer tentativas) {
        this.tentativasLoginFalhadas = tentativas;
    }

    public Boolean getAccountLocked() {
        return contaBloqueada;
    }

    public void setAccountLocked(Boolean bloqueada) {
        this.contaBloqueada = bloqueada;
    }

    public LocalDateTime getAccountLockedUntil() {
        return contaBloqueadaAte;
    }

    public void setAccountLockedUntil(LocalDateTime data) {
        this.contaBloqueadaAte = data;
    }

    public String getRole() {
        return perfis.stream().findFirst().orElse("USUARIO_PADRAO");
    }

    public Boolean getTwoFactorEnabled() {
        return autenticacaoDoisFatoresHabilitada;
    }

    public void setTwoFactorEnabled(Boolean habilitado) {
        this.autenticacaoDoisFatoresHabilitada = habilitado;
    }

    public String getLockReason() {
        return lockReason;
    }

    public void setLockReason(String lockReason) {
        this.lockReason = lockReason;
    }

    public String getStatusConta() {
        return statusConta;
    }

    public void setStatusConta(String statusConta) {
        this.statusConta = statusConta;
    }
} 