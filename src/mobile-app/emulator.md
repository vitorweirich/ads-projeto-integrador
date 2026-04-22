## Configurar WSL para usar network mode mirrored (importante)
Criar na raiz do usuário (`notepad $env:USERPROFILE\.wslconfig`)
```
[wsl2]
networkingMode=mirrored
```

## Instalar build de release no emulador (dentro do wsl)

```
adb install -r android/app/build/outputs/apk/release/app-release.apk
```

## Instalar emuladores (em terminal windows)
```
emulator -list-avds
```

## Rodar emulador (em terminal windows)
```
emulator @Medium_Phone_API_36.0
```
