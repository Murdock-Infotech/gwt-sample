# modular-webapp

This repo contains:
- `modular-webapp-server/` (Jetty + Spring MVC) — see [`modular-webapp-server/README.md`](modular-webapp-server/README.md)
- `modular-webapp-client/` (GWT SuperDevMode / CodeServer) — see [`modular-webapp-client/README.md`](modular-webapp-client/README.md)

## VS Code / Cursor setup (Windows Git Bash + automation terminal)

If you use **Git Bash on Windows**, you (each developer) will need to update [`.vscode/settings.json`](.vscode/settings.json) to point to the correct local path for your Git Bash executable (`bash.exe`).

This project also sets the **automation terminal** so Cursor/VS Code can run tasks/debug terminals using the same shell:
- `terminal.integrated.automationProfile.windows`

Example (matches the current repo defaults):

```jsonc
{
  "terminal.integrated.profiles.windows": {
    "Git Bash": {
      "path": "C:\\Program Files\\Git\\bin\\bash.exe"
    }
  },
  "terminal.integrated.defaultProfile.windows": "Git Bash",
  "terminal.integrated.automationProfile.windows": {
    "path": "C:\\Program Files\\Git\\bin\\bash.exe"
  }
}
```

## Debug configurations (`launch.json`)

`.vscode/launch.json` is ignored (developer-local). If you already have it tracked in git history, you may need to untrack it once:

```bash
git rm --cached .vscode/launch.json
```

