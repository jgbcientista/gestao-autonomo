#!/bin/bash
# Script de carga: 60 logins por usuario com localizacoes diversas e horarios de madrugada

API_URL="http://localhost:8081/api/v1/autenticacao/entrar"
PASSWORD="123456"

# Usuarios cadastrados
USERS=("joaoguedesdebrito@gmail.com" "maria.silva@empresa.com" "carlos.santos@empresa.com" "ana.oliveira@empresa.com" "carlos.souza@empresa.com")

# Localizacoes: estados brasileiros + paises estrangeiros
LOCATIONS=(
  "Sao Paulo, SP, BR"
  "Rio de Janeiro, RJ, BR"
  "Salvador, BA, BR"
  "Belo Horizonte, MG, BR"
  "Curitiba, PR, BR"
  "Recife, PE, BR"
  "Porto Alegre, RS, BR"
  "Fortaleza, CE, BR"
  "Brasilia, DF, BR"
  "Manaus, AM, BR"
  "Belem, PA, BR"
  "Goiania, GO, BR"
  "Florianopolis, SC, BR"
  "Vitoria, ES, BR"
  "Natal, RN, BR"
  "Campo Grande, MS, BR"
  "Maceio, AL, BR"
  "Sao Luis, MA, BR"
  "Joao Pessoa, PB, BR"
  "Teresina, PI, BR"
  "Cuiaba, MT, BR"
  "Aracaju, SE, BR"
  "Porto Velho, RO, BR"
  "Macapa, AP, BR"
  "Palmas, TO, BR"
  "Rio Branco, AC, BR"
  "Boa Vista, RR, BR"
  "New York, NY, US"
  "Los Angeles, CA, US"
  "Miami, FL, US"
  "London, UK"
  "Paris, FR"
  "Berlin, DE"
  "Tokyo, JP"
  "Beijing, CN"
  "Moscow, RU"
  "Buenos Aires, AR"
  "Santiago, CL"
  "Lima, PE"
  "Bogota, CO"
  "Mexico City, MX"
  "Toronto, CA"
  "Sydney, AU"
  "Mumbai, IN"
  "Dubai, AE"
  "Lagos, NG"
  "Cairo, EG"
  "Amsterdam, NL"
  "Madrid, ES"
  "Roma, IT"
  "Seoul, KR"
  "Bangkok, TH"
  "Singapore, SG"
  "Jakarta, ID"
  "Cape Town, ZA"
  "Lisbon, PT"
  "Stockholm, SE"
  "Warsaw, PL"
  "Prague, CZ"
  "Vienna, AT"
)

# IPs simulados por regiao
IPS=(
  "177.45.128.10"    # SP
  "200.158.12.45"    # RJ
  "189.75.33.201"    # BA
  "170.84.15.67"     # MG
  "186.215.80.99"    # PR
  "187.62.44.128"    # PE
  "191.177.55.12"    # RS
  "177.71.88.34"     # CE
  "170.82.100.55"    # DF
  "177.139.22.78"    # AM
  "200.149.67.89"    # PA
  "187.19.33.44"     # GO
  "191.36.77.55"     # SC
  "189.112.45.66"    # ES
  "177.20.11.88"     # RN
  "187.45.200.12"    # MS
  "189.6.78.33"      # AL
  "177.55.90.11"     # MA
  "200.129.34.56"    # PB
  "187.95.12.78"     # PI
  "189.40.55.22"     # MT
  "177.124.88.99"    # SE
  "200.174.23.45"    # RO
  "177.11.55.33"     # AP
  "189.50.77.88"     # TO
  "200.128.12.34"    # AC
  "177.222.33.44"    # RR
  "72.14.192.10"     # US NY
  "104.16.88.20"     # US LA
  "74.125.45.30"     # US Miami
  "51.15.120.40"     # UK
  "91.134.200.50"    # FR
  "85.214.88.60"     # DE
  "103.5.140.70"     # JP
  "116.228.55.80"    # CN
  "95.213.120.90"    # RU
  "181.44.33.100"    # AR
  "190.107.55.110"   # CL
  "190.42.88.120"    # PE
  "181.135.44.130"   # CO
  "189.203.77.140"   # MX
  "99.226.88.150"    # CA
  "103.4.120.160"    # AU
  "49.37.55.170"     # IN
  "94.200.33.180"    # AE
  "105.112.44.190"   # NG
  "41.44.55.200"     # EG
  "145.131.66.210"   # NL
  "88.27.77.220"     # ES
  "151.15.88.230"    # IT
  "121.165.99.240"   # KR
  "171.97.33.250"    # TH
  "175.176.44.11"    # SG
  "36.91.55.22"      # ID
  "41.185.66.33"     # ZA
  "194.210.77.44"    # PT
  "178.174.88.55"    # SE
  "83.28.99.66"      # PL
  "89.24.110.77"     # CZ
  "77.75.121.88"     # AT
)

