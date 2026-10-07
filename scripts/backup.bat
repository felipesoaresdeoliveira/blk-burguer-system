@echo off
chcp 65001 >nul
rem BLK Burguer - backup do banco PostgreSQL (#42)
rem Uso: backup.bat [banco]          (padrão: blkburguer)
rem Gera backups\blkburguer_AAAA-MM-DD_HHMM.backup (formato custom do pg_dump).
rem Conexão: variáveis PGHOST, PGPORT, PGUSER (padrão localhost, 5432, postgres).
rem A senha é pedida pelo pg_dump (ou use a variável PGPASSWORD / o arquivo pgpass.conf).
setlocal

set "BANCO=%~1"
if "%BANCO%"=="" set "BANCO=blkburguer"
if "%PGHOST%"=="" set "PGHOST=localhost"
if "%PGPORT%"=="" set "PGPORT=5432"
if "%PGUSER%"=="" set "PGUSER=postgres"

rem pg_dump: no PATH ou na instalação padrão do PostgreSQL
set "PG_DUMP=pg_dump"
where pg_dump >nul 2>nul
if errorlevel 1 (
    for /d %%v in ("%ProgramFiles%\PostgreSQL\*") do if exist "%%v\bin\pg_dump.exe" set "PG_DUMP=%%v\bin\pg_dump.exe"
)

for /f %%d in ('powershell -NoProfile -Command "Get-Date -Format yyyy-MM-dd_HHmm"') do set "DATA=%%d"
set "PASTA=%~dp0..\backups"
if not exist "%PASTA%" mkdir "%PASTA%"
set "ARQUIVO=%PASTA%\%BANCO%_%DATA%.backup"

echo Gerando backup de "%BANCO%" em %PGHOST%:%PGPORT%...
"%PG_DUMP%" -h %PGHOST% -p %PGPORT% -U %PGUSER% -F c -f "%ARQUIVO%" "%BANCO%"
if errorlevel 1 (
    echo.
    echo FALHOU: confira se o PostgreSQL está ligado, o usuário e a senha.
    if exist "%ARQUIVO%" del "%ARQUIVO%"
    exit /b 1
)
echo.
echo Backup salvo em:
echo   %ARQUIVO%
endlocal
