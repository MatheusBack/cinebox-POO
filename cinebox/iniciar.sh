#!/usr/bin/env sh
set -eu
cd -- "$(dirname -- "$0")"
command -v java >/dev/null 2>&1 || { echo "Configure um JDK 17 ou 21."; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "Configure o Maven 3.9+."; exit 1; }
echo "CineBox: requer JDK 17 ou 21 e Maven 3.9+."
echo "O Maven pode baixar dependências públicas na primeira execução."
echo "Use no seu ambiente pessoal ou autorizado. Confira o Java usado pelo Maven:"
mvn -version
echo "Após iniciar, acesse http://127.0.0.1:8080. Encerre com Ctrl+C."
exec mvn spring-boot:run
