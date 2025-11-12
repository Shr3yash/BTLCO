# Build-And-Deploy.ps1 (verbose + verify)
$ErrorActionPreference = 'Stop'

### ==== EDIT THESE IF NEEDED ====
$ProjectRoot      = 'C:\Users\Shreyash\Documents\batelco-migration'
$BastionUser      = 'shbhatka'
$BastionHost = '193.123.77.23'
$RemoteTargetHost = '10.5.119.21'
$RemoteTargetUser = 'dev-user'
# Use '.' to drop into the login directory you normally use after SFTP
$RemoteTargetDir  = '.'
$LocalKeyForBastion = "$env:USERPROFILE\.ssh\id_rsa"  # or $null if you use SSO/agent
### ===============================

$TargetDir = Join-Path $ProjectRoot 'target'
$DateTag   = Get-Date -Format 'ddMM'
$OutName   = "XMLGen$DateTag.jar"
$OutPath   = Join-Path $TargetDir $OutName

Write-Host ">> Building fat JAR..."
Push-Location $ProjectRoot
& mvn -DskipTests clean package
Pop-Location

$fat = Get-ChildItem -Path $TargetDir -File -Filter '*-jar-with-dependencies.jar' |
       Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $fat) { throw "No fat JAR (*-jar-with-dependencies.jar) found in $TargetDir" }

Copy-Item -Force $fat.FullName $OutPath
Write-Host ">> Staged $OutName"

$RemoteTmp = "/tmp/$OutName"
$dest = "${BastionUser}@${BastionHost}:$RemoteTmp"
$scpArgs = @()
if ($LocalKeyForBastion -and (Test-Path $LocalKeyForBastion)) { $scpArgs += @('-i', $LocalKeyForBastion) }
$scpArgs += @($OutPath, $dest)
if (-not (Get-Command scp -ErrorAction SilentlyContinue)) { throw "scp not found in PATH." }
Write-Host ">> Copying to bastion: $dest"
& scp @scpArgs

$remoteCmd = @"
set -euo pipefail
echo '== Bastion: verifying staged file =='
ls -l "$RemoteTmp" || { echo 'File not on bastion'; exit 1; }

echo '== SFTP to target and upload (verbose) =='
printf '%s\n' 'pwd' 'ls -l' 'put '"$RemoteTmp"' '"$RemoteTargetDir"'' 'ls -l '"$RemoteTargetDir"'' 'bye' > /tmp/sftp.batch
sftp -vv -b /tmp/sftp.batch -o "IdentitiesOnly=yes" -i "/home/${BastionUser}/id_rsa_dev_user_brm" ${RemoteTargetUser}@${RemoteTargetHost}

echo '== Verify on target host =='
ssh -o "IdentitiesOnly=yes" -i "/home/${BastionUser}/id_rsa_dev_user_brm" ${RemoteTargetUser}@${RemoteTargetHost} bash -lc '
  set -e
  echo "Remote PWD:"; pwd
  echo "Looking for $OutName in: \$(pwd) and ~"
  ls -l "./$OutName" || ls -l "$HOME/$OutName" || { echo "Not in PWD or HOME; showing recent jars:"; ls -ltr *.jar 2>/dev/null || true; }
'
"@

if (-not (Get-Command ssh -ErrorAction SilentlyContinue)) { throw "ssh not found in PATH." }
Write-Host ">> From bastion, uploading and verifying on ${RemoteTargetUser}@${RemoteTargetHost} ..."
& ssh "${BastionUser}@${BastionHost}" $remoteCmd

Write-Host ">> Done. Expected file: $OutName on ${RemoteTargetUser}@${RemoteTargetHost} (in default login directory)"
