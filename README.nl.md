# CastIRL

**[English version](README.md)**

---

CastIRL is een gratis en open-source Android app waarmee je jouw telefoonscherm direct naar jouw eigen RTMP- of SRT-server kunt streamen. Geen abonnement, geen watermark, geen tussenpartij.

Het idee is simpel: waarom maandelijks betalen voor iets wat technisch gezien gewoon een encoder is? Je host je eigen server, de app regelt de rest.

---

## Functies

- RTMP en SRT streaming
- H.264 en H.265 hardware encoding
- Microfoon en systeemaudio opname (systeemaudio vereist Android 10+)
- Instelbare bitrate, resolutie, FPS en keyframe interval
- SRT latency, passphrase en sleutellengte instellen
- Stream profielen — sla configuraties op en wissel ertussen
- Automatisch opnieuw verbinden bij verbindingsverlies
- Live statistieken: bitrate, FPS, gedropte frames, duur
- Material 3 Expressive UI

---

## Vereisten

- Android 5.0 of hoger (API 21+)
- Een zelf-gehoste RTMP- of SRT-server

---

## Eigen server draaien (Docker)

Snel aan de slag met [MediaMTX](https://github.com/bluenviron/mediamtx):

```bash
docker run -d \
  --name mediamtx \
  -p 1935:1935 \
  -p 8554:8554 \
  -p 8888:8888 \
  bluenviron/mediamtx
```

RTMP ontvangt op `rtmp://jouw-ip:1935/live`, streamkey naar keuze.

---

## Bouwen vanuit broncode

1. Kloon de repository
2. Open in Android Studio (Hedgehog of nieuwer)
3. Laat Gradle synchroniseren
4. Start op een fysiek apparaat of emulator (API 21+)

```bash
git clone https://github.com/DIMENTS/CastIRL.git
cd castirl
./gradlew assembleDebug
```

> RootEncoder wordt automatisch opgehaald via JitPack tijdens de Gradle sync.

---

## Tech stack

| Onderdeel | Library |
|---|---|
| Streaming / encoding | [RootEncoder](https://github.com/pedroSG94/RootEncoder) |
| UI | Jetpack Compose + Material 3 |
| Dependency injection | Hilt |
| State | Coroutines + StateFlow |
| Instellingen opslaan | DataStore |
| Navigatie | Navigation Compose |

---

## Bijdragen

Pull requests zijn welkom. Voor grotere wijzigingen, open eerst een issue zodat we kunnen bespreken wat je wilt veranderen.

---

## Licentie

[MIT](LICENSE)

---

## Ondersteuning

Vind je CastIRL nuttig? Je kunt de ontwikkeling steunen via [diments.nl](https://diments.nl).
Community en feedback: [diments.nl/discord](https://diments.nl/discord)
