package br.com.auth.infraestrutura.repositorios;

import br.com.auth.dominio.entidades.SessaoAtiva;
import br.com.auth.dominio.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioSessaoAtiva extends JpaRepository<SessaoAtiva, Long> {
    
    List<SessaoAtiva> findByStatus(String status);
    
    List<SessaoAtiva> findByUsuario(Usuario usuario);
    
    Optional<SessaoAtiva> findByTokenJwt(String tokenJwt);
    
    @Query("SELECT s FROM SessaoAtiva s WHERE s.lastActivity > :threshold AND s.status = 'ATIVA'")
    List<SessaoAtiva> findActiveSessions(LocalDateTime threshold);
    
    @Query("SELECT COUNT(s) FROM SessaoAtiva s WHERE s.status = 'ATIVA'")
    long countActiveSessions();
    
    @Query("SELECT COUNT(DISTINCT s.deviceInfo) FROM SessaoAtiva s WHERE s.status = 'ATIVA'")
    long countUniqueDevices();
    
    @Query("SELECT COUNT(DISTINCT s.location) FROM SessaoAtiva s WHERE s.status = 'ATIVA'")
    long countUniqueLocations();
    
    @Query("SELECT s FROM SessaoAtiva s WHERE s.riskLevel = 'ALTO' AND s.status = 'ATIVA'")
    List<SessaoAtiva> findHighRiskSessions();
} 