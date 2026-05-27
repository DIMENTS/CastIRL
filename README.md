# CastIRL

**[Nederlandse versie](README.nl.md)**

---

CastIRL is a free and open-source Android app that streams your phone screen directly to your own RTMP or SRT server. No subscription, no watermark, no middleman.

The idea is simple: why pay a monthly fee for something that is technically just an encoder? You host your own server, the app does the rest.

---

## Features

- RTMP and SRT streaming
- H.264 and H.265 hardware encoding
- Microphone and system audio capture (system audio requires Android 10+)
- Adjustable bitrate, resolution, FPS and keyframe interval
- SRT latency, passphrase and key length configuration
- Stream profiles — save and switch between configurations
- Automatic reconnect on disconnect
- Live stats: bitrate, FPS, dropped frames, duration
- Material 3 Expressive UI

---

## Requirements

- Android 5.0 or higher (API 21+)
- A self-hosted RTMP or SRT server

---

## Self-hosted server (Docker)

A quick way to get started with [MediaMTX](https://github.com/bluenviron/mediamtx):

```bash
docker run -d \
  --name mediamtx \
  -p 1935:1935 \
  -p 8554:8554 \
  -p 8888:8888 \
  bluenviron/mediamtx
```

RTMP receives on `rtmp://your-ip:1935/live`, stream key of your choice.

---

## Building from source

1. Clone the repository
2. Open in Android Studio (Hedgehog or newer)
3. Let Gradle sync
4. Run on a physical device or emulator (API 21+)

```bash
git clone https://github.com/DIMENTS/CastIRL.git
cd castirl
./gradlew assembleDebug
```

> RootEncoder is fetched from JitPack automatically during the Gradle sync.

---

## Tech stack

| Concern | Library |
|---|---|
| Streaming / encoding | [RootEncoder](https://github.com/pedroSG94/RootEncoder) |
| UI | Jetpack Compose + Material 3 |
| Dependency injection | Hilt |
| State | Coroutines + StateFlow |
| Settings persistence | DataStore |
| Navigation | Navigation Compose |

---

## Contributing

Pull requests are welcome. For larger changes, open an issue first so we can discuss what you would like to change.

---

## License

[MIT](LICENSE)

---

## Support

If you find CastIRL useful, you can support development at [diments.nl](https://diments.nl).
Community and feedback: [diments.nl/discord](https://diments.nl/discord)
