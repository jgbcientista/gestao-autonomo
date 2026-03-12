#!/bin/bash
# Gera SQL de carga: 60 logs de auditoria + 60 transacoes blockchain por usuario

OUTPUT="/tmp/carga.sql"

USERS=(1 2 3 4 5)
EMAILS=("joaoguedesdebrito@gmail.com" "maria.silva@empresa.com" "carlos.santos@empresa.com" "ana.oliveira@empresa.com" "carlos.souza@empresa.com")

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

IPS=(
  "177.45.128.10" "200.158.12.45" "189.75.33.201" "170.84.15.67" "186.215.80.99"
  "187.62.44.128" "191.177.55.12" "177.71.88.34" "170.82.100.55" "177.139.22.78"
  "200.149.67.89" "187.19.33.44" "191.36.77.55" "189.112.45.66" "177.20.11.88"
  "187.45.200.12" "189.6.78.33" "177.55.90.11" "200.129.34.56" "187.95.12.78"
  "189.40.55.22" "177.124.88.99" "200.174.23.45" "177.11.55.33" "189.50.77.88"
  "200.128.12.34" "177.222.33.44" "72.14.192.10" "104.16.88.20" "74.125.45.30"
  "51.15.120.40" "91.134.200.50" "85.214.88.60" "103.5.140.70" "116.228.55.80"
  "95.213.120.90" "181.44.33.100" "190.107.55.110" "190.42.88.120" "181.135.44.130"
  "189.203.77.140" "99.226.88.150" "103.4.120.160" "49.37.55.170" "94.200.33.180"
  "105.112.44.190" "41.44.55.200" "145.131.66.210" "88.27.77.220" "151.15.88.230"
  "121.165.99.240" "171.97.33.250" "175.176.44.11" "36.91.55.22" "41.185.66.33"
  "194.210.77.44" "178.174.88.55" "83.28.99.66" "89.24.110.77" "77.75.121.88"
)

USER_AGENTS=(
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/122.0.0.0"
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Safari/605.1.15"
  "Mozilla/5.0 (Linux; Android 14; Pixel 8) Chrome/122.0.6261.64 Mobile"
  "Mozilla/5.0 (iPhone; CPU iPhone OS 17_3) Mobile/15E148 Safari/604.1"
  "Mozilla/5.0 (X11; Linux x86_64; rv:123.0) Firefox/123.0"
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Edge/122.0.2365.52"
  "Mozilla/5.0 (iPad; CPU OS 17_3) Mobile/15E148 Safari/604.1"
  "Mozilla/5.0 (Linux; Android 14; Samsung Galaxy S24) Chrome/122.0.0.0 Mobile"
  "Mozilla/5.0 (Windows NT 11.0; Win64; x64) Chrome/121.0.0.0"
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_3) Chrome/122.0.0.0"
)

DISPOSITIVOS=("Desktop Windows" "Desktop Mac" "Mobile Android" "Mobile iPhone" "Desktop Linux" "Desktop Windows Edge" "Tablet iPad" "Mobile Samsung" "Desktop Windows 11" "Desktop Mac M3")

echo "-- Carga de dados: 60 logs e 60 transacoes blockchain por usuario" > $OUTPUT
echo "BEGIN;" >> $OUTPUT

