package br.com.auth.service;

import br.com.auth.dominio.entidades.LogAuditoria;
import br.com.auth.dominio.entidades.Usuario;
import br.com.auth.dominio.interfaces.IServicoAnaliseComportamentalIA.DadosContextoAcesso;
import br.com.auth.dominio.interfaces.IServicoAnaliseComportamentalIA.DadosFeatures;
import br.com.auth.infraestrutura.repositorios.RepositorioLogAuditoria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServicoExtracaoFeatures {

    private final RepositorioLogAuditoria repositorioLogAuditoria;
    private final ServicoGeolocalizacao servicoGeolocalizacao;

    public DadosFeatures extrairFeatures(Usuario usuario, DadosContextoAcesso dadosContexto) {
        log.debug("Extraindo features para usuário: {}", usuario.getEmail());

        // Obter histórico de logs do usuário (últimos 30 dias)
        LocalDateTime dataInicio = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        List<LogAuditoria> historico = repositorioLogAuditoria.findByUsuarioAndDataMaiorQue(usuario, dataInicio);

        return new DadosFeatures(
            extrairHoraAcesso(),
            extrairDiaSemana(),
            calcularFrequenciaAcessoSemanal(historico),
            verificarIpJaUtilizado(historico, dadosContexto.enderecoIp()),
            verificarDispositivoJaUtilizado(historico, dadosContexto.userAgent()),
            verificarLocalizacaoJaUtilizada(historico, dadosContexto.localizacaoGeografica()),
            calcularDistanciaLocalizacaoHabitual(historico, dadosContexto.localizacaoGeografica()),
            calcularDiferencaHorarioHabitual(historico),
            calcularTempoDesdeUltimoAcesso(historico),
            calcularMediaSessoesDiarias(historico),
            calcularDesvioPadraoHorarios(historico),
            contarIpsDistintos(historico),
            contarDispositivosDistintos(historico),
            calcularPadroesNavegacaoScore(historico)
        );
    }

    private Double extrairHoraAcesso() {
        return (double) LocalDateTime.now().getHour();
    }

    private Double extrairDiaSemana() {
        return (double) LocalDateTime.now().getDayOfWeek().getValue();
    }

    private Double calcularFrequenciaAcessoSemanal(List<LogAuditoria> historico) {
        if (historico.isEmpty()) {
            return 0.0;
        }

        // Contar acessos por semana
        long diasComAcesso = historico.stream()
            .map(log -> log.getCriadoEm().toLocalDate())
            .collect(Collectors.toSet())
            .size();

        return (double) historico.size() / Math.max(1, diasComAcesso / 7.0);
    }

    private Boolean verificarIpJaUtilizado(List<LogAuditoria> historico, String enderecoIp) {
        if (enderecoIp == null) return false;
        
        return historico.stream()
            .anyMatch(log -> enderecoIp.equals(log.getEnderecoIp()));
    }

    private Boolean verificarDispositivoJaUtilizado(List<LogAuditoria> historico, String userAgent) {
        if (userAgent == null) return false;
        
        return historico.stream()
            .anyMatch(log -> userAgent.equals(log.getUserAgent()));
    }

    private Boolean verificarLocalizacaoJaUtilizada(List<LogAuditoria> historico, String localizacao) {
        if (localizacao == null) return false;
        
        return historico.stream()
            .anyMatch(log -> localizacao.equals(log.getLocalizacao()));
    }

    private Double calcularDistanciaLocalizacaoHabitual(List<LogAuditoria> historico, String localizacaoAtual) {
        if (localizacaoAtual == null || historico.isEmpty()) {
            return 1000.0; // Distância máxima se não há dados
        }

        try {
            // Obter coordenadas da localização atual
            ServicoGeolocalizacao.DadosGeolocalizacao locAtual = 
                servicoGeolocalizacao.obterCoordenadasPorEndereco(localizacaoAtual);

            if (!locAtual.isValido()) {
                log.debug("Coordenadas inválidas para localização: {}", localizacaoAtual);
                return 500.0; // Distância padrão para localizações inválidas
            }

            // Encontrar localização mais frequente no histórico
            String localizacaoHabitual = historico.stream()
                .filter(log -> log.getLocalizacao() != null)
                .collect(Collectors.groupingBy(LogAuditoria::getLocalizacao, Collectors.counting()))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse(localizacaoAtual);

            // Se é a mesma localização textual, distância zero
            if (localizacaoAtual.equals(localizacaoHabitual)) {
                return 0.0;
            }

            // Obter coordenadas da localização habitual
            ServicoGeolocalizacao.DadosGeolocalizacao locHabitual = 
                servicoGeolocalizacao.obterCoordenadasPorEndereco(localizacaoHabitual);

            if (!locHabitual.isValido()) {
                log.debug("Coordenadas inválidas para localização habitual: {}", localizacaoHabitual);
                return 300.0; // Distância moderada para localizações habituais inválidas
            }

            // Calcular distância real usando fórmula de Haversine
            double distancia = servicoGeolocalizacao.calcularDistancia(locAtual, locHabitual);
            
            log.debug("Distância calculada entre '{}' e '{}': {:.2f} km", 
                     localizacaoAtual, localizacaoHabitual, distancia);
            
            return distancia;

        } catch (Exception e) {
            log.warn("Erro ao calcular distância de localização para usuário. Usando valor padrão: {}", e.getMessage());
            return 250.0; // Distância padrão em caso de erro
        }
    }

    private Double calcularDiferencaHorarioHabitual(List<LogAuditoria> historico) {
        if (historico.isEmpty()) {
            return 12.0; // Diferença máxima se não há dados
        }

        // Calcular horário médio de acesso
        double horarioMedio = historico.stream()
            .mapToInt(log -> log.getCriadoEm().getHour())
            .average()
            .orElse(12.0);

        int horarioAtual = LocalDateTime.now().getHour();
        return Math.abs(horarioAtual - horarioMedio);
    }

    private Double calcularTempoDesdeUltimoAcesso(List<LogAuditoria> historico) {
        if (historico.isEmpty()) {
            return 168.0; // 1 semana em horas se não há dados
        }

        LocalDateTime ultimoAcesso = historico.stream()
            .map(LogAuditoria::getCriadoEm)
            .max(LocalDateTime::compareTo)
            .orElse(LocalDateTime.now().minus(7, ChronoUnit.DAYS));

        return (double) ChronoUnit.HOURS.between(ultimoAcesso, LocalDateTime.now());
    }

    private Double calcularMediaSessoesDiarias(List<LogAuditoria> historico) {
        if (historico.isEmpty()) {
            return 0.0;
        }

        // Agrupar por dia e contar sessões
        long diasComAcesso = historico.stream()
            .map(log -> log.getCriadoEm().toLocalDate())
            .collect(Collectors.toSet())
            .size();

        return (double) historico.size() / Math.max(1, diasComAcesso);
    }

    private Double calcularDesvioPadraoHorarios(List<LogAuditoria> historico) {
        if (historico.size() < 2) {
            return 12.0; // Desvio máximo se poucos dados
        }

        List<Integer> horarios = historico.stream()
            .map(log -> log.getCriadoEm().getHour())
            .collect(Collectors.toList());

        double media = horarios.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);

        double variancia = horarios.stream()
            .mapToDouble(hora -> Math.pow(hora - media, 2))
            .average()
            .orElse(0.0);

        return Math.sqrt(variancia);
    }

    private Integer contarIpsDistintos(List<LogAuditoria> historico) {
        Set<String> ipsDistintos = historico.stream()
            .map(LogAuditoria::getEnderecoIp)
            .filter(ip -> ip != null && !ip.isEmpty())
            .collect(Collectors.toSet());

        return ipsDistintos.size();
    }

    private Integer contarDispositivosDistintos(List<LogAuditoria> historico) {
        Set<String> dispositivosDistintos = historico.stream()
            .map(LogAuditoria::getUserAgent)
            .filter(ua -> ua != null && !ua.isEmpty())
            .collect(Collectors.toSet());

        return dispositivosDistintos.size();
    }

    private Double calcularPadroesNavegacaoScore(List<LogAuditoria> historico) {
        if (historico.isEmpty()) {
            return 0.5; // Score neutro se não há dados
        }

        // Analisar padrões de navegação baseado em ações
        long acoesUnicas = historico.stream()
            .map(LogAuditoria::getAcao)
            .filter(acao -> acao != null)
            .collect(Collectors.toSet())
            .size();

        // Normalizar score (mais ações únicas = comportamento mais diverso)
        return Math.min(1.0, acoesUnicas / 10.0);
    }
} 