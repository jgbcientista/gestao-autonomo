//package br.com.auth.config;
//
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//
//import br.com.auth.service.BlockchainService;
//import br.com.auth.service.ContextAnalysisService;
//import br.com.auth.service.AiContextAnalysisService;
//import br.com.auth.service.ServicoAnaliseComportamentalIA;
//import br.com.auth.service.ServicoGeolocalizacao;
//import br.com.auth.service.ServicoScoreConfianca;
//import br.com.auth.dominio.entidades.Usuario;
//import br.com.auth.dto.RequisicaoAnaliseContexto;
//import br.com.auth.dto.RespostaAnaliseContexto;
//import br.com.auth.dominio.interfaces.IServicoAnaliseComportamentalIA;
//
///**
// * Configuração mínima para desenvolvimento
// * Fornece implementações mock dos serviços complexos
// */
//@Configuration
//@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "dev")
//public class MinimalAuthConfig {
//
//    @Bean
//    @Primary
//    public BlockchainService mockBlockchainService() {
//        return new BlockchainService() {
//            @Override
//            public void recordAuthenticationEvent(Usuario usuario, String eventType, String status, 
//                    Double trustScore, String ipAddress, String location, String deviceFingerprint) {
//                // Mock implementation - não faz nada
//            }
//        };
//    }
//
//    @Bean
//    @Primary
//    public ContextAnalysisService mockContextAnalysisService() {
//        return new ContextAnalysisService() {
//            public void analyzeContext(Usuario usuario, Object request) {
//                // Mock implementation - não faz nada
//            }
//        };
//    }
//
//    @Bean
//    @Primary
//    public AiContextAnalysisService mockAiContextAnalysisService() {
//        return new AiContextAnalysisService() {
//            public RespostaAnaliseContexto analyzeContext(Usuario usuario, RequisicaoAnaliseContexto request) {
//                return RespostaAnaliseContexto.builder()
//                    .decisao("PERMITIR")
//                    .confianca(0.8)
//                    .build();
//            }
//        };
//    }
//
//    @Bean
//    @Primary
//    public ServicoAnaliseComportamentalIA mockServicoAnaliseComportamentalIA() {
//        return new ServicoAnaliseComportamentalIA() {
//            @Override
//            public IServicoAnaliseComportamentalIA.PerfilComportamentalIA analisarComportamento(
//                    Usuario usuario, IServicoAnaliseComportamentalIA.DadosContextoAcesso dadosContexto) {
//                
//                return new IServicoAnaliseComportamentalIA.PerfilComportamentalIA() {
//                    @Override
//                    public Double getScoreAnomalia() { return 0.1; }
//                    
//                    @Override
//                    public String getClassificacaoAcesso() { return "NORMAL"; }
//                    
//                    @Override
//                    public String getRecomendacaoSeguranca() { return "Nenhuma ação necessária"; }
//                };
//            }
//        };
//    }
//
//    @Bean
//    @Primary
//    public ServicoGeolocalizacao mockServicoGeolocalizacao() {
//        return new ServicoGeolocalizacao() {
//            public DadosGeolocalizacao obterLocalizacaoPorIp(String ip) {
//                return new DadosGeolocalizacao() {
//                    public String getPais() { return "Brasil"; }
//                    public String getEstado() { return "São Paulo"; }
//                    public String getCidade() { return "São Paulo"; }
//                    public String getFusoHorario() { return "America/Sao_Paulo"; }
//                    public String getProvedor() { return "Mock ISP"; }
//                    public Double getLatitude() { return -23.5505; }
//                    public Double getLongitude() { return -46.6333; }
//                };
//            }
//        };
//    }
//
//    @Bean
//    @Primary
//    public ServicoScoreConfianca mockServicoScoreConfianca() {
//        return new ServicoScoreConfianca() {
//            public ScoreConfianca calcularScore(Usuario usuario, 
//                    IServicoAnaliseComportamentalIA.PerfilComportamentalIA perfil) {
//                return new ScoreConfianca() {
//                    public Double getScoreAtual() { return 0.8; }
//                    public String getNivelConfianca() { return "ALTO"; }
//                    public String getMotivoAlteracao() { return "Score padrão"; }
//                };
//            }
//            
//            public DecisaoAutenticacao determinarDecisao(ScoreConfianca score, 
//                    IServicoAnaliseComportamentalIA.PerfilComportamentalIA perfil) {
//                return DecisaoAutenticacao.PERMITIR;
//            }
//            
//            public void atualizarAposLogin(Usuario usuario, TipoEventoLogin tipo, 
//                    IServicoAnaliseComportamentalIA.PerfilComportamentalIA perfil) {
//                // Mock implementation
//            }
//        };
//    }
//} 