for u in $(seq 0 4); do
  UID_VAL=${USERS[$u]}
  EMAIL=${EMAILS[$u]}

  echo "" >> $OUTPUT
  echo "-- Usuario: $EMAIL (ID=$UID_VAL)" >> $OUTPUT

  for i in $(seq 1 60); do
    IDX=$(( (i - 1) % ${#LOCATIONS[@]} ))
    LOC="${LOCATIONS[$IDX]}"
    IP="${IPS[$IDX]}"
    UA_IDX=$(( (i - 1) % ${#USER_AGENTS[@]} ))
    UA="${USER_AGENTS[$UA_IDX]}"
    DISP="${DISPOSITIVOS[$UA_IDX]}"

    # Horarios de madrugada (00:00 a 05:59) nos ultimos 30 dias
    DAYS_AGO=$(( (i * 13 + u * 7) % 30 ))
    HOUR=$(( (i * 3 + u * 2) % 6 ))
    MINUTE=$(( (i * 7 + u * 11) % 60 ))

    # Sucesso na maioria, algumas falhas para variedade
    if [ $(( (i + u) % 8 )) -eq 0 ]; then
      SUCESSO="false"
      TIPO="LOGIN_FAILED"
      MOTIVO="Tentativa suspeita de acesso"
      DESC="Falha de autenticacao de $LOC"
    else
      SUCESSO="true"
      TIPO="LOGIN_SUCCESS"
      MOTIVO="NULL"
      DESC="Login bem-sucedido de $LOC"
    fi

    TIMESTAMP="2026-03-$(printf '%02d' $((12 - DAYS_AGO)))T$(printf '%02d' $HOUR):$(printf '%02d' $MINUTE):00"

    # Validar data (se dia < 1, ajustar)
    DAY=$((12 - DAYS_AGO))
    if [ $DAY -lt 1 ]; then
      DAY=$(( DAY + 28 ))
      TIMESTAMP="2026-02-$(printf '%02d' $DAY)T$(printf '%02d' $HOUR):$(printf '%02d' $MINUTE):00"
    fi

    # Log de auditoria
    if [ "$MOTIVO" == "NULL" ]; then
      echo "INSERT INTO log_auditoria (usuario_id, tipo_evento, descricao, endereco_ip, agente_usuario, localizacao, info_dispositivo, data_hora, sucesso, motivo_falha, criado_em) VALUES ($UID_VAL, '$TIPO', '$DESC', '$IP', '$UA', '$LOC', '$DISP', '$TIMESTAMP', $SUCESSO, NULL, '$TIMESTAMP');" >> $OUTPUT
    else
      echo "INSERT INTO log_auditoria (usuario_id, tipo_evento, descricao, endereco_ip, agente_usuario, localizacao, info_dispositivo, data_hora, sucesso, motivo_falha, criado_em) VALUES ($UID_VAL, '$TIPO', '$DESC', '$IP', '$UA', '$LOC', '$DISP', '$TIMESTAMP', $SUCESSO, '$MOTIVO', '$TIMESTAMP');" >> $OUTPUT
    fi

    # Gerar hash unico para blockchain
    HASH_DATA=$(echo -n "${EMAIL}${TIMESTAMP}${IP}${LOC}${i}" | md5sum | cut -d' ' -f1)
    HASH_TX=$(echo -n "tx_${EMAIL}_${i}_${TIMESTAMP}" | md5sum | cut -d' ' -f1)
    HASH_BLOCO=$(echo -n "blk_${i}_${TIMESTAMP}" | md5sum | cut -d' ' -f1)

    if [ "$SUCESSO" == "true" ]; then
      DECISAO="ALLOWED"
      RISCO_INT=$(( (i % 40) ))
      RISCO="0.${RISCO_INT}"
      [ $RISCO_INT -lt 10 ] && RISCO="0.0${RISCO_INT}"
    else
      DECISAO="DENIED"
      RISCO_INT=$(( 70 + (i % 30) ))
      RISCO="0.${RISCO_INT}"
    fi

    BLOCO_NUM=$(( 1000000 + i + u * 60 ))

    echo "INSERT INTO transacoes_blockchain (usuario_id, usuario_email, tipo_evento, hash_transacao, hash_dados, hash_bloco, numero_bloco, endereco_ip, localizacao, impressao_digital_dispositivo, pontuacao_risco, decisao, nome_rede, status_confirmacao, verificado, criado_em, confirmado_em) VALUES ($UID_VAL, '$EMAIL', '$TIPO', '$HASH_TX', '$HASH_DATA', '$HASH_BLOCO', $BLOCO_NUM, '$IP', '$LOC', '$DISP', $RISCO, '$DECISAO', 'hyperledger-fabric', 'CONFIRMADO', true, '$TIMESTAMP', '$TIMESTAMP');" >> $OUTPUT

  done
done

echo "" >> $OUTPUT
echo "COMMIT;" >> $OUTPUT

echo "SQL gerado em $OUTPUT"
wc -l $OUTPUT
