@echo off
setlocal
cd /d "%~dp0"
echo CineBox - diario de cinema local
echo Requisitos: JDK 17 ou 21 e Maven 3.9+.
echo O Maven pode baixar dependencias publicas na primeira execucao.
echo Use somente no seu ambiente pessoal ou autorizado.
echo.
where java >nul 2>nul
if errorlevel 1 (
  echo Java nao encontrado no PATH. Configure um JDK 17 ou 21.
  pause
  exit /b 1
)
where mvn >nul 2>nul
if errorlevel 1 (
  echo Maven nao encontrado no PATH. Configure o Maven 3.9+.
  pause
  exit /b 1
)
echo Confira abaixo o Java usado pelo Maven. Ele precisa ser 17 ou superior.
call mvn -version
echo.
echo Aguarde a mensagem Started CineboxApplication.
echo Acesse http://127.0.0.1:8080 e use Ctrl+C para encerrar.
echo.
call mvn spring-boot:run
if errorlevel 1 (
  echo.
  echo Nao foi possivel iniciar. Leia o erro acima e o README.md.
  pause
  exit /b 1
)
endlocal
