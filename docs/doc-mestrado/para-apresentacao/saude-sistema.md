**Saúde do sistema**

Os valores são reais, vêm do backend via API /api/v1/health/system. Aqui está o que cada um significa:

  **CPU:** 0% — É real, mas com uma ressalva. No Windows, getSystemLoadAverage() retorna -1, então o código usa getProcessCpuLoad() como alternativa (linha 107). Esse método retorna a carga do processo Java, não
  do sistema inteiro. Se a API estiver ociosa no momento da chamada, retorna ~0%. É normal.

  **Memória:** 4% — É real. Mede o heap da JVM (heapUsed / heapMax), não a RAM do sistema. Se o heapMax é grande (ex: 512MB ou 1GB) e o uso é baixo, 4% faz sentido para uma aplicação Spring Boot em repouso.

  **IA Load:** 47% — Este é semi-real. A fórmula na linha 148 é:
  iaLoadPercent = Math.min(100, totalAnalises > 0 ? 40 + (totalAnalises % 50) : 10);
  Não mede carga real dos modelos de IA. É uma estimativa baseada no total de análises no banco: 40 + (totalAnalises % 50). Se você tem 7 análises, dá 40 + 7 = 47%. É um indicador de atividade, não de uso real
   de CPU/GPU pelos modelos.