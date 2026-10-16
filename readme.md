Docker starten:
1. docker compose up -d
2. docker compose ps
3. docker compose exec postgres psql -U pictureframe -d pictureframe -c "SELECT version();"

GitIgnore refreshen:
- git rm --cached [.env]

Token generieren: (per powershell)
- $bytes = New-Object byte[] 32
- $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
- $rng.GetBytes($bytes)
- $token = [System.BitConverter]::ToString($bytes).Replace("-", "").ToLowerInvariant()
- $token

SH
- $tokenBytes = [System.Text.Encoding]::UTF8.GetBytes($token)
- $sha256 = [System.Security.Cryptography.SHA256]::Create()
- $hashBytes = $sha256.ComputeHash($tokenBytes)
- $sha256.Dispose()
- $hash = [System.BitConverter]::ToString($hashBytes).Replace("-", "").ToLowerInvariant()
- $hash