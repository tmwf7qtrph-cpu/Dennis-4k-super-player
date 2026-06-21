# Dennis 4K Super Player — Bouwinstructies

## OPTIE 1 — Volledig Automatisch via GitHub Actions (aanbevolen)

### Stap 1: Zet het project op GitHub

1. Maak een nieuw repository aan op **github.com** (gratis account volstaat)
2. Pak het gedownloade tar.gz uit
3. Ga in de uitgepakte map `dennis-player/` staan
4. Voer dit uit in je terminal:

```bash
git init
git add .
git commit -m "Dennis 4K Super Player v1.0.0"
git remote add origin https://github.com/JOUW-GEBRUIKERSNAAM/JOUW-REPO.git
git push -u origin main
```

### Stap 2: Wacht ~5 minuten

GitHub Actions bouwt de APK automatisch:
- Ga naar je repository op GitHub
- Klik op **Actions** (tabblad bovenaan)
- Je ziet een workflow genaamd **"Build Dennis 4K Super Player APK"**
- Wacht tot het groene vinkje ✓ verschijnt (~5 min)

### Stap 3: Download de APK

**Na de eerste push:**
- De APK verschijnt automatisch als **GitHub Release**
- Ga naar `github.com/JOUW-GEBRUIKERSNAAM/JOUW-REPO/releases`
- Download `Dennis-4K-Super-Player-v1.0.0.apk`

De APK is ook te vinden onder:
- **Actions** → klik op de laatste run → **Artifacts** → `Dennis-4K-Super-Player-APK`

---

## OPTIE 2 — Lokaal bouwen met Android Studio

### Vereisten

- **Java 21** (JDK 21) — https://adoptium.net/temurin/releases/?version=21
- **Android Studio** (gratis) — https://developer.android.com/studio

### Stappen

1. Pak `dennis-4k-super-player.tar.gz` uit
2. Open `android/` map in Android Studio
3. Wacht op Gradle sync (2-5 min eerste keer)
4. **Build → Build APK(s) → Build APK(s)**
5. APK staat in `android/app/build/outputs/apk/release/app-release.apk`

### Via command line:
```bash
cd android
./gradlew assembleRelease
```

---

## APK installeren op Android TV

```bash
# Via ADB (USB debugging moet aan staan op het apparaat)
adb install Dennis-4K-Super-Player-v1.0.0.apk

# Of: kopieer APK naar apparaat en open via Bestandsbeheerder
```

**Zorg dat "Onbekende bronnen installeren" aanstaat:**
- Android TV: Instellingen → Apparaatvoorkeuren → Beveiliging → Onbekende bronnen

---

## Stremio instellen

1. Open Stremio op je Android TV
2. Ga naar **Instellingen → Geavanceerd → Externe speler**
3. Selecteer **Dennis 4K Super Player**
4. Klaar — Stremio stuurt nu automatisch streams naar je speler

---

## Package info

| Veld | Waarde |
|------|--------|
| App naam | Dennis 4K Super Player |
| Package | nl.dennis.superplayer |
| Versie | 1.0.0 |
| Min Android | 10 (API 29) |
| Target Android | 11 (API 30) |
| Oriëntatie | Landscape only |
| Thema | Dark |
