package br.com.auth.controller;

import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.infraestrutura.repositorios.RepositorioUsuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/gestao-acesso")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestao de Acesso", description = "APIs para aprovacao e rejeicao de usuarios pendentes")
public class GestaoAcessoController {

    private final RepositorioUsuario repositorioUsuario;

    @GetMapping("/pendentes")
    @Operation(summary = "Listar usuarios pendentes de aprovacao")
    public ResponseEntity<?> listarPendentes() {
        try {
            List<Usuario> pendentes = repositorioUsuario.findByStatusConta("PENDENTE_APROVACAO_GESTOR");

            List<Map<String, Object>> resultado = pendentes.stream()
                    .map(u -> Map.<String, Object>of(
                            "id", u.getId(),
                            "nome", u.getNome(),
                            "email", u.getEmail(),
                            "statusConta", u.getStatusConta(),
                            "criadoEm", u.getCriadoEm() != null ? u.getCriadoEm().toString() : ""
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Erro ao listar usuarios pendentes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro ao listar pendentes: " + e.getMessage()));
        }
    }

    @PostMapping("/aprovar/{usuarioId}")
    @Operation(summary = "Aprovar acesso de um usuario")
    public ResponseEntity<?> aprovarUsuario(@PathVariable Long usuarioId) {
        try {
            Usuario usuario = repositorioUsuario.findById(usuarioId)
                    .orElse(null);

            if (usuario == null) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Usuario nao encontrado"));
            }

            if (!"PENDENTE_APROVACAO_GESTOR".equals(usuario.getStatusConta())) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Usuario nao esta pendente de aprovacao"));
            }

            usuario.setStatusConta("ATIVO");
            repositorioUsuario.save(usuario);

            log.info("Usuario {} aprovado com sucesso", usuario.getEmail());

            return ResponseEntity.ok(Map.of(
                    "mensagem", "Usuario aprovado com sucesso",
                    "id", usuario.getId(),
                    "nome", usuario.getNome(),
                    "email", usuario.getEmail(),
                    "statusConta", usuario.getStatusConta()
            ));
        } catch (Exception e) {
            log.error("Erro ao aprovar usuario {}: {}", usuarioId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro ao aprovar usuario: " + e.getMessage()));
        }
    }

    @PostMapping("/rejeitar/{usuarioId}")
    @Operation(summary = "Rejeitar acesso de um usuario")
    public ResponseEntity<?> rejeitarUsuario(@PathVariable Long usuarioId) {
        try {
            Usuario usuario = repositorioUsuario.findById(usuarioId)
                    .orElse(null);

            if (usuario == null) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Usuario nao encontrado"));
            }

            if (!"PENDENTE_APROVACAO_GESTOR".equals(usuario.getStatusConta())) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Usuario nao esta pendente de aprovacao"));
            }

            usuario.setStatusConta("REJEITADO");
            repositorioUsuario.save(usuario);

            log.info("Usuario {} rejeitado", usuario.getEmail());

            return ResponseEntity.ok(Map.of(
                    "mensagem", "Usuario rejeitado",
                    "id", usuario.getId(),
                    "nome", usuario.getNome(),
                    "email", usuario.getEmail(),
                    "statusConta", usuario.getStatusConta()
            ));
        } catch (Exception e) {
            log.error("Erro ao rejeitar usuario {}: {}", usuarioId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro ao rejeitar usuario: " + e.getMessage()));
        }
    }

    @GetMapping("/todos")
    @Operation(summary = "Listar todos os usuarios com status")
    public ResponseEntity<?> listarTodos() {
        try {
            List<Usuario> todos = repositorioUsuario.findAll();

            List<Map<String, Object>> resultado = todos.stream()
                    .map(u -> Map.<String, Object>of(
                            "id", u.getId(),
                            "nome", u.getNome(),
                            "email", u.getEmail(),
                            "statusConta", u.getStatusConta() != null ? u.getStatusConta() : "ATIVO",
                            "perfis", u.getPerfis().toString(),
                            "criadoEm", u.getCriadoEm() != null ? u.getCriadoEm().toString() : ""
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Erro ao listar usuarios: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("erro", "Erro ao listar usuarios: " + e.getMessage()));
        }
    }
}