# User-agents variados
USER_AGENTS=(
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2.1 Safari/605.1.15"
  "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.64 Mobile Safari/537.36"
  "Mozilla/5.0 (iPhone; CPU iPhone OS 17_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1"
  "Mozilla/5.0 (X11; Linux x86_64; rv:123.0) Gecko/20100101 Firefox/123.0"
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Edge/122.0.2365.52"
  "Mozilla/5.0 (iPad; CPU OS 17_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1"
  "Mozilla/5.0 (Linux; Android 14; Samsung Galaxy S24) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
  "Mozilla/5.0 (Windows NT 11.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
)

echo "============================================="
echo "  CARGA DE LOGINS - Sistema de Autenticacao"
echo "  60 logins por usuario = $(( ${#USERS[@]} * 60 )) total"
echo "============================================="
echo ""

TOTAL_OK=0
TOTAL_FAIL=0

for EMAIL in "${USERS[@]}"; do
  echo "-------------------------------------------"
  echo "Usuario: $EMAIL"
  echo "-------------------------------------------"

  OK=0
  FAIL=0

  for i in $(seq 1 60); do
    # Seleciona localizacao e IP correspondente
    IDX=$(( (i - 1) % ${#LOCATIONS[@]} ))
    LOC="${LOCATIONS[$IDX]}"
    IP="${IPS[$IDX]}"

    # Seleciona user-agent variado
    UA_IDX=$(( (i - 1) % ${#USER_AGENTS[@]} ))
    UA="${USER_AGENTS[$UA_IDX]}"

    # Faz o login
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
      -X POST "$API_URL" \
      -H "Content-Type: application/json" \
      -H "X-Forwarded-For: $IP" \
      -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\",\"ipAddress\":\"$IP\",\"userAgent\":\"$UA\",\"location\":\"$LOC\"}")

    if [ "$HTTP_CODE" == "200" ]; then
      OK=$((OK + 1))
      STATUS="OK"
    else
      FAIL=$((FAIL + 1))
      STATUS="FAIL($HTTP_CODE)"
    fi

    # Mostra progresso a cada 10
    if [ $((i % 10)) -eq 0 ]; then
      echo "  [$i/60] Sucesso: $OK | Falha: $FAIL | Ultimo: $LOC ($STATUS)"
    fi

    # Pequena pausa para nao sobrecarregar
    sleep 0.1
  done

  TOTAL_OK=$((TOTAL_OK + OK))
  TOTAL_FAIL=$((TOTAL_FAIL + FAIL))
  echo "  >> Total usuario: $OK sucesso, $FAIL falhas"
  echo ""
done

echo "============================================="
echo "  RESULTADO FINAL"
echo "  Total Sucesso: $TOTAL_OK"
echo "  Total Falhas:  $TOTAL_FAIL"
echo "  Total Geral:   $((TOTAL_OK + TOTAL_FAIL))"
echo "============================================="
