@echo off
chcp 65001 >nul
rem BLK Burguer - restauração de backup (#42)
rem Uso: restaurar.bat <arquivo.backup> [banco_destino]
rem Padrão do destino: blkburguer_restaurado (um banco SEPARADO, para conferir antes).
rem Para substituir o banco em uso, informe o nome dele: restaurar.bat arquivo.backup blkburguer
rem (feche o sistema em todos os computadores antes).
setlocal

if "%~1"=="" (
    echo Uso: restaurar.bat ^<arquivo.backup^> [banco_destino]
    exit /b 1
)
if not exist "%~1" (
    echo Arquivo não encontrado: %~1
    exit /b 1
)
set "ARQUIVO=%~1"
set "DESTINO=%~2"
if "%DESTINO%"=="" set "DESTINO=blkburguer_restaurado"
if "%PGHOST%"=="" set "PGHOST=localhost"
if "%PGPORT%"=="" set "PGPORT=5432"
if "%PGUSER%"=="" set "PGUSER=postgres"

set "PG_BIN="
where pg_restore >nul 2>nul
if errorlevel 1 (
    for /d %%v in ("%ProgramFiles%\PostgreSQL\*") do if exist "%%v\bin\pg_restore.exe" set "PG_BIN=%%v\bin\"
)

if /i "%DESTINO%"=="blkburguer" (
    echo ATENÇÃO: os dados atuais de "%DESTINO%" serão substituídos pelos do backup.
    set /p "CONFIRMA=Digite SIM para continuar: "
)
if /i "%DESTINO%"=="blkburguer" if /i not "%CONFIRMA%"=="SIM" (
    echo Cancelado.
    exit /b 1
)

echo Criando o banco "%DESTINO%" (se ainda não existir)...
"%PG_BIN%createdb" -h %PGHOST% -p %PGPORT% -U %PGUSER% "%DESTINO%" 2>nul

echo Restaurando "%ARQUIVO%" em "%DESTINO%"...
"%PG_BIN%pg_restore" -h %PGHOST% -p %PGPORT% -U %PGUSER% --clean --if-exists --no-owner -d "%DESTINO%" "%ARQUIVO%"
if errorlevel 1 (
    echo.
    echo FALHOU: veja as mensagens acima.
    exit /b 1
)
echo.
echo Restaurado em "%DESTINO%".
if /i not "%DESTINO%"=="blkburguer" echo Para usar este banco no sistema, ajuste db.url no db.properties.
endlocal
