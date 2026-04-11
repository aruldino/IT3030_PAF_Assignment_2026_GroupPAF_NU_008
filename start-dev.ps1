Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Set-Location $PSScriptRoot

function Import-DotEnvFile {
	param([string]$Path)

	if (-not (Test-Path $Path)) {
		return
	}

	Get-Content $Path | ForEach-Object {
		$line = $_.Trim()
		if ($line -eq '' -or $line.StartsWith('#')) {
			return
		}

		$separatorIndex = $line.IndexOf('=')
		if ($separatorIndex -lt 1) {
			return
		}

		$name = $line.Substring(0, $separatorIndex).Trim()
		$value = $line.Substring($separatorIndex + 1).Trim()
		if ($value.StartsWith('"') -and $value.EndsWith('"') -and $value.Length -ge 2) {
			$value = $value.Substring(1, $value.Length - 2)
		}

		[System.Environment]::SetEnvironmentVariable($name, $value, 'Process')
		Set-Item -Path "Env:$name" -Value $value
	}
}

Import-DotEnvFile -Path (Join-Path $PSScriptRoot '.env')

& npm.cmd run dev:all
